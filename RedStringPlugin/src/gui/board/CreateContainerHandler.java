package gui.board;

import gui.container.ContainerPanel;
import gui.container.file.FileContainerPanel;
import gui.container.file.FileTransferHandler;
import gui.container.image.BufferedImageModel;
import gui.container.image.ImageContainerPanel;
import gui.container.image.ImageTransferHandler;
import gui.container.text.TextContainerPanel;
import gui.container.text.TextTransferHandler;
import gui.board_frame.control.ControlPanel;
import gui.parsing.Parsing;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import rslib.commands.container.AddContainerCommand;
import static rslib.gui.board.InteractiveBoard.BORDER_DELTA;
import rslib.gui.container.BoardContainer;
import rslib.gui.container.text.TextContainer;
import rslib.gui.container.file.FileModel;

/** Represents handling new container DND */
public class CreateContainerHandler extends TransferHandler {
        
    /** Link to board */
    private BoardPanel board;

    public CreateContainerHandler(BoardPanel board) {
        if (board == null) {
            throw new IllegalArgumentException("CreateContainerHandler: "
                    + "board is null!");
        }
        this.board = board;
    }
    
    @Override
    public boolean canImport(TransferSupport support) {
        if (! support.isDrop()) {
            return false;
        }
        for (DataFlavor flavor : support.getDataFlavors()) {
            if (flavor.equals(DataFlavor.imageFlavor) ||
                    flavor.equals(DataFlavor.javaFileListFlavor) ||
                    flavor.equals(ControlPanel.DNDCONTANER_FLAVOR)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (! canImport(support)){
            return false;
        }
        
        Point dropPoint = support.getDropLocation().getDropPoint();

        if (support.isDataFlavorSupported(ControlPanel.DNDCONTANER_FLAVOR)) {
            BoardContainer.ContainerType type;
            try {
                type = (BoardContainer.ContainerType)support.getTransferable().
                        getTransferData(ControlPanel.DNDCONTANER_FLAVOR);
            }
            catch (UnsupportedFlavorException | IOException e) {
                return false;
            }
            if (type != null) {
                processAddingContainer(type, dropPoint);
                return true;
            }
        }
        
        if (support.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            try {

                BufferedImage image = (BufferedImage) support.
                        getTransferable().getTransferData(
                        DataFlavor.imageFlavor);
                if (processImage(image, dropPoint)) {
                    return true;
                }
                // Continue processing as a file
            } 
            catch (UnsupportedFlavorException | IOException e) {
                return false;
            }
        }
        if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            java.util.List files;
            try {
                files = (java.util.List)support.getTransferable().
                        getTransferData(DataFlavor.javaFileListFlavor);
                if (files.size() != 1) {
                    return false;
                }
                File file = (File) files.get(0);
                processFile(file, dropPoint);
                return true;

            }
            catch (ClassCastException |
                    UnsupportedFlavorException | IOException e) {
                return false;
            }
        }
        return false;
    }
    
    /***
     * Processes adding new container
     * @param type container type
     * @param dropPoint drop point 
     */
    private void processAddingContainer(BoardContainer.ContainerType type,
            Point dropPoint) {
        
        dropPoint = correctDropPoint(type, dropPoint);
          
        // Creating container
        BoardContainer container = null;
        switch(type) {
            case TEXT_CONTAINER: {
                container = new TextContainerPanel(type, 
                        board.getCommandFacade().getUsername(),
                        board.generateId(), board, dropPoint);
                break;
            }
            case IMAGE_CONTAINER: {
                container = new ImageContainerPanel(type, 
                        board.getCommandFacade().getUsername(),
                        board.generateId(), board, dropPoint);
                break;
            }
            case FILE_CONTAINER: {
                container = new FileContainerPanel(type, 
                        board.getCommandFacade().getUsername(),
                        board.generateId(), board, dropPoint);
                break;
            }
        }
        if (container != null) {
            board.getCommandFacade().doCommand(new AddContainerCommand(
                    board, container), true);
        }
    }
    
    /***
     * Processes image
     * @param image image
     * @param dropPoint drop point 
     * @return if image was processed successfully
     */
    private boolean processImage(BufferedImage image, Point dropPoint) {
        if (! ImageTransferHandler.checkImage(image)) {
            return false;
        }
        BoardContainer.ContainerType type = 
                BoardContainer.ContainerType.IMAGE_CONTAINER;
        dropPoint = correctDropPoint(type, dropPoint);
        BoardContainer container = new ImageContainerPanel(type, 
                board.getCommandFacade().getUsername(),
                        board.generateId(), board, dropPoint, 
        new BufferedImageModel(image, 100, 0, 0));
        board.getCommandFacade().doCommand(new AddContainerCommand(
                    board, container), true);
        return true;
    }
    
    /***
     * Processes file
     * @param file file
     * @param dropPoint drop point 
     */
    private void processFile(File file, Point dropPoint) {
        if (ImageTransferHandler.checkImageFile(file)) {
            BufferedImage image = 
                    ImageTransferHandler.readImageFile(board, file);
            if (image != null) {
                if (processImage(image, dropPoint)) {
                    return;
                }
            }
        }
        // Continue processing
        if (TextTransferHandler.checkTextFile(file)) {
            String text = TextTransferHandler.readTextFile(board, file);
            if (text != null && text.length() 
                    <= TextContainer.TEXT_MAXIMUM_SIZE) {
                BoardContainer.ContainerType type = 
                BoardContainer.ContainerType.TEXT_CONTAINER;
                dropPoint = correctDropPoint(type, dropPoint);
                BoardContainer container = new TextContainerPanel(type, 
                        board.getCommandFacade().getUsername(),
                                board.generateId(), board, dropPoint, text);
                board.getCommandFacade().doCommand(new AddContainerCommand(
                            board, container), true);
                return;
            }
        }
        // Continue processing
        if (FileTransferHandler.checkFile(file)) {
            BoardContainer.ContainerType type = 
                BoardContainer.ContainerType.FILE_CONTAINER;
            dropPoint = correctDropPoint(type, dropPoint);
            FileModel fileModel = Parsing.convertToFileModel(file);
            BoardContainer container = new FileContainerPanel(type, 
                    board.getCommandFacade().getUsername(),
                            board.generateId(), board, dropPoint, fileModel);
            board.getCommandFacade().doCommand(new AddContainerCommand(
                            board, container), true);
            return;
        }
        JOptionPane.showMessageDialog(board, "File can not bet directory or "
                + "more than 5 MB!", "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /***
     * Corrects drop point depending on container type
     * @param type container type
     * @param dropPoint drop point
     * @return drop point
     */
    private Point correctDropPoint(BoardContainer.ContainerType type, 
            Point dropPoint) {
        Dimension minDim = ContainerPanel.MINIMUM_DIMENSIONS.get(type);
        dropPoint.x -= minDim.width / 2;
        dropPoint.y -= minDim.height / 2;
        if (dropPoint.x + minDim.width > board.getComponentWidth() 
                - BORDER_DELTA) {
            dropPoint.x = board.getComponentWidth() 
                - BORDER_DELTA - minDim.width;
        } 
        if (dropPoint.x < BORDER_DELTA) {
            dropPoint.x = BORDER_DELTA;
        }
        if (dropPoint.y + minDim.height > board.getComponentHeight()
                - BORDER_DELTA) {
            dropPoint.y = board.getComponentHeight()
                - BORDER_DELTA - minDim.height;
        }
        if (dropPoint.y < BORDER_DELTA) {
            dropPoint.y = BORDER_DELTA;
        }
        return dropPoint;
    }
}
