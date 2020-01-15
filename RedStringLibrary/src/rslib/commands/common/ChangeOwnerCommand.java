package rslib.commands.common;

import rslib.cs.client.user.UserClient;
import rslib.cs.common.Status;
import rslib.cs.protocol.events.board.common.ChangeOwnerEvent;
import rslib.gui.BasicComponent;
import rslib.gui.board.InteractiveBoard;

/***
 * Represents a change owner command
 */
public class ChangeOwnerCommand extends ComponentCommand {

    /** New owner */
    private String newOwner;

    /** Old owner */
    private String oldOwner;

    /***
     * Constructor
     * @param board link to board
     * @param component target component
     * @param newOwner new component owner
     */
    public ChangeOwnerCommand(InteractiveBoard board, BasicComponent component, String newOwner) {
        super(board, component);
        if (newOwner == null) {
            throw new IllegalArgumentException("ChangeOwnerCommand: newOwner is null!");
        }
        oldOwner = component.getComponentOwner();
        this.newOwner = newOwner;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(Status.MODERATOR, component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeOwnerEvent(hash, id, newOwner));
        }
        else {
            throw new IllegalStateException("You have no rights to change owner of this component!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(Status.MODERATOR, component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeOwnerEvent(hash, id, oldOwner));
        }
        else {
            throw new IllegalStateException("You have no rights to change owner of this component!");
        }
    }
}
