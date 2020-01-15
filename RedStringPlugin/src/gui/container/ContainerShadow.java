package gui.container;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Represents a container shadow to support moving and resizing
 */
public class ContainerShadow extends JPanel {
    
    /** Link to container */
    private ContainerPanel container;

    /***
     * Constructor
     * @param container link to container 
     */
    public ContainerShadow(ContainerPanel container) {
        if (container == null) {
            throw new IllegalArgumentException("ContainerShadow:"
                    + "container is null!");
        }
        this.container = container;
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        setBackground(ContainerPanel.getShadowColor());
        setOpaque(! container.isComplexPainting());
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (container.isComplexPainting()) {
            drawComponent((Graphics2D) g);
        }
    }
    
    /***
     * Draw complex shadow style
     * @param graphics link to graphics
     */
    private void drawComponent(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			RenderingHints.VALUE_ANTIALIAS_ON);       
        graphics.setColor(ContainerPanel.getShadowColor());
        int arc = ContainerPanel.getArc();
        graphics.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
    }  
    
    public void setComplexPainting(boolean complexPainting) {
        setOpaque(! complexPainting);
        repaint();
    }
}
