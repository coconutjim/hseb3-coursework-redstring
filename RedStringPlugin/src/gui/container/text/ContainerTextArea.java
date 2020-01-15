package gui.container.text;

import gui.board.animation.Animatable;
import gui.board.animation.Animation;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JTextArea;

/***
 * Represents an area to text
 */
public class ContainerTextArea extends JTextArea 
        implements Animatable {
    
    /** Animations */
    private final List<Animation> animations;

    /***
     * Constructor
     */
    public ContainerTextArea() {
        animations = new CopyOnWriteArrayList<>();
        setOpaque(true);
        setLineWrap(true);
        setEditable(false);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			RenderingHints.VALUE_ANTIALIAS_ON);
        for (Animation animation : animations) {
            animation.draw(g2d);
        }
    }

    @Override
    public void addAnimation(Animation animation) {
        animations.add(animation);
    }

    @Override
    public void removeAnimation(Animation animation) {
        animations.remove(animation);
    }
}
