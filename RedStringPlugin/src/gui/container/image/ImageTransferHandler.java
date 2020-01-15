package gui.container.image;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import rslib.gui.container.image.ImageContainer;

/***
 * Represents an image transfer handler
 */
public class ImageTransferHandler extends TransferHandler {

    /** Link to container */
    private final ImageContainerPanel container;
    
    /** Correct file extensions */
    public static final List<String> IMAGE_FILE_EXTENSIONS;
    
    static {
        IMAGE_FILE_EXTENSIONS = new ArrayList<>();
        IMAGE_FILE_EXTENSIONS.add(".png");
        IMAGE_FILE_EXTENSIONS.add(".jpg");
        IMAGE_FILE_EXTENSIONS.add(".jpeg");
        IMAGE_FILE_EXTENSIONS.add(".gif");
        IMAGE_FILE_EXTENSIONS.add(".bmp");
    }

    /***
     * Constructor
     * @param container link to container 
     */
    public ImageTransferHandler(ImageContainerPanel container) {
        if (container == null) {
            throw new IllegalArgumentException("ImageTransferHandler: "
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
            if (flavor.equals(DataFlavor.imageFlavor) ||
                    flavor.equals(DataFlavor.javaFileListFlavor)) {
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

        if (support.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            try {

                BufferedImage image = (BufferedImage) support.
                        getTransferable().getTransferData(
                        DataFlavor.imageFlavor);

                processImage(image);
                return true;
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
                File imageFile = (File) files.get(0);
                processImage(imageFile);
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
     * Checks if file can be processed as a image file
     * @param file file
     * @return whether it can
     */
    public static boolean checkImageFile(File file) {
        String filename = file.getName();
        for (String extension : IMAGE_FILE_EXTENSIONS) {
            if (filename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /***
     * Checks if image can be processed
     * @param image image
     * @return whether it can
     */
    public static boolean checkImage(BufferedImage image) {
        return image.getWidth() <= ImageContainer.IMAGE_MAXIMUM_WIDTH && 
                image.getHeight() <= ImageContainer.IMAGE_MAXIMUM_HEIGHT;
    }
    
    /***
     * Reads image from file
     * @param parentComponent where to show a message in a error case
     * @param file file
     * @return read image
     */
    public static BufferedImage readImageFile(Component parentComponent, 
            File file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(parentComponent, 
                        "Error while reading the image!",
                        "Error", JOptionPane.ERROR_MESSAGE);
        }
        return image;
    }
    
    /***
     * Processes DND image
     * @param image DND image
     */
    private void processImage(BufferedImage image) {
        if (! checkImage(image)) {
            JOptionPane.showMessageDialog(container, 
                    "Image width and height can not be more than 1024 pixels"
                            + " each other!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        container.handleImage(image);
    }
    
    /***
     * Processes DND image file
     * @param image DND image
     */
    private void processImage(File file) {
        if (! checkImageFile(file)) {
            String message = "Only ";
            message += IMAGE_FILE_EXTENSIONS.get(0);
            for (int i = 1; i < IMAGE_FILE_EXTENSIONS.size(); ++ i) {
                message += ", " + IMAGE_FILE_EXTENSIONS.get(i);
            }
            message += " files are required!";
            JOptionPane.showMessageDialog(container, 
                        message, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        BufferedImage image = readImageFile(container, file);
        if (image != null) {
            processImage(image);
        }
    }
}