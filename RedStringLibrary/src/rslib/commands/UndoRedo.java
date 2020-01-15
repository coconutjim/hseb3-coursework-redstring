package rslib.commands;

import rslib.cs.client.user.UserClient;
import java.util.Stack;

/***
 * Facilities for an undo-redo mechanism (based on command functionality)
 */
public class UndoRedo {

    /** Link to client */
    protected UserClient userClient;

    /** The undo stack (done commands) */
    private Stack<Command> undoStack;

    /** The redo stack (undone commands) */
    private Stack<Command> redoStack;

    /***
     * Constructor
     * @param userClient link to client
     */
    public UndoRedo(UserClient userClient) {
        if (userClient == null) {
            throw new IllegalArgumentException("UndoRedo: userClient is null!");
        }
        this.userClient = userClient;
        undoStack = new Stack<>();
        redoStack = new Stack<>();
    }

    /***
     * Returns whether an undo is possible.
     * @return true is possible, false otherwise
     */
    public boolean canUndo() {
        return ! undoStack.empty();
    }

    /***
     * Returns whether a redo is possible.
     * @return true is possible, false otherwise
     */
    public boolean canRedo() {
        return ! redoStack.empty();
    }

    /***
     * Returns most recently done command
     * @return command at top of undo stack
     * @throws IllegalStateException if undo cannot be performed
     */
    public Command lastDone() throws IllegalStateException {
        if (! canUndo()) {
            throw new IllegalStateException("UndoRedo.lastDone(): "
                    + "can not undo anything!");
        }
        return undoStack.pop();
    }

    /***
     * Returns most recently undone command
     * @return command at top of redo stack
     * @throws IllegalStateException if redo cannot be performed
     */
    public Command lastUndone() throws IllegalStateException {
        if (! canRedo()) {
            throw new IllegalStateException("UndoRedo.lastUndone(): "
                    + "can not redo anything!");
        }
        return redoStack.pop();
    }

    /***
     * Clears all undo-redo history.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    /***
     * Adds given command to the do-history.
     * If the command was not yet executed, it does
     * @param command command
     * @param undoable if the command can be undone
     * @throws IllegalStateException if can not be performed
     */
    public void did(final Command command, boolean undoable) throws IllegalStateException {
        if (! command.isExecuted()) {
            command.execute(userClient);
        }
        if (undoable) {
            undoStack.push(command);
            redoStack.clear();
        }
    }

    /***
     * Undo the most recently done command, optionally allowing it to be redone.
     * @param redoable  if can be redone
     * @throws IllegalStateException  if can not be undone
     */
    public void undo(final boolean redoable)
            throws IllegalStateException {
        if (! canUndo()) {
            throw new IllegalStateException("UndoRedo.undo(): "
                    + "command can not be undone!");
        }
        Command recent = lastDone();
        recent.undo(userClient);
        if (redoable) {
            redoStack.push(recent);
        }
    }

    /**
     * Redo the most recently undone command.
     * @throws IllegalStateException  if can not be redone
     */
    public void redo()
            throws IllegalStateException {
        if (! canRedo()) {
            throw new IllegalStateException("UndoRedo.redo(): "
                    + "command can not be redone!");
        }
        Command recent = lastUndone();
        recent.execute(userClient);
        undoStack.push(recent);
    }

    /**
     * Undo all done commands.
     */
    public void undoAll() {
        while (canUndo()) {
            undo(true);
        }
    }

    /**
     * Redo all undone commands.
     */
    public void redoAll() {
        while (canRedo()) {
            redo();
        }
    }

}