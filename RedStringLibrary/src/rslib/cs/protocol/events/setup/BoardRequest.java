package rslib.cs.protocol.events.setup;

import rslib.gui.board.ExternalizableBoard;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a board request
 */
public class BoardRequest implements SetupEvent {

    /** For better parsing */
    public static final long serialVersionUID = 987652226773423L;

    /** New board */
    private ExternalizableBoard serializableBoard;

    /***
     * Constructor
     * @param serializableBoard saved data
     */
    public BoardRequest(ExternalizableBoard serializableBoard) {
        if (serializableBoard == null) {
            throw new IllegalArgumentException("BoardRequest: serializableBoard is null!");
        }
        this.serializableBoard = serializableBoard;
    }

    /***
     * Constructor for externalization
     */
    public BoardRequest() {
    }

    @Override
    public SetupEventType getIndex() {
        return SetupEventType.BOARD_R;
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
