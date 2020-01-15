package gui.container;

import gui.board.animation.Animatable;
import gui.board.animation.Animation;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSliderUI;

/***
 * Represents a JSlider with container customization
 */
public abstract class ControlSlider extends JSlider 
        implements Animatable {

    /** Link to container */
    protected ContainerPanel container;
    
    /** Stable height */
    private int stableHeight;
    
    /** Animations */
    private List<Animation> animations;
    
    /***
     * Constructor
     * @param container link to container
     * @param min min value
     * @param max max value
     * @param value 
     */
    public ControlSlider(ContainerPanel container) {
        if (container == null) {
            throw new IllegalArgumentException("ControlSlider:"
                    + "container is null!");
        }
        this.container = container;
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        animations = new CopyOnWriteArrayList<>();
        //setFocusPainted(false);
        setUI(new MySliderUI(this, false));
        stableHeight = getHeight();
    }
    
    /***
     * Changes appearance
     * @param varibale if slider value can be changed
     */
    public void changeAppearance(final boolean varibale) {
         SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setUI(new MySliderUI(ControlSlider.this, varibale));
            }
         });
    }
    
    /***
     * Customizing appearance
     */
    class MySliderUI extends BasicSliderUI {
        
        /** If slider value can be changed */
        private final boolean variable;

        /***
         * Constructor
         * @param b slider
         * @param variable if slider value can be changed
         */
        public MySliderUI(JSlider b, boolean variable) {
            super(b);
            this.variable = variable;
        }

        @Override
        protected Color getShadowColor() {
            return container.getForeground();
        }

        @Override
        protected Color getHighlightColor() {
            return container.getForeground();
        }

        @Override
        protected TrackListener createTrackListener(JSlider slider) {
            return new TrackListener() {
                @Override 
                public void mousePressed(MouseEvent e) {
                    if (variable) {
                        super.mousePressed(e);
                    }
                }
           
            };
        }
       
        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle rect = trackRect;
            g2d.setColor(container.getForeground());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(rect.x, (rect.y + rect.height) / 2, 
                    rect.x + rect.width, (rect.y + rect.height) / 2);
        }
        
        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle rect = thumbRect;
            g2d.setColor(container.getForeground());
            g2d.fillOval(rect.x, rect.y, rect.width, rect.height);
        }

        @Override
        protected void paintMajorTickForVertSlider(Graphics g, Rectangle tickBounds, int y) {
            g.setColor(container.getForeground());
            super.paintMajorTickForVertSlider(g, tickBounds, y); 
        }

        @Override
        protected void paintMinorTickForVertSlider(Graphics g, Rectangle tickBounds, int y) {
            g.setColor(container.getForeground());
            super.paintMinorTickForVertSlider(g, tickBounds, y); 
        }

        @Override
        protected void paintMajorTickForHorizSlider(Graphics g, Rectangle tickBounds, int x) {
            g.setColor(container.getForeground());
            super.paintMajorTickForHorizSlider(g, tickBounds, x); 
        }

        @Override
        protected void paintMinorTickForHorizSlider(Graphics g, Rectangle tickBounds, int x) {
            super.paintMinorTickForHorizSlider(g, tickBounds, x); //To change body of generated methods, choose Tools | Templates.
        }
          
        @Override
        public void paint(Graphics g, JComponent c) {
            recalculateIfInsetsChanged();
            recalculateIfOrientationChanged();
            Rectangle clip = g.getClipBounds();

            if (slider.getPaintTrack() && clip.intersects(trackRect)) {
                paintTrack(g);
            }
            if (slider.getPaintTicks() && clip.intersects(tickRect)) {
                paintTicks(g);
            }
            if (slider.getPaintLabels() && clip.intersects(labelRect)) {
                paintLabels(g);
            }
            if (clip.intersects(thumbRect)) {
                slider.setBackground(container.getForeground());
                paintThumb(g);
                slider.setBackground(container.getForeground());
            }
        }
    }
    
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(container.getFreeWidth(), 
                getSliderHeight());
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(container.getFreeWidth(), 
                getSliderHeight());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(container.getFreeWidth(), 
                getSliderHeight());
    }
    
    protected abstract int getSliderHeight();
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			RenderingHints.VALUE_ANTIALIAS_ON);
        for (Animation animation : animations) {
            animation.draw(g2d);
        }
    }

    @Override
    public void addAnimation(Animation animation) {
        animations.add(animation);
    }

    @Override
    public void removeAnimation(Animation animation) {
        animations.remove(animation);
    }
}
