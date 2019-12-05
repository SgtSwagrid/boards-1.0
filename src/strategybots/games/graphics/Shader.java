package strategybots.games.graphics;

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

import strategybots.games.event.Event;
import strategybots.games.graphics.Window.WindowResizeEvent;

/**
 * Superclass for all shader classes.
 * Contains code for loading shaders from file.
 * 
 * @author Alec Dorrington
 */
public abstract class Shader {
    
    /** IDs for shader programs. */
    private int shaderProgramId, vertexShaderId, fragmentShaderId;
    /** Source files for shader code. */
    private String vertexShaderFile, fragmentShaderFile;
    
    /** Uniform variables associated with this shader. */
    private Map<String, Integer> uniforms = new HashMap<>();
    
    /** Whether the window has been resized. */
    private volatile boolean resized = false;
    
    /**
     * @param vertexShader the file containing vertex shader source.
     * @param fragmentShader the file containing fragment shader source.
     */
    protected Shader(String vertexShader, String fragmentShader) {
        vertexShaderFile = vertexShader;
        fragmentShaderFile = fragmentShader;
    }
    
    /**
     * Used to bind names to VBOs.
     * Called once upon initialization.
     */
    protected abstract void bindAttribs();
    
    /**
     * Used to perform shader-specific initialization.
     * Called once upon initialization.
     */
    protected abstract void init();
    
    /**
     * Used to adjust shader based on window size.
     * Called once each time the window is resized.
     */
    protected abstract void onWindowResize();
    
    /**
     * Used to render contents.
     * Called once per shader per frame.
     */
    protected abstract void render();
    
    /**
     * Initialize shader program.
     */
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
    
    /**
     * Bind a name to a VBO.
     * @param vboId the ID of the VBO.
     * @param name the name of the VBO.
     */
    protected void bindAttrib(int vboId, String name) {
        glBindAttribLocation(shaderProgramId, vboId, name);
    }
    
    /**
     * Load integer uniform.
     * @param name of uniform.
     * @param value assigned to uniform.
     */
    protected void setUniform(String name, int value) {
        glUniform1i(locationOf(name), value);
    }
    
    /**
     * Load float uniform.
     * @param name of uniform.
     * @param value assigned to uniform.
     */
    protected void setUniform(String name, float value) {
        glUniform1f(locationOf(name), value);
    }
    
    /**
     * Load boolean uniform.
     * @param name of uniform.
     * @param value assigned to uniform.
     */
    protected void setUniform(String name, boolean value) {
        glUniform1f(locationOf(name), value ? 1 : 0);
    }
    
    /**
     * Load vector2 uniform.
     * @param name of uniform.
     * @param value assigned to uniform.
     */
    protected void setUniform(String name, Vector2f vector) {
        glUniform2f(locationOf(name), vector.x, vector.y);
    }
    
    /**
     * Load vector3 uniform.
     * @param name of uniform.
     * @param value assigned to uniform.
     */
    protected void setUniform(String name, Vector3f vector) {
        glUniform3f(locationOf(name), vector.x, vector.y, vector.z);
    }
    
    /**
     * Load vector4 uniform.
     * @param name of uniform.
     * @param value assigned to uniform.
     */
    protected void setUniform(String name, Vector4f vector) {
        glUniform4f(locationOf(name), vector.x, vector.y, vector.z, vector.w);
    }
    
    /**
     * Load int array uniform.
     * @param name of uniform.
     * @param value assigned to uniform.
     */
    protected void setUniform(String name, int[] values) {
        IntBuffer buffer = BufferUtils.createIntBuffer(values.length);
        buffer.put(values);
        buffer.rewind();
        glUniform1(locationOf(name), buffer);
    }
    
    /**
     * Load float array uniform.
     * @param name of uniform.
     * @param value assigned to uniform.
     */
    protected void setUniform(String name, float[] values) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
        buffer.put(values);
        buffer.rewind();
        glUniform1(locationOf(name), buffer);
    }
    
    /**
     * Load vector2 array uniform.
     * @param name of uniform.
     * @param value assigned to uniform.
     */
    protected void setUniform(String name, Vector2f[] vectors) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vectors.length * 2);
        for(Vector2f vector : vectors) {
            buffer.put(vector.x);
            buffer.put(vector.y);
        }
        buffer.rewind();
        glUniform2(locationOf(name), buffer);
    }
    
    /**
     * Load vector3 array uniform.
     * @param name of uniform.
     * @param value assigned to uniform.
     */
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
    
    /** Float buffer used to load matrix3 uniform. */
    private static FloatBuffer m3b = BufferUtils.createFloatBuffer(9);
    /** Float buffer used to load matrix4 uniform. */
    private static FloatBuffer m4b = BufferUtils.createFloatBuffer(16);
    
    /**
     * Load matrix3 uniform.
     * @param name of uniform.
     * @param value assigned to uniform.
     */
    protected void setUniform(String name, Matrix3f matrix) {
        matrix.store(m3b);
        m3b.flip();
        glUniformMatrix3(locationOf(name), false, m3b);
    }
    
    /**
     * Load matrix4 uniform.
     * @param name of uniform.
     * @param value assigned to uniform.
     */
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
    
    /**
     * Perform an each for each line in a file.
     * @param fileName the file to read.
     * @param action the action to perform for each line.
     */
    private static void forEachLine(String fileName, Consumer<String> action) {
        
        try {
            
            //Read lines of file.
            InputStream s = Shader.class.getResourceAsStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(s));
            //Perform action for each line.
            reader.lines().forEach(action);
            reader.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}