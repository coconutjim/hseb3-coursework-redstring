package gui.container.image;

import gui.board.BoardPanel;
import gui.container.ContainerPanel;
import gui.container.SaveEditButton;
import gui.parsing.Parsing;
import rslib.commands.container.image.ChangeImageCommand;

/***
 * Represents an image save edit button
 */
public class ImageSaveEditButton extends SaveEditButton {
    
    /** Link to paint panel */
    private PaintPanel paintPanel;
    
    /** Link to slider */
    private ImageScaleSlider slider;
    
    /** Old image */
    private BufferedImageModel oldImage;

    /***
     * Constructor
     * @param paintPanel link to panel
     * @param slider link to slider
     * @param board link to board
     * @param container link to container
     */
    public ImageSaveEditButton(PaintPanel paintPanel, ImageScaleSlider slider,
            BoardPanel board, 
            ContainerPanel container) {
        super(board, container);
        if (paintPanel == null) {
            throw new IllegalArgumentException("ImageSaveEditJButton: "
                    + "paintJPanel is null!");
        }
        if (slider == null) {
            throw new IllegalArgumentException("ImageSaveEditJButton: "
                    + "slider is null!");
        }
        this.paintPanel = paintPanel;
        this.slider = slider;
        oldImage = null;
    }

    @Override
    protected void editActions() {
        super.editActions();
        BufferedImageModel original = paintPanel.getImage();
        oldImage = original;
        /*if (original == null) {
            oldImage = original;
        }
        else {
            BufferedImageModel copy = new BufferedImageModel(
                    new BufferedImage(original.getImage().getWidth(),
                    original.getImage().getHeight(), BufferedImage.TYPE_INT_ARGB),
                    original.getScale());
            copy.getImage().getGraphics().drawImage(original.getImage(), 0, 0, null);
            oldImage = copy;
        }*/
        paintPanel.setMouseScrollListener(false);
        paintPanel.setPaintMouseListener(true);
        paintPanel.setPaintSettingsPopup(true);
        slider.changeAppearance(true);
    }

    @Override
    protected void saveActions() {
        super.saveActions();
        BufferedImageModel newImage = paintPanel.getImage();
        if (! (newImage == null && oldImage == null) 
            || (newImage != null && ! newImage.equals(oldImage))) { 
            paintPanel.setImage(oldImage);
            commandFacade.doCommand(new ChangeImageCommand(board, 
                    (ImageContainerPanel)container, 
                    Parsing.convertToImageModel(newImage)), true);
        }
        
        paintPanel.setPaintMouseListener(false);
        paintPanel.setPaintSettingsPopup(false);
        paintPanel.setMouseScrollListener(true);
        slider.changeAppearance(false);
        oldImage = null;
    } 
}