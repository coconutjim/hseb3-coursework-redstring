package rslib.commands.board;

import rslib.cs.client.user.UserClient;
import rslib.cs.common.Status;
import rslib.cs.protocol.events.board.board.ChangeGeneralFontEvent;
import rslib.commands.Command;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.style.FontModel;

/***
 * Represents changing general container font command
 */
public class ChangeGeneralFontCommand extends Command {

    /** Old font */
    private FontModel oldFont;

    /** New font */
    private FontModel newFont;

    /***
     * Constructor (from origin)
     * @param board link to board
     * @param newFont new font
     */
    public ChangeGeneralFontCommand(InteractiveBoard board, FontModel newFont) {
        super(board);
        if (newFont == null) {
            throw new IllegalArgumentException("ChangeGeneralFontCommand: newFont is null!");
        }
        oldFont = board.getGeneralContainerFont();
        this.newFont = newFont;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        InteractiveBoard board = getBoard();
        if (userClient.checkBoardRights(Status.LOBBY_ROOT, board.getComponentOwner())) {
            checkBlocked(userClient, board);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeGeneralFontEvent(hash, newFont));
        }
        else {
            throw new IllegalStateException("You have no rights to change general container font!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        InteractiveBoard board = getBoard();
        if (userClient.checkBoardRights(Status.LOBBY_ROOT, board.getComponentOwner())) {
            checkBlocked(userClient, board);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeGeneralFontEvent(hash, oldFont));
        }
        else {
            throw new IllegalStateException("You have no rights to change general container font!");
        }
    }
}
