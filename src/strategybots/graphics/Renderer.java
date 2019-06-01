package strategybots.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import strategybots.event.Event;
import strategybots.graphics.Window.WindowResizeEvent;

/**
 * Superclass for all shader classes.
 * Contains code for loading shaders from file.
 * @author Alec
 */
public abstract class Renderer {
    
    private int shaderProgramId, vertexShaderId, fragmentShaderId;
    private String vertexShaderFile, fragmentShaderFile;
    
    private Map<String, Integer> uniforms = new HashMap<>();
    
    private volatile boolean resized = false;
    
    protected Renderer(String vertexShader, String fragmentShader) {
        vertexShaderFile = vertexShader;
        fragmentShaderFile = fragmentShader;
    }
    
    protected abstract void bindAttribs();
    protected abstract void init();
    protected abstract void onWindowResize();
    protected abstract void render();
    
    public void doInit() {
        
        //Parse shader source files.
        vertexShaderId = parseShader(vertexShaderFile, GL_VERTEX_SHADER);
        fragmentShaderId = parseShader(fragmentShaderFile, GL_FRAGMENT_SHADER);
        
        //Create new shader program.
        shaderProgramId = glCreateProgram();
        
        //Attach custom shaders to shader program.
        glAttachShader(shaderProgramId, vertexShaderId);
        glAttachShader(shaderProgramId, fragmentShaderId);
        
        //Bind input attribute locations.
        bindAttribs();
        
        glLinkProgram(shaderProgramId);
        glValidateProgram(shaderProgramId);
        
        //Run any shader specific initialisation.
        glUseProgram(shaderProgramId);
        init();
        glUseProgram(0);
        
        Event.addHandler(WindowResizeEvent.class, e -> resized = true);
    }
    
    /**
     * Start rendering using this shader.
     */
    public void doRender() {
        glUseProgram(shaderProgramId);
        
        if(resized) {
            onWindowResize();
            resized = false;
        }
        render();
        glUseProgram(0);
    }
    
    /**
     * Permanently destroy this shader. Called during cleanup operations.
     */
    public void destroy() {
        
        glUseProgram(0);
        
        //Remove custom shaders from shader program.
        glDetachShader(shaderProgramId, vertexShaderId);
        glDetachShader(shaderProgramId, fragmentShaderId);
        
        //Delete custom shaders.
        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);
        
        //Delete shader program.
        glDeleteProgram(shaderProgramId);
    }
    
    protected void bindAttrib(int vaoId, String name) {
        glBindAttribLocation(shaderProgramId, vaoId, name);
    }
    
    protected void setUniform(String name, int value) {
        glUniform1i(locationOf(name), value);
    }
    
    protected void setUniform(String name, float value) {
        glUniform1f(locationOf(name), value);
    }
    
    protected void setUniform(String name, boolean value) {
        glUniform1f(locationOf(name), value ? 1 : 0);
    }
    
    protected void setUniform(String name, Vector2f vector) {
        glUniform2f(locationOf(name), vector.x, vector.y);
    }
    
    protected void setUniform(String name, Vector3f vector) {
        glUniform3f(locationOf(name), vector.x, vector.y, vector.z);
    }
    
    protected void setUniform(String name, Vector4f vector) {
        glUniform4f(locationOf(name), vector.x, vector.y, vector.z, vector.w);
    }
    
    protected void setUniform(String name, int[] values) {
        IntBuffer buffer = BufferUtils.createIntBuffer(values.length);
        buffer.put(values);
        buffer.rewind();
        glUniform1(locationOf(name), buffer);
    }
    
    protected void setUniform(String name, float[] values) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
        buffer.put(values);
        buffer.rewind();
        glUniform1(locationOf(name), buffer);
    }
    
    protected void setUniform(String name, Vector2f[] vectors) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vectors.length * 2);
        for(Vector2f vector : vectors) {
            buffer.put(vector.x);
            buffer.put(vector.y);
        }
        buffer.rewind();
        glUniform2(locationOf(name), buffer);
    }
    
    protected void setUniform(String name, Vector3f[] vectors) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vectors.length * 3);
        for(Vector3f vector : vectors) {
            buffer.put(vector.x);
            buffer.put(vector.y);
            buffer.put(vector.z);
        }
        buffer.rewind();
        glUniform3(locationOf(name), buffer);
    }
    
    private static FloatBuffer m3b = BufferUtils.createFloatBuffer(9);
    private static FloatBuffer m4b = BufferUtils.createFloatBuffer(16);
    
    protected void setUniform(String name, Matrix3f matrix) {
        matrix.store(m3b);
        m3b.flip();
        glUniformMatrix3(locationOf(name), false, m3b);
    }
    
    protected void setUniform(String name, Matrix4f matrix) {
        matrix.store(m4b);
        m4b.flip();
        glUniformMatrix4(locationOf(name), false, m4b);
    }
    
    /**
     * Returns the location of the uniform variable of the given name.
     */
    private int locationOf(String name) {
        //Query OpenGL for the uniform location if it isn't yet stored in uniforms.
        if(!uniforms.containsKey(name)) {
            uniforms.put(name, glGetUniformLocation(shaderProgramId, name));
        }
        return uniforms.get(name);
    }
    
    /*
     * Parse a shader source file, returning a shader id.
     */
    private int parseShader(String fileName, int shaderType) {
        
        //Read shader source from file.
        String[] src = new String[] {""};
        forEachLine(fileName, s -> src[0] += s + '\n');
        
        //Create shader and compile source.
        int shaderId = glCreateShader(shaderType);
        glShaderSource(shaderId, src[0]);
        glCompileShader(shaderId);
        
        //If the shader has errors, print them and quit.
        if(glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println(glGetShaderInfoLog(shaderId, 500));
            System.exit(0);
        }
        return shaderId;
    }
    
    private static void forEachLine(String fileName, Consumer<String> action) {
        
        try {
            
            InputStream s = Renderer.class.getResourceAsStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(s));
            reader.lines().forEach(action);
            reader.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}