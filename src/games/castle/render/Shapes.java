package games.castle.render;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;

import games.castle.render.Mesh;

public class Shapes {
	
private static Map<Integer, List<Integer>> vaos = new HashMap<>();
	
	public static final Mesh NULL = new Mesh(createVao(
			new float[] {}, new float[] {}, new float[] {}), 0);
	
	public static final Mesh SQUARE = new Mesh(createVao(
			
			new float[] {-0.5F, -0.5F,
						 0.5F, 0.5F,
						 -0.5F, 0.5F,
						 0.5F, 0.5F,
						 -0.5F, -0.5F,
						 0.5F, -0.5F},
			
			new float[] {0.0F, 1.0F,
						 1.0F, 0.0F,
						 0.0F, 0.0F,
						 1.0F, 0.0F,
						 0.0F, 1.0F,
						 1.0F, 1.0F})
	, 6);
	
	private static int createVao(float[] vertices, float[] normals, float[] textures) {
		
		int vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);
		
		List<Integer> vbos = new ArrayList<>();
		
		vbos.add(createVbo(0, vertices, 3));
		vbos.add(createVbo(1, normals, 3));
		vbos.add(createVbo(2, textures, 2));
		
		vaos.put(vaoId, vbos);
		
		glBindVertexArray(0);
		
		return vaoId;
	}
	
	private static int createVao(float[] vertices, float[] normals) {
		
		int vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);
		
		List<Integer> vbos = new ArrayList<>();
		
		vbos.add(createVbo(0, vertices, 2));
		vbos.add(createVbo(1, normals, 2));
		
		vaos.put(vaoId, vbos);
		
		glBindVertexArray(0);
		
		return vaoId;
	}
	
	public static void unload() {
		for(int vaoId : vaos.keySet()) {
			glDeleteVertexArrays(vaoId);
			for(int vboId : vaos.get(vaoId)) {
				glDeleteBuffers(vboId);
			}
		}
	}
	
	private static int createVbo(int attrib, float[] data, int dim) {
		
		int vboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		glVertexAttribPointer(attrib, dim, GL_FLOAT, false, 0, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0); 
		
		return vboId;
	}
}