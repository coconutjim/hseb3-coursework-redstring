package rslib.commands.common;

import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.board.common.ChangeFontEvent;
import rslib.gui.BasicComponent;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.style.FontModel;

/***
 * Represents change font command
 */
public class ChangeFontCommand extends ComponentCommand {

    /** Old font */
    private FontModel oldFont;

    /** New font */
    private FontModel newFont;

    /***
     * Constructor
     * @param board link to board
     * @param component target component
     * @param newFont new font
     */
    public ChangeFontCommand(InteractiveBoard board, BasicComponent component, FontModel newFont) {
        super(board, component);
        if (newFont == null) {
            throw new IllegalArgumentException("ChangeFontCommand: newFont is null!");
        }
        oldFont = component.getComponentFont();
        this.newFont = newFont;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(component.getComponentStatus(), component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeFontEvent(hash, id, newFont));
        }
        else {
            throw new IllegalStateException("You have no rights to change font of this component!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(component.getComponentStatus(), component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeFontEvent(hash, id, oldFont));
        }
        else {
            throw new IllegalStateException("You have no rights to change font of this component!");
        }
    }
}
