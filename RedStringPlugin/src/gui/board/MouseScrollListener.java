package gui.board;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JViewport;

/***
 * Represents a listener that allows scrolling via mouse
 */
public class MouseScrollListener extends MouseAdapter {

    /** Source cursor */
    private Cursor sourceCursor;

    /** Component to scroll */
    private final JComponent component;
    
    /** Component viewport */
    private final JViewport viewport;
    
    /** Old point */
    private Point old;

    /***
     * Constructor
     * @param component component to scroll
     * @param viewport component viewport
     */
    public MouseScrollListener(JComponent component, JViewport viewport) {
        this.component = component;
        this.viewport = viewport;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        sourceCursor = component.getCursor();
        component.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        old = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point point = e.getPoint();
        Point viewPoint = viewport.getViewPosition();
        if (old != null) {
        viewPoint.translate(old.x - point.x, old.y - point.y);
            component.scrollRectToVisible(new Rectangle(viewPoint, 
                    viewport.getSize()));
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        component.setCursor(sourceCursor);
        sourceCursor = null;
        old = null;
    }
}
