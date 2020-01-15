package rslib.commands.container;

import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.board.container.ChangeContainerLayerEvent;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.container.BoardContainer;

/***
 * Represents setting container to front command
 */
public class ChangeContainerLayerCommand extends ContainerCommand {

    /** Old layer position */
    private int oldLayer;

    /** New layer position */
    private int newLayer;

    /***
     * Constructor
     * @param board link to board
     * @param container link to container
     * @param newLayer new layer position
     */
    public ChangeContainerLayerCommand(InteractiveBoard board, BoardContainer container, int newLayer) {
        super(board, container);
        oldLayer = board.getLayerPosition(container);
        this.newLayer = newLayer;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        BoardContainer container = getContainer();
        if (userClient.checkBoardRights(container.getComponentStatus(), container.getComponentOwner())) {
            checkBlocked(userClient, container);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeContainerLayerEvent(hash, id, newLayer));
        }
        else {
            throw new IllegalStateException("You have no rights to change container layer position!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        BoardContainer container = getContainer();
        if (userClient.checkBoardRights(container.getComponentStatus(), container.getComponentOwner())) {
            checkBlocked(userClient, container);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeContainerLayerEvent(hash, id, oldLayer));
        }
        else {
            throw new IllegalStateException("You have no rights to change container layer position!");
        }
    }
}
