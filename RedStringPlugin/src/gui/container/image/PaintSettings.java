package gui.container.image;

import java.awt.Color;

/***
 * Represents a class that hold info about thickness and color
 */
public class PaintSettings {
    
    /** Constants */
    public static final int MIN_THICKNESS = 1;
    public static final int MAX_THICKNESS = 5;
    
    /** Paint thickness */
    private int thickness;
    
    /** Paint color */
    private Color color;

    /***
     * Constructor
     * @param thickness paint thickness
     * @param color paint color
     */
    public PaintSettings(int thickness, Color color) {
        if (thickness < 0 || thickness > 5) {
            throw new IllegalArgumentException("PaintSettings: "
                    + "illegal thickness!");
        }
        if (color == null) {
            throw new IllegalArgumentException("PaintSettings: "
                    + "color is null!");
        }
        this.thickness = thickness;
        this.color = color;
    }

    public int getThickness() {
        return thickness;
    }

    public Color getColor() {
        return color;
    }
}
