package rslib.commands.board;

import rslib.cs.client.user.UserClient;
import rslib.cs.common.Status;
import rslib.cs.protocol.events.board.board.ChangeGeneralOpaqueEvent;
import rslib.commands.Command;
import rslib.gui.board.InteractiveBoard;

/***
 * Represents changing general container opaque command
 */
public class ChangeGeneralOpaqueCommand extends Command {

    /** Old opaque */
    private boolean oldOpaque;

    /** New opaque */
    private boolean newOpaque;

    /***
     * Constructor (from origin)
     * @param board link to board
     * @param newOpaque new opaque
     */
    public ChangeGeneralOpaqueCommand(InteractiveBoard board, boolean newOpaque) {
        super(board);
        oldOpaque = board.isGeneralContainerOpaque();
        this.newOpaque = newOpaque;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        InteractiveBoard board = getBoard();
        if (userClient.checkBoardRights(Status.LOBBY_ROOT, board.getComponentOwner())) {
            checkBlocked(userClient, board);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeGeneralOpaqueEvent(hash, newOpaque));
        }
        else {
            throw new IllegalStateException("You have no rights to change general container opaque!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        InteractiveBoard board = getBoard();
        if (userClient.checkBoardRights(Status.LOBBY_ROOT, board.getComponentOwner())) {
            checkBlocked(userClient, board);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeGeneralOpaqueEvent(hash, oldOpaque));
        }
        else {
            throw new IllegalStateException("You have no rights to change general container opaque!");
        }
    }
}
