package rslib.commands.board;

import rslib.cs.client.user.UserClient;
import rslib.cs.common.Status;
import rslib.cs.protocol.events.setup.SetBoardEvent;
import rslib.commands.Command;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.board.ExternalizableBoard;

/***
 * Represents a set board command
 */
public class SetBoardCommand extends Command {

    /** Old board */
    private ExternalizableBoard oldBoard;

    /** New board */
    private ExternalizableBoard newBoard;

    /***
     * Constructor
     * @param board link to board
     * @param newBoard new board
     */
    public SetBoardCommand(InteractiveBoard board, ExternalizableBoard newBoard) {
        super(board);
        if (newBoard == null) {
            throw new IllegalArgumentException("SetBoardCommand: newBoard is null!");
        }
        oldBoard = board.toExternalizable();
        this.newBoard = newBoard;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        InteractiveBoard board = getBoard();
        if (userClient.checkBoardRights(Status.LOBBY_ROOT, board.getComponentOwner())) {
            checkBlocked(userClient, board);
            userClient.addSetupEvent(new SetBoardEvent(newBoard));
        }
        else {
            throw new IllegalStateException("You have no rights to set board!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        InteractiveBoard board = getBoard();
        if (userClient.checkBoardRights(Status.LOBBY_ROOT, board.getComponentOwner())) {
            checkBlocked(userClient, board);
            userClient.addSetupEvent(new SetBoardEvent(oldBoard));
        }
        else {
            throw new IllegalStateException("You have no rights to set board!");
        }
    }
}
