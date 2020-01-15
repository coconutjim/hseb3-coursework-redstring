package rslib.commands.container.file;

import rslib.commands.container.ContainerCommand;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.container.BoardContainer;
import rslib.gui.container.file.FileContainer;

/***
 * Represents a text container command
 */
public abstract class FileContainerCommand extends ContainerCommand {

    /***
     * Constructor
     * @param board link to board
     * @param container target container
     */
    public FileContainerCommand(InteractiveBoard board, BoardContainer container) {
        super(board, container);
    }

    /***
     * Gets image container by its id
     * @return file container
     * @throws IllegalStateException if container was not found
     */
    protected FileContainer getFileContainer() throws IllegalStateException {
        try {
            return (FileContainer) getContainer();
        }
        catch (ClassCastException e) {
            throw new IllegalStateException(getClass().getSimpleName() + ": container not found!");
        }
    }
}