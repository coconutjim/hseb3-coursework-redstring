package gui.board.animation;

import java.awt.Color;

/***
 * Represents an entity that can be pointed
 */
public interface Pointable {

    /***
     * Marks the point on the element
     * (may call point on the child element)
     * @param am link to animation manager
     * @param x x-coordinate
     * @param y y-coordinate
     * @param color point color
     */
    public void point(AnimationManager am, int x, int y, Color color);
}
