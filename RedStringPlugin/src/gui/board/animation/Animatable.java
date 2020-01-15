package gui.board.animation;

/***
 * Represents an entity that can draw animations
 */
public interface Animatable {
    
    /***
     * Adds an animation to draw
     * @param animation animation
     */
    public void addAnimation(Animation animation);
    
    /***
     * Removes animation
     * @param animation animation
     */
    public void removeAnimation(Animation animation);
    
}
