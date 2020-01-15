package rslib.commands.common;

import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.board.common.ChangeBlockEvent;
import rslib.gui.BasicComponent;
import rslib.gui.board.InteractiveBoard;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/***
 * Represents change block command
 */
public class ChangeBlockCommand extends ComponentCommand {

    /** New block */
    private boolean newBlock;

    /***
     * Constructor
     * @param board link to board
     * @param component target component
     * @param newBlock new block
     */
    public ChangeBlockCommand(InteractiveBoard board, BasicComponent component, boolean newBlock) {
        super(board, component);
        this.newBlock = newBlock;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(component.getComponentStatus(), component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeBlockEvent(hash, id, newBlock, userClient.getUsername()));
        }
        else {
            throw new IllegalStateException("You have no rights to change block of this component!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        throw new NotImplementedException();
    }
}