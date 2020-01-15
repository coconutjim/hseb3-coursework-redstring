package rslib.cs.protocol.events.board.container;

import rslib.cs.protocol.events.board.BoardEvent;
import rslib.gui.container.ExternalizableContainer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a new container event
 */
public class AddContainerEvent extends BoardEvent {

    /** For better parsing */
    public static final long serialVersionUID = 998176541232L;

    /** New container */
    private ExternalizableContainer serializableContainer;

    /***
     * Constructor
     * @param hash board hash
     * @param serializableContainer saved container
     */
    public AddContainerEvent(int hash, ExternalizableContainer serializableContainer) {
        super(hash);
        if (serializableContainer == null) {
            throw new IllegalArgumentException("AddContainerEvent: serializableContainer is null!");
        }
        this.serializableContainer = serializableContainer;
    }

    /***
     * Constructor for externalization
     */
    public AddContainerEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.ADD_CONTAINER_E;
    }

    public ExternalizableContainer getSerializableContainer() {
        return serializableContainer;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(serializableContainer);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        serializableContainer = (ExternalizableContainer) in.readObject();
    }
}
