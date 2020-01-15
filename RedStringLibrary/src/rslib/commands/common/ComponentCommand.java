package rslib.commands.common;

import rslib.commands.Command;
import rslib.gui.BasicComponent;
import rslib.gui.board.InteractiveBoard;

/***
 * Represents a component command
 */
public abstract class ComponentCommand extends Command {

    /** Component id */
    protected int id;

    /***
     * Constructor
     * @param board link to board
     * @param component target component
     */
    public ComponentCommand(InteractiveBoard board, BasicComponent component) {
        super(board);
        if (component == null) {
            throw new IllegalArgumentException("ComponentCommand: component is null!");
        }
        id = component.getComponentId();
    }

    /***
     * Gets component by its id
     * @return component
     * @throws IllegalStateException if component was not found
     */
    protected BasicComponent getComponent() throws IllegalStateException {
        BasicComponent component;
        if (id == InteractiveBoard.BOARD_ID) {
            component = getBoard();
        }
        else {
            component = getBoard().findContainer(id);
            if (component == null) {
                throw new IllegalStateException(getClass().getSimpleName() + ": component not found!");
            }
        }
        return component;
    }
}
