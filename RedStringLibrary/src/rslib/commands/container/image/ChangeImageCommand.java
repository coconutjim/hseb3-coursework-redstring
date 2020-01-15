package rslib.commands.container.image;

import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.board.container.image.ChangeImageEvent;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.container.image.ImageModel;
import rslib.gui.container.image.ImageContainer;

/***
 * Represents a change image command
 */
public class ChangeImageCommand extends ImageContainerCommand {

    /** Old image */
    private ImageModel oldImage;

    /** New image */
    private ImageModel newImage;

    /***
     * Constructor
     * @param board link to board
     * @param imageContainer image container
     * @param newImage new image
     */
    public ChangeImageCommand(InteractiveBoard board, ImageContainer imageContainer, ImageModel newImage) {
        super(board, imageContainer);
        oldImage = imageContainer.getImage();
        this.newImage = newImage;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        ImageContainer imageContainer = getImageContainer();
        if (userClient.checkBoardRights(imageContainer.getComponentStatus(), imageContainer.getComponentOwner())) {
            checkBlocked(userClient, imageContainer);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeImageEvent(hash, id, newImage));
        }
        else {
            throw new IllegalStateException("You have no rights to set image to this container!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        ImageContainer imageContainer = getImageContainer();
        if (userClient.checkBoardRights(imageContainer.getComponentStatus(), imageContainer.getComponentOwner())) {
            checkBlocked(userClient, imageContainer);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeImageEvent(hash, id, oldImage));
        }
        else {
            throw new IllegalStateException("You have no rights to set image to this container!");
        }
    }
}
