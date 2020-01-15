package rslib.commands.common;

import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.board.common.ChangeOpaqueEvent;
import rslib.gui.BasicComponent;
import rslib.gui.board.InteractiveBoard;

/***
 * Represents a change opaque command
 */
public class ChangeOpaqueCommand extends ComponentCommand {

    /** New opaque */
    private boolean newOpaque;

    /** Old opaque */
    private boolean oldOpaque;

    /***
     * Constructor
     * @param board link to board
     * @param component target component
     * @param newOpaque opaque or transparent
     */
    public ChangeOpaqueCommand(InteractiveBoard board, BasicComponent component, boolean newOpaque) {
        super(board, component);
        oldOpaque = component.isComponentOpaque();
        this.newOpaque = newOpaque;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(component.getComponentStatus(), component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeOpaqueEvent(hash, id, newOpaque));
        }
        else {
            throw new IllegalStateException("You have no rights to change opaque of this component!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(component.getComponentStatus(), component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeOpaqueEvent(hash, id, oldOpaque));
        }
        else {
            throw new IllegalStateException("You have no rights to change opaque of this component!");
        }
    }
}
