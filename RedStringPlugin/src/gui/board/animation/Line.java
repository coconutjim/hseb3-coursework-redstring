package gui.board.animation;

/***
 * Represents a line
 */
public class Line {
    
    /** Start coordinates */
    private final int x1;
    private final int y1;
    
    /** End coordinates */
    private final int x2;
    private final int y2;

    /***
     * Constructor
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
    public Line(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
    
    /***
     * Calculates line length
     * @return length
     */
    public double length() {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }
}
