package rslib.cs.protocol.events.board.board;

import rslib.cs.protocol.events.board.BoardEvent;
import rslib.gui.container.ExternalizableContainer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * Represents board content event
 */
public class SetBoardContentEvent extends BoardEvent {

    /** Saved data */
    private CopyOnWriteArrayList<ExternalizableContainer> serializableContainers;

    /***
     * Constructor
     * @param hash board hash
     * @param serializableContainers saved data
     */
    public SetBoardContentEvent(int hash,
                                CopyOnWriteArrayList<ExternalizableContainer> serializableContainers) {
        super(hash);
        if (serializableContainers == null) {
            throw new IllegalArgumentException("SetBoardContentEvent: serializableContainers is null!");
        }
        this.serializableContainers = serializableContainers;
    }

    /***
     * Constructor for externalization
     */
    public SetBoardContentEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.SET_BOARD_CONTENT_E;
    }

    public CopyOnWriteArrayList<ExternalizableContainer> getSerializableContainers() {
        return serializableContainers;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(serializableContainers);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        serializableContainers = (CopyOnWriteArrayList<ExternalizableContainer>) in.readObject();
    }
}
