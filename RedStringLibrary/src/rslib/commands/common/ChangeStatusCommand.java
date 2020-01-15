package rslib.commands.common;

import rslib.cs.client.user.UserClient;
import rslib.cs.common.Status;
import rslib.cs.protocol.events.board.common.ChangeStatusEvent;
import rslib.gui.BasicComponent;
import rslib.gui.board.InteractiveBoard;

/***
 * Represents a change status command
 */
public class ChangeStatusCommand extends ComponentCommand {

    /** New status */
    private Status newStatus;

    /** Old owner */
    private Status oldStatus;

    /***
     * Constructor
     * @param board link to board
     * @param component target component
     * @param newStatus new component status
     */
    public ChangeStatusCommand(InteractiveBoard board, BasicComponent component, Status newStatus) {
        super(board, component);
        if (newStatus.ordinal() > Status.LOBBY_ROOT.ordinal() || newStatus.ordinal() < Status.READONLY.ordinal()) {
            throw new IllegalArgumentException("ChangeOwnerCommand: illegal newStatus!");
        }
        oldStatus = component.getComponentStatus();
        this.newStatus = newStatus;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(Status.MODERATOR, component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeStatusEvent(hash, id, newStatus));
        }
        else {
            throw new IllegalStateException("You have no rights to change status of this component!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(Status.MODERATOR, component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeStatusEvent(hash, id, oldStatus));
        }
        else {
            throw new IllegalStateException("You have no rights to change owner of this component!");
        }
    }
}