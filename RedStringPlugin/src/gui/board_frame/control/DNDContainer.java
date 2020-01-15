package gui.board_frame.control;

import gui.util.Images;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import rslib.gui.container.BoardContainer;

/**
 * *
 * Represents a DND adding container element
 */
public class DNDContainer extends JButton {

    /** Icon*/
    private Image icon;
    
    /** Container type */
    private BoardContainer.ContainerType type;

    /***
     * Constructor
     * @param type container type
     */
    public DNDContainer(BoardContainer.ContainerType type) {
        if (type == null) {
            throw new IllegalArgumentException("DNDContainerLabel: "
                    + "type is null!");
        }
        this.type = type;
        this.icon = Images.CONTAINER_IMAGES.get(type);
        setIcon(Images.CONTAINER_ICONS.get(type));
        setPreferredSize(ControlPanel.CONTROL_BUTTON_DIMENSION);
        setTransferHandler(new ComponentHandler());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e)
            {
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, e, TransferHandler.MOVE);
            }
        });
        
    }
    
    

    /**
     * *
     * Represents a handler for button
     */
    class ComponentHandler extends TransferHandler {

        @Override
        public int getSourceActions(JComponent c) {
            setDragImage(icon);
            return MOVE;
        }

        @Override
        public Transferable createTransferable(final JComponent c) {
            return new Transferable() {
                @Override
                public Object getTransferData(DataFlavor flavor) {
                    return type;
                }

                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    DataFlavor[] flavors = new DataFlavor[1];
                    flavors[0] = ControlPanel.DNDCONTANER_FLAVOR;
                    return flavors;
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return flavor.equals(ControlPanel.DNDCONTANER_FLAVOR);
                }
            };
        }
    }
}
