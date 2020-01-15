package rslib.commands.board;

import rslib.cs.client.user.UserClient;
import rslib.cs.common.Status;
import rslib.cs.protocol.events.board.board.ClearBoardEvent;
import rslib.cs.protocol.events.board.board.SetBoardContentEvent;
import rslib.commands.Command;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.container.ExternalizableContainer;

import java.util.concurrent.CopyOnWriteArrayList;

/***
 * Represents clear board command
 */
public class ClearBoardCommand extends Command {

    /** Saved containers */
    private CopyOnWriteArrayList<ExternalizableContainer> serializableContainers;

    /***
     * Constructor
     * @param board link to board
     */
    public ClearBoardCommand(InteractiveBoard board) {
        super(board);
        serializableContainers = board.toExternalizable().getContainers();
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        InteractiveBoard board = getBoard();
        if (userClient.checkBoardRights(Status.LOBBY_ROOT, board.getComponentOwner())) {
            checkBlocked(userClient, board);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ClearBoardEvent(hash));
        }
        else {
            throw new IllegalStateException("You have no rights to clear board!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        InteractiveBoard board = getBoard();
        if (userClient.checkBoardRights(Status.LOBBY_ROOT, board.getComponentOwner())) {
            checkBlocked(userClient, board);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new SetBoardContentEvent(hash, serializableContainers));
        }
        else {
            throw new IllegalStateException("You have no rights to set board content!");
        }
    }
}
