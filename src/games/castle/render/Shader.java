package games.castle.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

/**
 * Superclass for all shader classes.
 * Contains code for loading shaders from file.
 * @author Alec
 */
public abstract class Shader {
	
	private final int SHADER_PROGRAM_ID, VERTEX_SHADER_ID, FRAGMENT_SHADER_ID;
	
	private Map<String, Integer> uniforms = new HashMap<>();
	
	protected Shader(String vertexShader, String fragmentShader) {
		
		//Parse shader source files.
		VERTEX_SHADER_ID = parseShader(vertexShader, GL_VERTEX_SHADER);
		FRAGMENT_SHADER_ID = parseShader(fragmentShader, GL_FRAGMENT_SHADER);
		
		//Create new shader program.
		SHADER_PROGRAM_ID = glCreateProgram();
		
		//Attach custom shaders to shader program.
		glAttachShader(SHADER_PROGRAM_ID, VERTEX_SHADER_ID);
		glAttachShader(SHADER_PROGRAM_ID, FRAGMENT_SHADER_ID);
		
		//Bind input attribute locations.
		bindAttribs();
		
		glLinkProgram(SHADER_PROGRAM_ID);
		glValidateProgram(SHADER_PROGRAM_ID);
		
		//Run any shader specific initialisation.
		glUseProgram(SHADER_PROGRAM_ID);
		init();
		glUseProgram(0);
		
		//Call onWindowResize when the window is resized.
		//Event.addHandler(WindowResizeEvent.class, e -> {
		//	onWindowResize(e.WIDTH, e.HEIGHT);
		//});
	}
	
	/**
	 * Start rendering using this shader.
	 */
	public void start() {
		glUseProgram(SHADER_PROGRAM_ID);
		loadFrame();
	}
	
	/**
	 * Stop rendering using this shader.
	 */
	public void stop() {
		unloadFrame();
		glUseProgram(0);
	}
	
	/**
	 * Permanently destroy this shader. Called during cleanup operations.
	 */
	public void destroy() {
		
		stop();
		
		//Remove custom shaders from shader program.
		glDetachShader(SHADER_PROGRAM_ID, VERTEX_SHADER_ID);
		glDetachShader(SHADER_PROGRAM_ID, FRAGMENT_SHADER_ID);
		
		//Delete custom shaders.
		glDeleteShader(VERTEX_SHADER_ID);
		glDeleteShader(FRAGMENT_SHADER_ID);
		
		//Delete shader program.
		glDeleteProgram(SHADER_PROGRAM_ID);
	}
	
	protected void bindAttrib(int vaoId, String name) {
		glBindAttribLocation(SHADER_PROGRAM_ID, vaoId, name);
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
	
	protected abstract void bindAttribs();
	protected abstract void init();
	protected abstract void loadFrame();
	protected abstract void unloadFrame();
	
	/**
	 * Returns the location of the uniform variable of the given name.
	 */
	private int locationOf(String name) {
		//Query OpenGL for the uniform location if it isn't yet stored in uniforms.
		if(!uniforms.containsKey(name)) {
			uniforms.put(name, glGetUniformLocation(SHADER_PROGRAM_ID, name));
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
			
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			reader.lines().forEach(action);
			reader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}