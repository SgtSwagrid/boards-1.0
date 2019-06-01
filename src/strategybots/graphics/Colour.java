package strategybots.graphics;

import org.lwjgl.util.vector.Vector4f;

/**
 * Helper class containing colour presets and utilities.
 * An instance of this class represents a particular colour.
 * @author Alec
 */
public class Colour {
    
    /** Aqua colour; hex code 0x00FFFF. */
    public static final Colour AQUA = hex(0x00FFFF);
    
    /** Black colour; hex code 0x000000. */
    public static final Colour BLACK = hex(0x000000);
    
    /** Blue colour; hex code 0x0000FF. */
    public static final Colour BLUE = hex(0x0000FF);
    
    /** Fuchsia colour; hex code 0xFF00FF. */
    public static final Colour FUCHSIA = hex(0xFF00FF);
    
    /** Gray colour; hex code 0x808080. */
    public static final Colour GRAY = hex(0x808080);
    
    /** Green colour; hex code 0x00FF00. */
    public static final Colour GREEN = hex(0x008000);
    
    /** Lime colour: hex code 0x00FF00. */
    public static final Colour LIME = hex(0x00FF00);
    
    /** Maroon colour; hex code 0x800000. */
    public static final Colour MAROON = hex(0x800000);
    
    /** Navy colour; hex code 0x000080. */
    public static final Colour NAVY = hex(0x000080);
    
    /** Olive colour; hex code 0x808000. */
    public static final Colour OLIVE = hex(0x808000);
    
    /** Orange colour; hex code 0xFFA500. */
    public static final Colour ORANGE = hex(0xFFA500);
    
    /** Purple colour; hex code 0x800080. */
    public static final Colour PURPLE = hex(0x800080);
    
    /** Red colour; hex code 0xFF0000. */
    public static final Colour RED = hex(0xFF0000);
    
    /** Silver colour; hex code 0xC0C0C0. */
    public static final Colour SILVER = hex(0xC0C0C0);
    
    /** Teal colour; hex code 0x008080. */
    public static final Colour TEAL = hex(0x008080);
    
    /** White colour; hex code 0xFFFFFF. */
    public static final Colour WHITE = hex(0xFFFFFF);
    
    /** Yellow colour; hex code 0xFFFF00. */
    public static final Colour YELLOW = hex(0xFFFF00);
    
    /** Colour channel value, from 0.0-1.0. */
    public final float R, G, B, A;
    
    /**
     * Returns a colour object representing the given RGB value.
     * @param r red colour channel 0-255.
     * @param g green colour channel 0-255.
     * @param b blue colour channel 0-255.
     * @return the colour vector.
     */
    public static Colour rgb(int r, int g, int b) {
        return new Colour(
                r / 255.0F,
                g / 255.0F,
                b / 255.0F, 1.0F);
    }
    
    /**
     * Returns a colour object representing the given RGBA value.
     * @param r red colour channel 0-255.
     * @param g green colour channel 0-255.
     * @param b blue colour channel 0-255.
     * @param a alpha colour channel 0-255.
     * @return the colour vector.
     */
    public static Colour rgba(int r, int g, int b, int a) {
        return new Colour(
                r / 255.0F,
                g / 255.0F,
                b / 255.0F,
                a / 255.0F);
    }
    
    /**
     * Returns a colour vector representing the given hexadecimal value.
     * Hexadecimal values are integers prefixed by '0x'.
     * This is not compatible with colours containing an alpha value.
     * For colours with an alpha value, use hexa() instead.
     * @param hex a hexadecimal colour value.
     * @return the colour vector.
     */
    public static Colour hex(int hex) {
        return rgb(
                hex / 65536,
                (hex / 256) % 256,
                hex % 256);
    }
    
    /**
     * Returns a colour vector representing the given hexadecimal value.
     * Hexadecimal values are integers prefixed by '0x'.
     * This is not compatible with colours without an alpha value.
     * For colours with no alpha value, use hex() instead.
     * @param hex a hexadecimal colour value.
     * @return the colour vector.
     */
    public static Colour hexa(int hex) {
        return rgba(
                hex / 16777216,
                (hex / 65536) % 256,
                (hex / 265) % 256,
                hex % 256);
    }
    
    public static Colour mix(Colour... colours) {
        
        float r = 0.0F, g = 0.0F, b = 0.0F, a = 0.0F;
        
        for(Colour colour : colours) {
            
            r += colour.R / colours.length;
            g += colour.G / colours.length;
            b += colour.B / colours.length;
            a += colour.A / colours.length;
        }
        
        return new Colour(r, g, b, a);
    }
    
    /**
     * Initializes a colour with the given colour channel values.
     * @param red colour channel 0.0-1.0.
     * @param green colour channel 0.0-1.0.
     * @param blue colour channel 0.0-1.0.
     * @param alpha colour channel 0.0-1.0.
     */
    private Colour(float r, float g, float b, float a) {
        R = r;
        G = g;
        B = b;
        A = a;
    }
    
    public Colour darken(float amount) {
        return new Colour(R - amount, G - amount, B - amount, A);
    }
    
    public Colour lighten(float amount) {
        return new Colour(R + amount, G + amount, B + amount, A);
    }
    
    /**
     * Returns a vector representation of this colour for shader compatibility.
     * r, g, b and a map to x, y, z and w respectively.
     * @return this colour, as a vector.
     */
    public Vector4f asVector() {
        return new Vector4f(R, G, B, A);
    }
    
    @Override public String toString() {
        return asVector().toString();
    }
    
    @Override public boolean equals(Object o) {
        if(!(o instanceof Colour)) return false;
        Colour c = (Colour) o;
        return asVector().equals(c.asVector());
    }
}