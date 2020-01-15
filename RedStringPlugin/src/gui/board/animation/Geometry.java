package gui.board.animation;

import java.awt.Rectangle;
import javax.swing.JComponent;

/***
 * Represents different painting functionality
 */
public class Geometry {
    
    /** Point radius */
    private static final int radius = 5;
    
    /***
     * Paints a point
     * @param component target component
     * @param x x-coordinate
     * @param y y-coordinate
     * @param username point author
     */
    /*public static void paintPoint(JComponent component,
            int x, int y, String username) {
        paintPointSimpleTest(component, x, y, username);
    }*/
    
    /***
     * Paints a point (test method)
     * @param component target component
     * @param x x-coordinate
     * @param y y-coordinate
     * @param username point author
     */
    /*public static void paintPointSimpleTest(JComponent component,
            int x, int y, String username) {
        Graphics g = component.getGraphics();
        g.setColor(Color.RED);
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }*/
    
    /***
     * Defines if the point is inside this component
     * @param component target component
     * @param x x-coordinate
     * @param y y-coordinate
     * @return if the point is inside container
     */
    public static boolean hasPoint(JComponent component, int x, int y) {
        return x >= component.getX() && x <= component.getX() + 
                component.getWidth() &&
                y >= component.getY() && y <= component.getY() + 
                component.getHeight();
    }
    
    /***
     * Defines minimum distance between container bounds
     * @param oldBounds old bounds
     * @param newBounds new bounds
     * @return 
     */
    public static Line defineContainersDistance(Rectangle oldBounds,
            Rectangle newBounds) {
        return new Line((int) oldBounds.getCenterX(),
                (int) oldBounds.getCenterY(),
              (int) newBounds.getCenterX(),(int) newBounds.getCenterY());
        /*Line minLine;
        double minDis;
        minLine = distanceToRectangle(oldBounds.x, oldBounds.y, newBounds);
        minDis = minLine.length();
        Line line2 = distanceToRectangle(oldBounds.x + oldBounds.width, 
                oldBounds.y, newBounds);
        if (line2.length() < minDis) {
            minLine = line2;
            minDis = line2.length();
        }
        Line line3 = distanceToRectangle(oldBounds.x, 
                oldBounds.y + oldBounds.height, newBounds);
        if (line3.length() < minDis) {
            minLine = line3;
            minDis = line3.length();
        }
        Line line4 = distanceToRectangle(oldBounds.x + oldBounds.width, 
                oldBounds.y + oldBounds.height, newBounds);
        if (line4.length() < minDis) {
            minLine = line4;
        }
        return minLine;*/
    }
    
    /***
     * Calculates minimum distance from point to rectangle
     * @param x point x
     * @param y point y
     * @param rec rectangle
     */
    private static Line distanceToRectangle(int x, int y, Rectangle rec) {
        Line minLine;
        double minDis;
        minLine = new Line(x, y, rec.x, rec.y);
        minDis = minLine.length();
        Line line2 = new Line(x, y, rec.x + rec.width, rec.y);
        if (line2.length() < minDis) {
            minLine = line2;
            minDis = line2.length();
        }
        Line line3 = new Line(x, y, rec.x, rec.y + rec.height);
        if (line3.length() < minDis) {
            minLine = line3;
            minDis = line3.length();
        }
        Line line4 = new Line(x, y, rec.x + rec.width, rec.y + rec.height);
        if (line4.length() < minDis) {
            minLine = line4;
        }
        return minLine;
    }
}
