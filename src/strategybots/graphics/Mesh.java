package strategybots.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;

public class Mesh {
    
    public static final Mesh NULL = new Mesh(createVao(
            new float[] {}, new float[] {}), 0);
    
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
    
    private static Map<Integer, List<Integer>> vaos = new HashMap<>();
    
    private final int vaoId, numVertices;
    
    private Mesh(int vaoId, int numVertices) {
        this.vaoId = vaoId;
        this.numVertices = numVertices;
    }
    
    public int getVaoId() { return vaoId; }
    
    public int getNumVertices() { return numVertices; }
    
    public static void deleteAll() {
        
        for(int vaoId : vaos.keySet()) {
            glDeleteVertexArrays(vaoId);
            for(int vboId : vaos.get(vaoId)) {
                glDeleteBuffers(vboId);
            }
        }
    }
    
    private static int createVao(float[] vertices, float[] textures) {
        
        int vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        
        List<Integer> vbos = new ArrayList<>();
        
        vbos.add(createVbo(0, vertices, 2));
        vbos.add(createVbo(1, textures, 2));
        
        //vaos.put(vaoId, vbos);
        
        glBindVertexArray(0);
        
        return vaoId;
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