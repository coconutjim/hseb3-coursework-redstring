package rslib.commands.container;

import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.board.container.AddContainerEvent;
import rslib.cs.protocol.events.board.container.DeleteContainerEvent;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.container.BoardContainer;

/***
 * Represents delete container command
 */
public class DeleteContainerCommand extends ContainerCommand {

    /** Container */
    private BoardContainer container;

    /***
     * Constructor
     * @param board link to board
     * @param container container for deleting
     */
    public DeleteContainerCommand(InteractiveBoard board, BoardContainer container) {
        super(board, container);
        this.container = container;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        InteractiveBoard board = getBoard();
        BoardContainer container = getContainer();
        if (userClient.checkBoardRights(board.getComponentStatus(), board.getComponentOwner())) {
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new DeleteContainerEvent(hash, id));
        }
        else {
            throw new IllegalStateException("You have no rights to delete container!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        InteractiveBoard board = getBoard();
        if (userClient.checkBoardRights(board.getComponentStatus(), board.getComponentOwner())) {
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new AddContainerEvent(hash, container.toExternalizable()));
        }
        else {
            throw new IllegalStateException("You have no rights to add container!");
        }
    }
}
