package rslib.commands.common;

import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.board.common.ChangeColorEvent;
import rslib.gui.BasicComponent;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.style.ColorModel;

/***
 * Represents change color command
 */
public class ChangeColorCommand extends ComponentCommand {
    
    /** Old foreground color */
    private ColorModel oldForeground;

    /** Old background color */
    private ColorModel oldBackground;
    
    /** New foreground color */
    private ColorModel newForeground;

    /** New background color */
    private ColorModel newBackground;

    /***
     * Constructor
     * @param board link to board
     * @param component target component
     * @param newForeground new foreground color
     * @param newBackground new background color
     */
    public ChangeColorCommand(InteractiveBoard board, BasicComponent component,
                              ColorModel newForeground, ColorModel newBackground) {
        super(board, component);
        if (newForeground == null && newBackground == null) {
            throw new IllegalArgumentException("ChangeColorCommand: both colors are null!");
        }
        this.newForeground = newForeground;
        oldForeground = component.getComponentForeground();
        this.newBackground = newBackground;
        oldBackground = component.getComponentBackground();
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(component.getComponentStatus(), component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeColorEvent(hash, id, newForeground, newBackground));
        }
        else {
            throw new IllegalStateException("You have no rights to change color of this component!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        BasicComponent component = getComponent();
        if (userClient.checkBoardRights(component.getComponentStatus(), component.getComponentOwner())) {
            checkBlocked(userClient, component);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeColorEvent(hash, id,
                    newForeground == null? null : oldForeground,
                    newBackground == null? null : oldBackground));
        }
        else {
            throw new IllegalStateException("You have no rights to change color of this component!");
        }
    }
}
