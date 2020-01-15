package rslib.commands.board;

import rslib.cs.client.user.UserClient;
import rslib.cs.common.Status;
import rslib.cs.protocol.events.board.board.ChangeGeneralColorEvent;
import rslib.commands.Command;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.style.ColorModel;

/***
 * Represents changing general container color command
 */
public class ChangeGeneralColorCommand extends Command {

    /** Old foreground color */
    private ColorModel oldForeground;

    /** Old background color */
    private ColorModel oldBackground;

    /** New foreground color */
    private ColorModel newForeground;

    /** New background color */
    private ColorModel newBackground;


    /***
     * Constructor (from origin)
     * @param board link to board
     * @param newForeground new foreground color
     * @param newBackground new background color
     */
    public ChangeGeneralColorCommand(InteractiveBoard board, ColorModel newForeground, ColorModel newBackground) {
        super(board);
        if (newForeground == null && newBackground == null) {
            throw new IllegalArgumentException("ChangeGeneralColorCommand: both colors are null!");
        }
        this.newForeground = newForeground;
        oldForeground = board.getGeneralContainerForeground();
        this.newBackground = newBackground;
        oldBackground = board.getGeneralContainerBackground();
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        InteractiveBoard board = getBoard();
        if (userClient.checkBoardRights(Status.LOBBY_ROOT, board.getComponentOwner())) {
            checkBlocked(userClient, board);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeGeneralColorEvent(hash, newForeground, newBackground));
        }
        else {
            throw new IllegalStateException("You have no rights to change general container color!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        InteractiveBoard board = getBoard();
        if (userClient.checkBoardRights(Status.LOBBY_ROOT, board.getComponentOwner())) {
            checkBlocked(userClient, board);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeGeneralColorEvent(hash,
                    newForeground == null? null : oldForeground,
                    newBackground == null? null : oldBackground));
        }
        else {
            throw new IllegalStateException("You have no rights to change general container color!");
        }
    }
}
