package rslib.cs.protocol.events.board.board;

import rslib.cs.protocol.events.board.BoardEvent;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents change sync asynchronous event
 */
public class ChangeSyncModeEvent extends BoardEvent {

    /** For better parsing */
    public static final long serialVersionUID = 83789657239342L;

    /** New asynchronous */
    private boolean asynchronous;

    /***
     * Constructor
     * @param hash board hash
     * @param asynchronous new asynchronous
     */
    public ChangeSyncModeEvent(int hash, boolean asynchronous) {
        super(hash);
        this.asynchronous = asynchronous;
    }

    /***
     * Constructor for externalization
     */
    public ChangeSyncModeEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CHANGE_SYNC_MODE_E;
    }

    public boolean isAsynchronous() {
        return asynchronous;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(asynchronous);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        asynchronous = in.readBoolean();
    }
}