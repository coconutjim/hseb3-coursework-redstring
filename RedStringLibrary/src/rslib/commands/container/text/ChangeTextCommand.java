package rslib.commands.container.text;

import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.board.container.text.ChangeTextEvent;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.container.text.TextContainer;

/***
 * Represents change text command
 */
public class ChangeTextCommand extends TextContainerCommand {

    /** Old text */
    protected String oldText;

    /** New text */
    protected String newText;

    /***
     * Constructor
     * @param board link to board
     * @param textContainer text container
     * @param newText new text
     */
    public ChangeTextCommand(InteractiveBoard board, TextContainer textContainer, String newText) {
        super(board, textContainer);
        oldText = textContainer.getText();
        this.newText = newText;
    }

    @Override
    public void execute(UserClient userClient) throws IllegalStateException {
        super.execute(userClient);
        TextContainer textContainer = getTextContainer();
        if (userClient.checkBoardRights(textContainer.getComponentStatus(), textContainer.getComponentOwner())) {
            checkBlocked(userClient, textContainer);
            if (newText != null && newText.length() > TextContainer.TEXT_MAXIMUM_SIZE) {
                throw new IllegalStateException("Too much text! Not more than " +
                        "100,000 symbols allowed!");
            }
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeTextEvent(hash, id, newText));
        }
        else {
            throw new IllegalStateException("You have no rights to change text of this container!");
        }
    }

    @Override
    public void undo(UserClient userClient) throws IllegalStateException {
        super.undo(userClient);
        TextContainer textContainer = getTextContainer();
        if (userClient.checkBoardRights(textContainer.getComponentStatus(), textContainer.getComponentOwner())) {
            checkBlocked(userClient, textContainer);
            int hash = getBoard().hashCode();
            userClient.addBoardEvent(new ChangeTextEvent(hash, id, oldText));
        }
        else {
            throw new IllegalStateException("You have no rights to change text of this container!");
        }
    }
}
