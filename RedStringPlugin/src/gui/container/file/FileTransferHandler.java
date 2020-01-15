package gui.container.file;

import gui.parsing.Parsing;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import rslib.gui.container.file.FileContainer;
import rslib.gui.container.file.FileModel;

/***
 * Represents an file transfer handler
 */
public class FileTransferHandler extends TransferHandler {

    /** Link to container */
    private final FileContainerPanel container;

    /***
     * Constructor
     * @param container link to container 
     */
    public FileTransferHandler(FileContainerPanel container) {
        if (container == null) {
            throw new IllegalArgumentException("FileTransferHandler: "
                    + "container is null!");
        }
        this.container = container;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (! support.isDrop()) {
            return false;
        }

        for (DataFlavor flavor : support.getDataFlavors()) {
            if (flavor.equals(DataFlavor.javaFileListFlavor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (! canImport(support)) {
            return false;
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
                processFile(file);
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
     * Checks if file can be processed
     * @param file file
     * @return whether it can
     */
    public static boolean checkFile(File file) {
        return ! file.isDirectory() && 
                file.length() <= FileContainer.FILE_MAXIMUM_SIZE;
    }
    
    /***
     * Processes DND file
     * @param file DND file
     */
    private void processFile(File file) {
        if (! checkFile(file)) {
            JOptionPane.showMessageDialog(container, "File can not be directory or "
                + "more than 5 MB!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        FileModel fileModel = Parsing.convertToFileModel(file);
        container.handleFile(fileModel);
    }
}
