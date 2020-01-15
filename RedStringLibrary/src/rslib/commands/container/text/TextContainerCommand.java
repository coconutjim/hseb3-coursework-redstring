package rslib.commands.container.text;


import rslib.commands.container.ContainerCommand;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.container.BoardContainer;
import rslib.gui.container.text.TextContainer;

/***
 * Represents a text container command
 */
public abstract class TextContainerCommand extends ContainerCommand {

    /***
     * Constructor
     * @param board link to board
     * @param container target container
     */
    public TextContainerCommand(InteractiveBoard board, BoardContainer container) {
        super(board, container);
    }

    /***
     * Gets text container by its id
     * @return text container
     * @throws IllegalStateException if container was not found
     */
    protected TextContainer getTextContainer() throws IllegalStateException {
        try {
            return (TextContainer) getContainer();
        }
        catch (ClassCastException e) {
            throw new IllegalStateException(getClass().getSimpleName() + ": container not found!");
        }
    }
}
