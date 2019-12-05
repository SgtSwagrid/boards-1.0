package strategybots.games.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GLContext;
import org.newdawn.slick.opengl.TextureLoader;

/**
 * A texture loaded from file used in rendering.
 * 
 * @author Alec Dorrington
 */
public class Texture {
   
    /** Set of all currently loaded textures. */
    private static Map<String, Texture> textures = new HashMap<>();
    
    /** The file name for this texture. */
    private String fileName;
    
    /** The ID of this texture. */
    private int textureId = -1;
    
    /**
     * Creates a new texture.
     * @param fileName the texture file to load.
     */
    private Texture(String fileName) {
        this.fileName = fileName;
    }
    
    /**
     * Get or create a texture from a file name.
     * @param fileName the file name of the texture to load.
     * @return the texture associated with this file name.
     */
    public static Texture getTexture(String fileName) {
        //Create new texture if it isn't yet loaded.
        if(!textures.containsKey(fileName))
            textures.put(fileName, new Texture(fileName));
        //Return existing texture if it is loaded.
        return textures.get(fileName);
    }
    
    /**
     * @return the ID OpenGL has associated with this texture.
     */
    public int getTextureId() {
        if(textureId == -1) textureId = loadPng(fileName);
        return textureId;
    }
    
    /**
     * @return whether this texture is fully opaque.
     */
    public boolean isOpaque() { return true; }
    
    /**
     * Load a new PNG image from a file, returning a texture ID.
     * @param fileName the name of the image.
     * @return the ID of the texture.
     */
    private int loadPng(String fileName) {
        
        int textureId = 0;
        
        try {
            //Load texture.
            textureId = TextureLoader
                    .getTexture("PNG", new FileInputStream(fileName))
                    .getTextureID();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //Enable mipmapping.
        glGenerateMipmap(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, 0);
        
        //Enable anisotropic filtering.
        if(GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            float filtering = Math.min(16F, glGetFloat(
                    EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
            glTexParameterf(GL_TEXTURE_2D,
                    EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, filtering);
        }
        return textureId;
    }
}