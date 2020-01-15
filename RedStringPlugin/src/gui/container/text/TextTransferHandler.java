package gui.container.text;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import rslib.gui.container.text.TextContainer;

/***
 * Represents a text transfer handler
 */
public class TextTransferHandler extends TransferHandler {

    /** Text container */
    private TextContainerPanel container;
    
    /** Maximum text file size in bytes, 3 MB */
    public static int MAX_FILE_SIZE = 3*1024*1024;
    
    /** Correct file extensions */
    public static final List<String> TEXT_FILE_EXTENSIONS;
    
    static {
        TEXT_FILE_EXTENSIONS = new ArrayList<>();
        TEXT_FILE_EXTENSIONS.add(".txt");
        TEXT_FILE_EXTENSIONS.add(".cfg");
        TEXT_FILE_EXTENSIONS.add(".java");
        TEXT_FILE_EXTENSIONS.add(".c");
        TEXT_FILE_EXTENSIONS.add(".h");
        TEXT_FILE_EXTENSIONS.add(".cc");
        TEXT_FILE_EXTENSIONS.add(".hh");
        TEXT_FILE_EXTENSIONS.add(".cpp");
        TEXT_FILE_EXTENSIONS.add(".hpp");
        TEXT_FILE_EXTENSIONS.add(".ini");
        TEXT_FILE_EXTENSIONS.add(".py");
        TEXT_FILE_EXTENSIONS.add(".css");
        TEXT_FILE_EXTENSIONS.add(".js");
        TEXT_FILE_EXTENSIONS.add(".jsp");
        TEXT_FILE_EXTENSIONS.add(".php");
        TEXT_FILE_EXTENSIONS.add(".pom");
        TEXT_FILE_EXTENSIONS.add(".bat");
        TEXT_FILE_EXTENSIONS.add(".sh");
        TEXT_FILE_EXTENSIONS.add(".xml");
    }

    public TextTransferHandler(TextContainerPanel container) {
        if (container == null) {
            throw new IllegalArgumentException("TextTranferHandler: "
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

        // Get the data
        Transferable t = support.getTransferable();
        java.util.List files;
        // Checking
        try {
            files = (java.util.List)t.getTransferData(DataFlavor.javaFileListFlavor);
        }
        catch (UnsupportedFlavorException | IOException e) {
            return false;
        }
        if (files.size() != 1) {
            return false;
        }
        processFile((File) files.get(0));
        return true;
    }
    
    /***
     * Checks if file can be processed as a text file
     * @param file file
     * @return whether it can
     */
    public static boolean checkTextFile(File file) {
        if (file.length() > MAX_FILE_SIZE) {
            return false;
        }
        String filename = file.getName();
        for (String extension : TEXT_FILE_EXTENSIONS) {
            if (filename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /***
     * Reads text from file
     * @param parentComponent where to show a message in a error case
     * @param file file
     * @return read text
     */
    public static String readTextFile(Component parentComponent, File file) {
        BufferedReader br = null;
        String text = "";
        try {
            String line;
            br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                text += line + "\n";
            }
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(parentComponent, "Error while"
                    + "reading text!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e1) {
                // ????????
            }
        }
        return text;
    }
    
    /***
     * Processes DND file
     * @param file DND file
     */
    private void processFile(File file) {
        if (! checkTextFile(file)) {
            if (file.length() > MAX_FILE_SIZE) {
                JOptionPane.showMessageDialog(container, 
                        "Text files more than 3 MB are not allowed!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            else {
                String message = "Only ";
                message += TEXT_FILE_EXTENSIONS.get(0);
                for (int i = 1; i < TEXT_FILE_EXTENSIONS.size(); ++ i) {
                    message += ", " + TEXT_FILE_EXTENSIONS.get(i);
                }
                message += " files are required!";
                JOptionPane.showMessageDialog(container, 
                            message, "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        String text = readTextFile(container, file);
        if (text != null && text.length() > TextContainer.TEXT_MAXIMUM_SIZE) {
            JOptionPane.showMessageDialog(container, 
                        "Text with more than 100,000 symbols is not allowed!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (text != null) {
            container.handleTextFromFile(text);
        }
    }
}
