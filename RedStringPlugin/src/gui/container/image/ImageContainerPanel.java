package gui.container.image;

import gui.board.BoardPanel;
import gui.board.animation.AnimationManager;
import gui.board.animation.Geometry;
import gui.board.animation.PointAnimation;
import gui.container.ContainerPanel;
import gui.container.ContentScroll;
import gui.parsing.Parsing;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.JViewport;
import rslib.commands.container.image.ChangeImageCommand;
import rslib.gui.container.BoardContainer.ContainerType;
import rslib.gui.container.ExternalizableContainer;
import rslib.gui.container.image.ExternalizableImageContainer;
import rslib.gui.container.image.ImageContainer;
import rslib.gui.container.image.ImageModel;
import rslib.gui.style.ColorModel;
import rslib.gui.style.FontModel;

/***
 * Represents a container that can hold image
 */
public class ImageContainerPanel extends ContainerPanel 
        implements ImageContainer {
    
    /** Image content */
    private final PaintPanel paintPanel;
    
    /** Image scroll */
    private ContentScroll scroll;
    
    /** Slider */
    private final ImageScaleSlider slider;
    
    /** Edit save button */
    private ImageSaveEditButton saveEditButton;
    
    /***
     * Constructor
     * @param type container type
     * @param owner container owner
     * @param textId text container id
     * @param id container id
     * @param board link to board
     * @param origin origin point
     * @param image image
     */
    public ImageContainerPanel(ContainerType type, 
            String owner, int id, BoardPanel board,
            Point origin, BufferedImageModel image) {
        this(type, owner, id, board, origin);
        paintPanel.setImage(image);
    }
    
    /***
     * Constructor
     * @param type container type
     * @param owner container owner
     * @param textId text container id
     * @param id container id
     * @param board link to board
     * @param origin origin point
     */
    public ImageContainerPanel(ContainerType type, 
            String owner, int id, BoardPanel board,
            Point origin) {
        super(type, owner, id, board, origin);
        paintPanel = new PaintPanel(this, null);
        slider = new ImageScaleSlider(this, paintPanel);
        initComponents();
    }
    
    /***
     * Constructor
     * @param board link to board
     * @param eic saved data
     */
    public ImageContainerPanel(BoardPanel board, ExternalizableImageContainer
            eic) {
        super(board, eic);
        paintPanel = new PaintPanel(this, 
                Parsing.createImage(eic.getImage()));
        slider = new ImageScaleSlider(this, paintPanel);
        initComponents();
    }
    
    /***
     * Initializes GUI components and features
     */
    private void initComponents() {  
        scroll = new ContentScroll(this, paintPanel);
        paintPanel.setOpaque(true);
        paintPanel.setBackground(getBackground());
        paintPanel.repaint();
        scroll.setAlignmentX(CENTER_ALIGNMENT);
        add(scroll, "wrap");  
        scroll.changeAppearance();
        add(Box.createVerticalStrut(vertical_gap));       
        slider.changeAppearance(false);
        slider.setVisible(opaque);
        slider.removeChangeListener(null);
        setSliderScale(paintPanel.getScale());
        add(slider);
        add(Box.createVerticalStrut(vertical_gap));
        
        saveEditButton = new ImageSaveEditButton(paintPanel, 
                slider, board, this);
        saveEditButton.setAlignmentX(CENTER_ALIGNMENT);
        saveEditButton.setEnabled(opaque);
        add(saveEditButton, "wrap");
        add(Box.createVerticalStrut(vertical_gap));
        initFeatures();
    }
    
    /***
     * Sets image container features
     */
    private void initFeatures() {
        paintPanel.setTransferHandler(new ImageTransferHandler(this));
        paintPanel.initListeners(scroll.getViewport());
    }
    
    @Override
    public int getFreeHeight() {
        return super.getFreeHeight() - slider.getPreferredSize().height - 
                saveEditButton.getHeight() - 3 * vertical_gap;
    }
    
    /***
     * Handles DND image
     * @param image DND image
     */
    public void handleImage(BufferedImage image) {
        board.getCommandFacade().doCommand(new 
            ChangeImageCommand(board, this, 
                    Parsing.convertToImageModel(new
                    BufferedImageModel(image, 100, 0, 0))), true);
    }
 
    @Override
    public void setComponentBackground(ColorModel cm) {
        super.setComponentBackground(cm); 
        scroll.changeAppearance();
        slider.changeAppearance(! saveEditButton.isState());
        paintPanel.setBackground(getBackground());
    }
    
    @Override
    public void setComponentOpaque(boolean bln) {
        super.setComponentOpaque(bln);
        slider.setVisible(bln);
        slider.changeAppearance(! saveEditButton.isState());
        scroll.changeAppearance();
        saveEditButton.setEnabled(bln);
        saveEditButton.createIcon(getForeground(), bln);
        revalidate();
    }

    @Override
    public void setComponentForeground(ColorModel cm) {
        super.setComponentForeground(cm); 
        scroll.changeAppearance();
        slider.changeAppearance(! saveEditButton.isState());
        saveEditButton.createIcon(getForeground(), isComponentOpaque());
    }

    @Override
    public void setComponentFont(FontModel fm) {
        super.setComponentFont(fm); 
        revalidate();
    }

    @Override
    public void setImage(ImageModel im) {
        paintPanel.setImage(Parsing.createImage(im));
        setSliderScale(paintPanel.getScale());
    }
    
    //TODO: remove
    public void settext(String text) {
        nameLabel.setText(text);
    }

    @Override
    public ImageModel getImage() {
        return Parsing.convertToImageModel(paintPanel.getImage());
    }
   
    @Override
    public void clearContainer() {
        paintPanel.setImage(null);
    }

    @Override
    public void setContent(ExternalizableContainer ec) {
        setImage(((ExternalizableImageContainer) ec).getImage());
    }
    
    @Override
    public ExternalizableContainer toExternalizable() {
        return new ExternalizableImageContainer(getComponentOwner(), 
                getComponentStatus(), getComponentLeft(), 
                getComponentTop(), getComponentWidth(), getComponentHeight(),
                getComponentMinimumWidth(), getComponentMinimumHeight(),
                getComponentMaximumWidth(), getComponentMaximumHeight(),
                getComponentName(), getComponentId(),
                getComponentFont(), isComponentOpaque(),
                getComponentForeground(), getComponentBackground(),
                getLayer(), getType(), isBlocked(), getBlockOwner(), getImage());
    }
    
    @Override
    public String getToolTipText() {
        String text = super.getToolTipText();
        text += "<html><br> Correct file extensiions:";
        for (String extension : ImageTransferHandler.IMAGE_FILE_EXTENSIONS) {
            text += "<br>" + extension;
        }
        text += "</html>";
        return text;
    }   
    
    public int getScrollWidth() {
        return scroll.getWidth() - 
                scroll.getVerticalScrollBar().getWidth() - 2;
    }
    
    public int getScrollHeight() {
        return scroll.getHeight() - 
                scroll.getHorizontalScrollBar().getHeight() - 2;
    }
    
    public int getVScrollBarWidth() {
        return scroll.getVerticalScrollBar().getWidth();
    }
    
    public int getHScrollBarHeight() {
        return scroll.getHorizontalScrollBar().getHeight();
    }
    
    public void setSliderScale(int scale) {
        slider.setValue(scale);
    }
    
    public int getSliderScale() {
        return slider.getValue();
    }
    
    /***
     * Centers viewport on image
     */
    public void centerViewport() {
        JViewport viewport = scroll.getViewport();
        Dimension viewSize = viewport.getSize();
        Dimension panelSize = paintPanel.getSize();
        viewport.setViewPosition(new Point((panelSize.width - viewSize.width) / 2,
                (panelSize.height - viewSize.height) / 2));
    }
    
    @Override
    public void point(AnimationManager am, int i, int i1, Color color) {
        if (Geometry.hasPoint(blockLabel, i, i1)) {
            am.addAnimation(
                    new PointAnimation(blockLabel, i - blockLabel.getX(), 
                    i1 - blockLabel.getY(), color));
            return;
        }
        if (Geometry.hasPoint(nameLabel, i, i1)) {
            am.addAnimation(
                    new PointAnimation(nameLabel, i - nameLabel.getX(), 
                    i1 - nameLabel.getY(), color));
            return;
        }
        if (Geometry.hasPoint(scroll, i, i1)) {
            Rectangle bounds = scroll.getViewport().getViewRect();
            am.addAnimation(
                    new PointAnimation(paintPanel, 
                            i - scroll.getX() + bounds.x, 
                    i1 - scroll.getY() + bounds.y, color));
            return;
        }
        if (Geometry.hasPoint(slider, i, i1)) {
            am.addAnimation(
                    new PointAnimation(slider, i - slider.getX(), 
                    i1 - slider.getY(), color));
            return;
        }
        if (Geometry.hasPoint(saveEditButton, i, i1)) {
            am.addAnimation(
                    new PointAnimation(saveEditButton, i - saveEditButton.getX(), 
                    i1 - saveEditButton.getY(), color));
            return;
        }
        am.addAnimation(new PointAnimation(this, i, i1, color));
    }
}