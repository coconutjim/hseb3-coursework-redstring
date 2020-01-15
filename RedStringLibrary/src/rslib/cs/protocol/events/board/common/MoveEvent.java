package rslib.cs.protocol.events.board.common;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a move event
 */
public class MoveEvent extends ComponentEvent {

    /** For better parsing */
    public static final long serialVersionUID = 8287311214123L;

    /** New left coordinate */
    private int left;

    /** New top coordinate */
    private int top;

    /** If is needed to unblock the component after moving */
    private boolean unblock;

    /***
     * Constructor
     * @param hash board hash
     * @param id component id
     * @param left new left coordinate
     * @param top new top coordinate
     * @param unblock if is need to unblock the component
     */
    public MoveEvent(int hash, int id, int left, int top, boolean unblock) {
        super(hash, id);
        this.left = left;
        this.top = top;
        this.unblock = unblock;
    }

    /***
     * Constructor for externalization
     */
    public MoveEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.MOVE_E;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public boolean isUnblock() {
        return unblock;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(left);
        out.writeInt(top);
        out.writeBoolean(unblock);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        left = in.readInt();
        top = in.readInt();
        unblock = in.readBoolean();
    }
}
