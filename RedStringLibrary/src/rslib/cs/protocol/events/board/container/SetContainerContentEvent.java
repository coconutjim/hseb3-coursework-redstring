package rslib.cs.protocol.events.board.container;

import rslib.cs.protocol.events.board.common.ComponentEvent;
import rslib.gui.container.ExternalizableContainer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents set content event
 */
public class SetContainerContentEvent extends ComponentEvent {

    /** Saved data */
    private ExternalizableContainer serializableContainer;

    /***
     * Constructor
     * @param hash board has
     * @param id container id
     * @param serializableContainer saved data
     */
    public SetContainerContentEvent(int hash, int id, ExternalizableContainer serializableContainer) {
        super(hash, id);
        if (serializableContainer == null) {
            throw new IllegalArgumentException("SetConatinerContentEvent: serializableContainer is null!");
        }
        this.serializableContainer = serializableContainer;
    }

    /***
     * Constructor for externalization
     */
    public SetContainerContentEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.SET_CONTAINER_CONTENT_E;
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
