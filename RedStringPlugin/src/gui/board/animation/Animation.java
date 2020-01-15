package gui.board.animation;

import java.awt.Graphics2D;

/***
 * Represents an animation interface
 */
public abstract class Animation {
    
    /** Entity where animation is to be drawn */
    private Animatable animatable;
    
    /** If animation was not set yet */
    private boolean newA;

    /***
     * Constructor
     * @param animatable entity where animation is to be drawn
     */
    public Animation(Animatable animatable) {
        if (animatable == null) {
            throw new IllegalArgumentException("Animation: "
                    + " animatable is null!");
        }
        this.animatable = animatable;
        newA = true;
    }
    
    /***
     * Draws the animation
     * @param g2d link to graphics
     */
    public abstract void draw(Graphics2D g2d);
    
    /***
     * Decrements the animation
     */
    public abstract void decrement();
    
    /***
     * Defines if the animation is ended
     * @return true if ended, false otherwise
     */
    public abstract boolean isEnded();

    /***
     * Checks if animation was not set yet
     * If it was not, it is set to animatable
     * @return true if it was new, false otherwise
     */
    public boolean checkIfNew() {
        if (newA) {
            animatable.addAnimation(this);
            newA = false;
            return true;
        }
        return false;
    }
    
    /***
     * Removes animation from animatable
     */
    public void removeAnimation() {
        animatable.removeAnimation(this);
    }
}
