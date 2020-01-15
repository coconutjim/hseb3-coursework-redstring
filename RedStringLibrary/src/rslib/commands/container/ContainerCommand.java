package rslib.commands.container;

import rslib.commands.common.ComponentCommand;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.container.BoardContainer;

/**
 * Represents a container command
 */
public abstract class ContainerCommand extends ComponentCommand {

    /***
     * Constructor
     * @param board link to board
     * @param container target container
     */
    public ContainerCommand(InteractiveBoard board, BoardContainer container) {
        super(board, container);
    }

    /***
     * Gets container by its id
     * @return container
     * @throws IllegalStateException if container was not found
     */
    protected BoardContainer getContainer() throws IllegalStateException {
        BoardContainer container = getBoard().findContainer(id);
        if (container == null) {
            throw new IllegalStateException(getClass().getSimpleName() + ": container not found!");
        }
        return container;
    }
}
