package rslib.cs.protocol.events.board.common;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a blocking/unblocking event
 */
public class ChangeBlockEvent extends ComponentEvent {

    /** Username */
    private String username;

    /** Block of unblock */
    private boolean block;

    /***
     * Constructor
     * @param hash board hash
     * @param id component id
     * @param block or unblocked
     * @param username username
     */
    public ChangeBlockEvent(int hash, int id, boolean block, String username) {
        super(hash, id);
        this.username = username;
        this.block = block;
    }

    /***
     * Constructor for externalization
     */
    public ChangeBlockEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CHANGE_BLOCK_E;
    }

    public String getUsername() {
        return username;
    }

    public boolean isBlock() {
        return block;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(block);
        if (username == null) {
            out.writeInt(0);
        }
        else {
            out.writeInt(username.length());
            out.writeUTF(username);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        block = in.readBoolean();
        if (in.readInt() != 0) {
            username = in.readUTF();
        }
    }
}
