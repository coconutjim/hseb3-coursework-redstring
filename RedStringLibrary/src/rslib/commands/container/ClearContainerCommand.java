package rslib.commands.container;

import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.board.container.ClearContainerEvent;
import rslib.cs.protocol.events.board.container.SetContainerContentEvent;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.container.BoardContainer;
import rslib.gui.container.ExternalizableContainer;

/***
 * Represents clear container command
 */
public class ClearContainerCommand extends ContainerCommand {

    /** Saved data */
    private ExternalizableContainer serializableContainer;

    /***
     * Constructor (from origin)
     * @param container container for deleting
     */
    public ClearContainerCommand(InteractiveBoard board, BoardContainer container) {
        super(board, container);
        serializableContainer = container.toExternalizable();
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        InteractiveBoard board = getBoard();
        BoardContainer container = getContainer();
        if (userClient.checkBoardRights(board.getComponentStatus(), board.getComponentOwner())) {
            checkBlocked(userClient, container);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ClearContainerEvent(hash, id));
        }
        else {
            throw new IllegalStateException("You have no rights to clear this container!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        InteractiveBoard board = getBoard();
        BoardContainer container = getContainer();
        if (userClient.checkBoardRights(board.getComponentStatus(), board.getComponentOwner())) {
            checkBlocked(userClient, container);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new SetContainerContentEvent(hash, id, serializableContainer));
        }
    }
}
