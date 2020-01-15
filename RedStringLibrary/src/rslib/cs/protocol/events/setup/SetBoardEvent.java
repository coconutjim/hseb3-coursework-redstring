package rslib.cs.protocol.events.setup;

import rslib.gui.board.ExternalizableBoard;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a set board event
 */
public class SetBoardEvent implements SetupEvent {

    /** For better parsing */
    public static final long serialVersionUID = 972728287223278L;

    /** Board */
    private ExternalizableBoard serializableBoard;

    /***
     * Constructor
     * @param serializableBoard link to board
     */
    public SetBoardEvent(ExternalizableBoard serializableBoard) {
        if (serializableBoard == null) {
            throw new IllegalArgumentException("SetBoardEvent: serializableBoard is null!");
        }
        this.serializableBoard = serializableBoard;
    }

    /***
     * Constructor for externalization
     */
    public SetBoardEvent() {
    }

    @Override
    public SetupEventType getIndex() {
        return SetupEventType.SET_BOARD_E;
    }

    public ExternalizableBoard getSerializableBoard() {
        return serializableBoard;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(serializableBoard);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        serializableBoard = (ExternalizableBoard) in.readObject();
    }
}
