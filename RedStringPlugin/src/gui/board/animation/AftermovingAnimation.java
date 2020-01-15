package gui.board.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/***
 * Represents an animation after moving containers
 */
public class AftermovingAnimation extends Animation {
    
    /** Opacity step */
    private static final int step = 50;
    
    /** End value */
    private static final int endValue = 10;
    
    /** Line */
    private Line line;
    
    /** Current color */
    private Color color;
    
    /** Color opacity */
    private int alpha;

    public AftermovingAnimation(Animatable animatable, Line line, 
            Color color) {
        super(animatable);
        if (line == null) {
            throw new IllegalArgumentException("AftermovingAnimation: "
                    + "line is null!");
        }
        if (color == null) {
            throw new IllegalArgumentException("AftermovingAnimation: "
                    + "color is null!");
        }
        this.line = line;
        this.color = color;
        alpha = 255;
        this.color = new Color(color.getRed(), color.getGreen(),
            color.getBlue(), alpha);
    }
    
    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());
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
