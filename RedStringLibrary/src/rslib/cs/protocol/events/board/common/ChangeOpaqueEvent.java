package rslib.cs.protocol.events.board.common;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a change opaque event
 */
public class ChangeOpaqueEvent extends ComponentEvent {

    /** Opaque or transparent */
    private boolean opaque;

    /***
     * Constructor
     * @param hash board hash
     * @param id component id
     * @param opaque opaque or transparent
     */
    public ChangeOpaqueEvent(int hash, int id, boolean opaque) {
        super(hash, id);
        this.opaque = opaque;
    }

    /***
     * Constructor for externalization
     */
    public ChangeOpaqueEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CHANGE_OPAQUE_E;
    }

    public boolean isOpaque() {
        return opaque;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(opaque);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        opaque = in.readBoolean();
    }
}
