package gui.container.image;

import gui.board.MouseScrollListener;
import gui.board.animation.Animatable;
import gui.board.animation.Animation;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JPanel;
import javax.swing.JViewport;

/***
 * Represents a paint panel
 */
 public class PaintPanel extends JPanel implements Animatable {
    
    /** Link to container */
    private ImageContainerPanel container;
     
    /** Image */
    private BufferedImageModel image;
    
    /** Link to scroll listener */
    private MouseAdapter mouseScrollListener;
    
    /** Link to paint listener */
    private MouseAdapter paintMouseListener;
    
    /** Link to paint settings pop up */
    private MouseAdapter settingsPopup;
    
    /** Paint settings */
    private PaintSettings paintSettings;
    
    /** Animations */
    private final List<Animation> animations;

    /***
     * Constructor
     * @param container link to container
     * @param image image
     */
    public PaintPanel(ImageContainerPanel container,
            BufferedImageModel image) {
        if (container == null) {
            throw new IllegalArgumentException("PaintPanel: "
                    + "container is null!");
        }
        this.container = container;
        this.image = image;  
        paintSettings = new PaintSettings(1, Color.BLACK);
        animations = new CopyOnWriteArrayList<>();
        repaint();
    }
    
    /***
     * Initializes mouse listeners
     * @param viewport link to scroll viewport
     */
    public void initListeners(JViewport viewport) {
         if (viewport == null) {
            throw new IllegalArgumentException("PaintPanel: "
                    + "viewport is null!");
        }
        mouseScrollListener = 
                new MouseScrollListener(this, viewport);
        paintMouseListener = new PaintMouseListener(container, this);
        settingsPopup = new PaintSettingsPopupMenu(this).createPopUpMenu();
        setMouseScrollListener(true);
    }
        
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        if (image != null) {
            double delta = image.getScale() / 100.0;
            int imageWidth =  (int) Math.round(delta * 
                    image.getImage().getWidth());
            int imageHeight = (int) Math.round(delta * 
                    image.getImage().getHeight());
         
            int width = container.getScrollWidth();
            int height = container.getScrollHeight();
            int x = 0;
            int y = 0;
            if (width > imageWidth) {
                x = (width - imageWidth + 
                        container.getVScrollBarWidth() + 1) / 2;
            }
            if (height > imageHeight) {
                y = (height - imageHeight + 
                        container.getHScrollBarHeight() + 1) / 2;
            }
            if (imageWidth > width) {
                width = imageWidth;
            }
            if (imageHeight > height) {
                height = imageHeight;
            }
            setPreferredSize(new Dimension(width, height));
            g.drawImage(image.getImage(), x, y, imageWidth, imageHeight, null);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
            for (Animation animation : animations) {
                animation.draw(g2d);
            }
            revalidate();
        }
    }   
    
    /***
     * Calculates image offset
     * @return image offset
     */
    public Point calculateOffset() {
        if (image == null) {
            return new Point(0, 0);
        }
        //TODO: code repetition
        double delta = image.getScale() / 100.0;
        int imageWidth =  (int) Math.round(delta * 
                image.getImage().getWidth());
        int imageHeight = (int) Math.round(delta * 
                image.getImage().getHeight());
        
        int width = container.getScrollWidth();
        int height = container.getScrollHeight();
        int x = 0;
        int y = 0;
        if (width > imageWidth) {
            x = (width - imageWidth + 
                    container.getVScrollBarWidth() + 1) / 2;
        }
        if (height > imageHeight) {
            y = (height - imageHeight + 
                    container.getHScrollBarHeight() + 1) / 2;
        }
        return new Point(x, y);
    }

    public void setImage(BufferedImageModel image) {
        this.image = image;
        if (image != null) {
            setPreferredSize(new Dimension(image.getImage().getWidth(), 
                    image.getImage().getHeight()));
            setScale(image.getScale());
        }
        else {
            setPreferredSize(new Dimension(container.getScrollWidth(),
                container.getScrollHeight()));
        }
        revalidate();
        repaint();
    }

    public BufferedImageModel getImage() {
        return image;
    }

    public void setScale(int scale) {
        if (image != null) {
            image.setScale(scale);
            repaint();
        }
    }

    public int getScale() {
        if (image != null) {
            return image.getScale();
        }
        else {
            return 100;
        }
    }
    
    /***
     * Centers viewport on image
     */
    public void centerViewport() {
       container.centerViewport();
    }

    /***
     * Sets mouse scroll listener enabled
     * @param enabled enabled/disabled
     */
    public void setMouseScrollListener(boolean enabled) {
        if (enabled) {
            addMouseListener(mouseScrollListener);
            addMouseMotionListener(mouseScrollListener);
        }
        else {
            removeMouseListener(mouseScrollListener);
            removeMouseMotionListener(mouseScrollListener);
        }
    }
    
    /***
     * Sets mouse paint listener enabled
     * @param enabled enabled/disabled
     */
    public void setPaintMouseListener(boolean enabled) {
        if (enabled) {
            addMouseListener(paintMouseListener);
            addMouseMotionListener(paintMouseListener);
        }
        else {
            removeMouseListener(paintMouseListener);
            removeMouseMotionListener(paintMouseListener);
        }
    }
    
    /***
     * Sets paint settings pop up enabled
     * @param enabled enabled/disabled
     */
    public void setPaintSettingsPopup(boolean enabled) {
        if (enabled) {
            addMouseListener(settingsPopup);
            addMouseMotionListener(settingsPopup);
        }
        else {
            removeMouseListener(settingsPopup);
            removeMouseMotionListener(settingsPopup);
        }
    }

    public PaintSettings getPaintSettings() {
        return paintSettings;
    }

    public void setPaintSettings(PaintSettings paintSettings) {
        this.paintSettings = paintSettings;
    }

    /***
     * Sets an empty transparent image that fits container
     */
    public void setTransparentImage() {
        int width = container.getVScrollBarWidth();
        int height = container.getHScrollBarHeight();
        setImage(null);
        setPlainImage(new Color(0, 0, 0, 0), container.getScrollWidth() + width, 
                container.getScrollHeight() + height);
    }
    
    /***
     * Fills image with specified background color
     * @param color image color
     */
    public void fillImage(Color color) {
        int width;
        int height;
        if (image != null) {
            width = image.getImage().getWidth();
            height = image.getImage().getHeight();
        }
        else {
            setImage(null);
            width = container.getScrollWidth();
            height = container.getScrollHeight();
        }
        setPlainImage(color, width, height);
    }
    
    /***
     * Sets plain image
     * @param color image color
     * @param width image width
     * @param height image height
     */
    private void setPlainImage(Color color, int width, int height) {
        BufferedImage bi = new BufferedImage(width, 
                height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        container.setSliderScale(100);
        setImage(new BufferedImageModel(bi, 100, 0, 0));
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