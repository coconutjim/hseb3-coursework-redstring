package rslib.commands.common;

import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.board.common.ChangeNameEvent;
import rslib.gui.BasicComponent;
import rslib.gui.board.InteractiveBoard;

/***
 * Represents set name command
 */
public class ChangeNameCommand extends ComponentCommand {

    /** Old name */
    private String oldName;

    /** New name */
    private String newName;

    /***
     * Constructor
     * @param board link to board
     * @param component target component
     * @param newName new component name
     */
    public ChangeNameCommand(InteractiveBoard board, BasicComponent component, String newName) {
        super(board, component);
        if (newName == null) {
            throw new IllegalArgumentException("ChangeNameCommand: newName is null!");
        }
        oldName = component.getComponentName();
        this.newName = newName;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(component.getComponentStatus(), component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeNameEvent(hash, id, newName));
        }
        else {
            throw new IllegalStateException("You have no rights to change name of this component!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(component.getComponentStatus(), component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeNameEvent(hash, id, oldName));
        }
        else {
            throw new IllegalStateException("You have no rights to change name of this component!");
        }
    }
}
