package gui.container;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/***
 * Represents an adapter to the container
 */
public class DispatchAdapter extends MouseAdapter {

    /** Link to container */
    private final ContainerPanel container;

    public DispatchAdapter(ContainerPanel container) {
        this.container = container;
        if (container == null) {
            throw new IllegalArgumentException("DispacthAdapter: "
                    + "container is null!");
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        container.dispatchEvent(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        container.dispatchEvent(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        container.dispatchEvent(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        container.dispatchEvent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        container.dispatchEvent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        container.dispatchEvent(e);
    } 
}
