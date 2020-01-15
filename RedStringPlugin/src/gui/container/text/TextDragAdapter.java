package gui.container.text;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.TransferHandler;

/***
 * Represents a text drag adapter
 */
public class TextDragAdapter extends MouseAdapter {
    
    /** Link to container */
    TextContainerPanel container;

    /***
     * Constructor
     * @param container link to container 
     */
    public TextDragAdapter(TextContainerPanel container) {
        if (container == null) {
            throw new IllegalArgumentException("TextDragAdapter: "
                    + "container is null!");
        }
        this.container = container;
    }
  
    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e); 
        container.getTransferHandler().exportAsDrag(container, 
                e, TransferHandler.COPY);
    }
    
    
}
