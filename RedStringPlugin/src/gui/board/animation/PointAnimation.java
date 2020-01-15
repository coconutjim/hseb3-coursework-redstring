package gui.board.animation;

import java.awt.Color;
import java.awt.Graphics2D;

/***
 * Represents a point animation
 */
public class PointAnimation extends Animation {
    
    /** Radius */
    private static final int radius = 10;
    
    /** Opacity step */
    private static final int step = 50;
    
    /** End value */
    private static final int endValue = 10;
    
    /** X - coordinate */
    private int x;
    
    /** Y - coordinate */
    private int y;
    
    /** Current color */
    private Color color;
    
    /** Color opacity */
    private int alpha;

    public PointAnimation(Animatable animatable,
            int x, int y, Color color) {
        super(animatable);
        if (color == null) {
            throw new IllegalArgumentException("PointAnimation: "
                    + "color is null!");
        }
        this.x = x;
        this.y = y;
        alpha = 255;
        this.color = new Color(color.getRed(), color.getGreen(),
            color.getBlue(), alpha);
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    @Override
    public void decrement() {
        alpha -= step;
        if (alpha > endValue) {
            color = new Color(color.getRed(), color.getGreen(), 
                    color.getBlue(), alpha);
        }
    }

    @Override
    public boolean isEnded() {
        return alpha <= endValue;
    }
}
