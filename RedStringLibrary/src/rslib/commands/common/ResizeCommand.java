package rslib.commands.common;

import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.board.common.ResizeEvent;
import rslib.gui.BasicComponent;
import rslib.gui.board.InteractiveBoard;

/***
 * Represents resize command
 */
public class ResizeCommand extends ComponentCommand {

    /** New left */
    private int newLeft;

    /** New top */
    private int newTop;

    /** New width */
    private int newWidth;

    /** New height */
    private int newHeight;

    /** Old left */
    private int oldLeft;

    /** Old top */
    private int oldTop;

    /** Old width */
    private int oldWidth;

    /** Old height */
    private int oldHeight;

    /** If is needed to unblock the component after resizing */
    private boolean unblock;

    /***
     * Constructor
     * @param board link to board
     * @param component target component
     * @param newLeft new left
     * @param newTop new top
     * @param newWidth new width
     * @param newHeight new height
     * @param unblock if is need to unblock the component
     */
    public ResizeCommand(InteractiveBoard board, BasicComponent component, int newLeft, int newTop,
                         int newWidth, int newHeight, boolean unblock) {
        super(board, component);
        oldLeft = component.getComponentLeft();
        oldTop = component.getComponentTop();
        oldWidth = component.getComponentWidth();
        oldHeight = component.getComponentHeight();
        this.newLeft = newLeft;
        this.newTop = newTop;
        newWidth = Math.max(newWidth, component.getComponentMinimumWidth());
        newWidth = Math.min(newWidth, component.getComponentMaximumWidth());
        this.newWidth = newWidth;
        newHeight = Math.max(newHeight, component.getComponentMinimumHeight());
        newHeight = Math.min(newHeight, component.getComponentMaximumHeight());
        this.newHeight = newHeight;
        this.unblock = unblock;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(component.getComponentStatus(), component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ResizeEvent(hash, id, newLeft, newTop, newWidth, newHeight, unblock));
        }
        else {
            throw new IllegalStateException("You have no rights to resize this component!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(component.getComponentStatus(), component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ResizeEvent(hash, id, oldLeft, oldTop, oldWidth, oldHeight, false));
        }
        else {
            throw new IllegalStateException("You have no rights to resize this component!");
        }
    }
}
