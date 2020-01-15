package gui.container.image;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

/***
 * Represents an image paint listener
 */
public class PaintMouseListener extends MouseAdapter {
    
    /** Source cursor */
    private Cursor sourceCursor;
    
    /** Link to paint panel */
    private PaintPanel paintPanel;
    
    /** Link to container */
    private ImageContainerPanel container;

    /** Pressed point */
    private Point pressed;
    
    /** Link to current graphics */
    private Graphics2D g2d;
    
    /***
     * Constructor 
     * @param container link to container
     * @param paintJPanel link to paint panel 
     */
    public PaintMouseListener(ImageContainerPanel container,
            PaintPanel paintJPanel) {
        if (container == null) {
            throw new IllegalArgumentException("PaintMouseListener: "
                    + "container is null!");
        }
        if (paintJPanel == null) {
            throw new IllegalArgumentException("PaintMouseListener: "
                    + "paintJPanel is null!");
        }
        this.container = container;
        this.paintPanel = paintJPanel;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e); 
        sourceCursor = paintPanel.getCursor();
        paintPanel.setCursor(
                Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e); 
        paintPanel.setCursor(sourceCursor);
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e); 
        if (! SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        pressed = e.getPoint();
        if (paintPanel.getImage() == null) {
            paintPanel.setTransparentImage();
        }
        g2d = paintPanel.getImage().getImage().createGraphics();
        PaintSettings paintSettings = paintPanel.getPaintSettings();
        g2d.setStroke(new BasicStroke(paintSettings.getThickness()));
        g2d.setColor(paintSettings.getColor());
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			RenderingHints.VALUE_ANTIALIAS_ON);
        int th = paintSettings.getThickness();
        // Recover original points
        double delta = paintPanel.getScale() / 100.0;
        Point offset = paintPanel.calculateOffset();
        int x = (int)((pressed.x - offset.x) / delta);
        int y = (int)((pressed.y - offset.y) / delta);
        g2d.fillOval(x - th / 2, y - th / 2, th, th);
        paintPanel.repaint();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
        if (! SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        if (pressed != null) {
            // Recover original points
            double delta = paintPanel.getScale() / 100.0;
            Point offset = paintPanel.calculateOffset();
            int x1 = (int)((pressed.x - offset.x) / delta);
            int y1 = (int)((pressed.y - offset.y) / delta);
            int x2 = (int)((e.getX() - offset.x) / delta);
            int y2 = (int)((e.getY() - offset.y ) / delta);
            g2d.drawLine(x1, y1, x2, y2);
            paintPanel.repaint();
            pressed = e.getPoint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        if (! SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        pressed = null;
        g2d = null;
    } 
}
