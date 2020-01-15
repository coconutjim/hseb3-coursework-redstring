package gui.container.file;

import gui.board.BoardPanel;
import gui.container.ContainerIconButton;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import rslib.commands.common.ChangeBlockCommand;
import rslib.gui.container.file.FileModel;

/***
 * Represents a download file content button
 */
public class DownloadButton extends ContainerIconButton {
    
    /** Link to file panel */
    private final FilePanel filePanel;
    
    /** File chooser */
    private final JFileChooser fc;
    
    /** Icon paths */
    private final List<GeneralPath> downloadIcon;

    /***
     * Constructor
     * @param container link to container
     * @param filePanel link to file panel 
     */
    public DownloadButton(BoardPanel board, 
            FileContainerPanel container, FilePanel filePanel) {
        super(board, container);
        if (filePanel == null) {
            throw new IllegalArgumentException("DownloadJButton: "
                    + "filePanel is null!");
        }
        this.filePanel = filePanel;
        downloadIcon = new ArrayList<>();
        fc = new JFileChooser();
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (filePanel.getFile() == null) {
                    JOptionPane.showMessageDialog(container, 
                            "Nothing to download!", "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (commandFacade.doCommand(
                            new ChangeBlockCommand(board, container, true), false)) {
                    int value = fc.showSaveDialog(container);
                    if (value != JFileChooser.APPROVE_OPTION) {
                        commandFacade.doCommand(
                            new ChangeBlockCommand(board, container, false), false);
                    }
                    else {
                        FileModel source = filePanel.getFile();
                        File destination = fc.getSelectedFile();
                        
                        String filename = source.getFilename();
                        String extension = "";
                        int i = filename.lastIndexOf('.');
                        if (i > 0) {
                            extension = filename.substring(i);
                        }
                        if (! extension.equals("")) {
                            if (! destination.getName().endsWith(extension)) {
                                destination = new File(
                                        destination.getAbsolutePath() + extension);
                            }
                        }
                        try {
                            copyData(source, destination);
                        }
                        catch(IOException ex) {
                            JOptionPane.showMessageDialog(container, 
                                    "Error while saving file", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                        finally {
                            commandFacade.doCommand(
                                new ChangeBlockCommand(board, container, false), false);
                        }
                    }
                }
            }
        });
        createPaths();
        createIcon(container.getForeground(), container.isComponentOpaque());
    }
    
    /***
     * Copies file
     * @param source source file
     * @param destination destination file
     */
    private void copyData(FileModel source, File destination) 
            throws IOException {
        byte[] bytes = source.getData();
        Path path = destination.toPath();
        Files.write(path, bytes);
    }

    @Override
    public void createIcon(Color color, boolean opaque) {
        BufferedImage ic = createTransparentIcon();
        if (! opaque) {
            icon = ic;
            return;
        }
        Graphics2D g2d = ic.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);    
        g2d.setColor(color);
        for (GeneralPath path : downloadIcon) {
            g2d.fill(path);
            g2d.draw(path);
        }     
        icon = ic;
    }
    
    /***
     * Creates paths for icon
     */
    private void createPaths() {
        int[] arrowX = { 10, 13, 13, 16, 18, 18, 19, 19, 13, 10, 4, 4, 5, 5, 7, 10 };
        int[] arrowY = { 0, 0, 13, 10, 10, 11, 11, 12, 18, 18, 12, 11, 11, 10, 10, 13 };
        GeneralPath arrow = createPath(arrowX, arrowY);

        int[] blockX = { 2, 21, 21, 2 };
        int[] blockY = { 21, 21, 23, 23 };
        GeneralPath block = createPath(blockX, blockY);
        
        downloadIcon.add(arrow);
        downloadIcon.add(block);
    }
}
