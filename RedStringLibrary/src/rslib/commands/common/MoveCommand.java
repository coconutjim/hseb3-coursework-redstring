package rslib.commands.common;

import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.board.common.MoveEvent;
import rslib.gui.BasicComponent;
import rslib.gui.board.InteractiveBoard;

/***
 * Represents a move command
 */
public class MoveCommand extends ComponentCommand {

    /** New left coordinate */
    private int newLeft;

    /** New top coordinate */
    private int newTop;

    /** Old left coordinate */
    private int oldLeft;

    /** Old top coordinate */
    private int oldTop;

    /** If is needed to unblock the component after moving */
    private boolean unblock;

    /***
     * Constructor
     * @param board link to board
     * @param component target component
     * @param newLeft new left coordinate
     * @param newTop new top coordinate
     * @param unblock if is need to unblock
     */
    public MoveCommand(InteractiveBoard board, BasicComponent component, int newLeft, int newTop, boolean unblock) {
        super(board, component);
        oldLeft = component.getComponentLeft();
        oldTop = component.getComponentTop();
        this.newLeft = newLeft;
        this.newTop = newTop;
        this.unblock = unblock;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(component.getComponentStatus(), component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new MoveEvent(hash, id, newLeft, newTop, unblock));
        }
        else {
            throw new IllegalStateException("You have no rights to move this component!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(component.getComponentStatus(), component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new MoveEvent(hash, id, oldLeft, oldTop, false));
        }
        else {
            throw new IllegalStateException("You have no rights to move this component!");
        }
    }
}
