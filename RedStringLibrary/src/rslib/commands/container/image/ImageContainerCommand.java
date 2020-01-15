package rslib.commands.container.image;

import rslib.commands.container.ContainerCommand;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.container.BoardContainer;
import rslib.gui.container.image.ImageContainer;

/***
 * Represents a text container command
 */
public abstract class ImageContainerCommand extends ContainerCommand {

    /***
     * Constructor
     * @param board link to board
     * @param container target container
     */
    public ImageContainerCommand(InteractiveBoard board, BoardContainer container) {
        super(board, container);
    }

    /***
     * Gets image container by its id
     * @return image container
     * @throws IllegalStateException if container was not found
     */
    protected ImageContainer getImageContainer() throws IllegalStateException {
        try {
            return (ImageContainer) getContainer();
        }
        catch (ClassCastException e) {
            throw new IllegalStateException(getClass().getSimpleName() + ": container not found!");
        }
    }
}