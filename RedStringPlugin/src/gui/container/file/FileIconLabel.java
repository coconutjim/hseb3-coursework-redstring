package gui.container.file;

import gui.container.AnimatableLabel;
import gui.container.ContainerIconButton;
import static gui.container.ContainerIconButton.createTransparentIcon;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/***
 * Represents a file icon
 */
public class FileIconLabel extends AnimatableLabel {
    
    /** Link to panel */
    private final FilePanel panel;
    
     /** Dimension */
    public static final Dimension CONTAINER_LABEL_DIM = new Dimension(24, 24);
    
    /** Icon */
    private BufferedImage icon;
    
    /** Icon paths */
    private List<GeneralPath> fileIcon;
    
    /***
     * Constructor
     * @param panel link to panel 
     */
    public FileIconLabel(FilePanel panel) {
        if (panel == null) {
            throw new IllegalArgumentException("FileIconLabel: "
                    + "panel is null!");
        }
        this.panel = panel;
        fileIcon = new ArrayList<>();
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        setPreferredSize(CONTAINER_LABEL_DIM);
        setMinimumSize(CONTAINER_LABEL_DIM);
        setMaximumSize(CONTAINER_LABEL_DIM);
        createPaths();
        createIcon(panel.getForeground());
    }
    
    /***
     * Creates icon
     * @param color icon color 
     */
    public void createIcon(Color color) {
       BufferedImage ic = createTransparentIcon();
       if (color == null) {
            icon = ic;
            return;
        }
        Graphics2D g2d = ic.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        if (panel.getFile() != null) { 
            for (GeneralPath path : fileIcon) {
                g2d.fill(path);
                g2d.draw(path);
            }
        }     
        icon = ic; 
    }
    
    /***
     * Creates paths for icon
     */
    private void createPaths() {
        int[] topX = { 2, 2, 3, 8, 10, 11, 11, 21, 21 };
        int[] topY = { 6, 3, 2, 2, 4, 4, 5, 5, 6 };
        GeneralPath top = ContainerIconButton.createPath(topX, topY);

        int[] blockX = { 0, 23, 22, 1 };
        int[] blockY = { 8, 8, 21, 21 };
        GeneralPath block = ContainerIconButton.createPath(blockX, blockY);
        
        fileIcon.add(top);
        fileIcon.add(block);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(icon, 0, 0, null);
    }
}
