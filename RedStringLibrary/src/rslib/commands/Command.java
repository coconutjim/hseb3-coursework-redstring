package rslib.commands;

import rslib.cs.client.user.UserClient;
import rslib.gui.BasicComponent;
import rslib.gui.board.InteractiveBoard;

/**
 * Represents an executable and undoable command
 */
public abstract class Command {

    /** Link to board */
    private InteractiveBoard board;

    /** Execution state */
    private boolean executed;

    /***
     * Constructor
     * @param board link to board
     */
    public Command(InteractiveBoard board) {
        if (board == null) {
            throw new IllegalArgumentException("Command: board is null!");
        }
        this.board = board;
        this.executed = false;
    }

    /***
     * Executes the command
     * @param userClient link to client
     * @throws IllegalStateException if command has been executed already
     */
    public void execute(UserClient userClient) throws IllegalStateException {
        if (executed) {
            throw new IllegalStateException("Command.execute(): "
                    + "command was already executed");
        }
        executed = true;
    }

    /***
     * Undoes the command
     * @param userClient link to client
     * @throws IllegalStateException if command has not been executed or can not been undone
     */
    public void undo(UserClient userClient) throws IllegalStateException {
        if (! executed) {
            throw new IllegalStateException("Command.undo(): "
                    + "command was not yet executed");
        }
        executed = false;
    }

    public boolean isExecuted() {
        return executed;
    }

    /***
     * Return board
     * @return board
     * @throws IllegalStateException if board is not valid
     */
    protected InteractiveBoard getBoard() throws IllegalStateException {
        //TODO: mb some checks
        if (board == null) {
            throw new IllegalStateException("Board was not found!");
        }
        return board;
    }

    /***
     * Checks if component is blocked
     * @param userClient client
     * @param component component
     * @throws IllegalStateException if component is blocked
     */
    protected void checkBlocked(UserClient userClient, BasicComponent component) throws IllegalStateException {
        if (component.isBlocked() && ! userClient.getUsername().equals(component.getBlockOwner())) {
            throw new IllegalStateException("Component is blocked!");
        }
    }
}
