package gui.container;

import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicScrollBarUI;

/***
 * Represents a JScrollPane with container customization
 */
public class ContentScroll extends JScrollPane {
    
    /** Link to text container */
    private ContainerPanel container;

    /***
     * Constructor
     * @param container container
     * @param text text area
     */
    public ContentScroll(ContainerPanel container, JComponent component) {
        super(component);
        if (container == null) {
            throw new IllegalArgumentException("JTextScroll: container is null!");
        }
        this.container = container;
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        setOpaque(false);
        setBackground(container.getBackground());
        boolean opaque = container.isComponentOpaque();
        setBorder(opaque ? BorderFactory.createLineBorder(
                container.getForeground(), 1) : null);
        setVerticalScrollBarPolicy(opaque ? ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED :
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(opaque ? ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED :
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollBar vertical = getVerticalScrollBar();
        JScrollBar horizontal = getVerticalScrollBar();
        vertical.setOpaque(false);
        vertical.setUI(new MyScrollBarUI());
        horizontal.setOpaque(false);
        horizontal.setUI(new MyScrollBarUI());
        /*getVerticalScrollBar().setBorder(opaque ? BorderFactory.
                        createLineBorder(container.getForeground(), 1) : null);
        getHorizontalScrollBar().setBorder(opaque ? BorderFactory.
                        createLineBorder(container.getForeground(), 1) : null);*/
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(container.getFreeWidth(), container.getFreeHeight());
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(container.getFreeWidth(), container.getFreeHeight());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(container.getFreeWidth(), container.getFreeHeight());
    }
    
    /***
     * Changes appearance
     */
    public void changeAppearance() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                boolean opaque = container.isComponentOpaque();
                setVerticalScrollBarPolicy(opaque ? 
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED :
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                setHorizontalScrollBarPolicy(opaque ? 
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED :
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                setBorder(opaque ? BorderFactory.
                        createLineBorder(container.getForeground(), 1) : null);
                setBackground(container.getBackground());
                getVerticalScrollBar().setUI(new MyScrollBarUI());
                getHorizontalScrollBar().setUI(new MyScrollBarUI());
                /*getVerticalScrollBar().setBorder(opaque ? BorderFactory.
                        createLineBorder(container.getForeground(), 1) : null);
                getHorizontalScrollBar().setBorder(opaque ? BorderFactory.
                        createLineBorder(container.getForeground(), 1) : null);*/
            }
        });
    }
    
    /***
     * Customizing appearance
     */
    class MyScrollBarUI extends BasicScrollBarUI {
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
           return new BasicArrowButton(orientation, container.getBackground(),
           container.getForeground(), container.getForeground(),
           container.getForeground());
        }

        @Override
       protected JButton createIncreaseButton(int orientation) {
           return new BasicArrowButton(orientation, container.getBackground(),
           container.getForeground(), container.getForeground(),
           container.getForeground());
        }

        @Override
        protected void configureScrollBarColors() {
            /*thumbHighlightColor = container.getForeground();
            thumbLightShadowColor = container.getForeground();
            thumbDarkShadowColor = container.getForeground();
            thumbColor = container.getForeground();
            trackColor = container.getBackground();
            trackHighlightColor = container.getForeground();*/
            thumbHighlightColor = container.getForeground();
            thumbLightShadowColor = container.getForeground();
            thumbDarkShadowColor = container.getBackground();
            thumbColor = container.getBackground();
            trackColor = container.getBackground();
            trackHighlightColor = container.getForeground();
        }        
    }
}
