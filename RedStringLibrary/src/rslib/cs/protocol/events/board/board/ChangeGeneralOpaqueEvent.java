package rslib.cs.protocol.events.board.board;

import rslib.cs.protocol.events.board.BoardEvent;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents change general container opaque event
 */
public class ChangeGeneralOpaqueEvent extends BoardEvent {

    /** For better parsing */
    public static final long serialVersionUID = 8373656672222342L;

    /** New opaque */
    private boolean opaque;

    /***
     * Constructor
     * @param hash board hash
     * @param opaque new opaque
     */
    public ChangeGeneralOpaqueEvent(int hash, boolean opaque) {
        super(hash);
        this.opaque = opaque;
    }

    /***
     * Constructor for externalization
     */
    public ChangeGeneralOpaqueEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CHANGE_GENERAL_OPAQUE_E;
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
