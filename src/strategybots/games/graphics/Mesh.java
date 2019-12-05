package strategybots.games.graphics;

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

/**
 * A VAO mesh for rendering.
 * 
 * @author Alec Dorrington
 */
public class Mesh {
    
    /** Set of loaded VAOs indexed by ID. */
    private static Map<Integer, List<Integer>> vaos = new HashMap<>();
    
    /** Empty mesh. */
    public static final Mesh NULL = new Mesh(createVao(
            new float[] {}, new float[] {}), 0);
    
    /** Basic square mesh. */
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
    
    /** ID of VAO and number of vertices for each mesh. */
    private final int vaoId, numVertices;
    
    /**
     * @param vaoId ID of the mesh VAO.
     * @param numVertices the number of vertices in the mesh.
     */
    private Mesh(int vaoId, int numVertices) {
        this.vaoId = vaoId;
        this.numVertices = numVertices;
    }
    
    /**
     * @return the ID of the VAO of the mesh.
     */
    public int getVaoId() { return vaoId; }
    
    /**
     * @return the number of vertices in the mesh.
     */
    public int getNumVertices() { return numVertices; }
    
    /**
     * Unload all VAOs, to be called upon clean-up.
     */
    public static void deleteAll() {
        
        //Delete each VAO.
        for(int vaoId : vaos.keySet()) {
            glDeleteVertexArrays(vaoId);
            //Delete each VBO associated with the VAO.
            for(int vboId : vaos.get(vaoId)) {
                glDeleteBuffers(vboId);
            }
        }
    }
    
    /**
     * 
     * @param vertices array of 2D vertices in VAO.
     * @param textures array of 2D texture coordinates in VAO.
     * @return the ID of the VAO.
     */
    private static int createVao(float[] vertices, float[] textures) {
        
        //Create new VAO.
        int vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        
        //Create VBOs for VAO (one each for vertices and texcoords).
        List<Integer> vbos = new ArrayList<>();
        vbos.add(createVbo(0, vertices, 2));
        vbos.add(createVbo(1, textures, 2));
        
        //Save reference to VAO, unbind and return ID.
        vaos.put(vaoId, vbos);
        glBindVertexArray(0);
        return vaoId;
    }
    
    /**
     * @param attrib the attribute ID of the VBO.
     * @param data the array of vertices for the VBO.
     * @param dim the dimensionality of the given data.
     * @return the ID of the VBO.
     */
    private static int createVbo(int attrib, float[] data, int dim) {
        
        //Create new VBO.
        int vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        
        //Load data into VBO.
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glVertexAttribPointer(attrib, dim, GL_FLOAT, false, 0, 0);
        
        //Unbind VBO and return ID.
        glBindBuffer(GL_ARRAY_BUFFER, 0); 
        return vboId;
    }
}