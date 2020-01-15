package rslib.cs.protocol.events.board.common;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a resize event
 */
public class ResizeEvent extends ComponentEvent {

    /** For better parsing */
    public static final long serialVersionUID = 4277761632223L;

    /** New left */
    private int left;

    /** New top */
    private int top;

    /** New width */
    private int width;

    /** New height */
    private int height;

    /** If is needed to unblock the component after resizing */
    private boolean unblock;

    /***
     * Constructor
     * @param hash board hash
     * @param id component id
     * @param left new left
     * @param top new top
     * @param width new width
     * @param height new height
     * @param unblock if is need to unblock
     */
    public ResizeEvent(int hash, int id, int left, int top, int width, int height, boolean unblock) {
        super(hash, id);
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.unblock = unblock;
    }

    /***
     * Constructor for externalization
     */
    public ResizeEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.RESIZE_E;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isUnblock() {
        return unblock;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(left);
        out.writeInt(top);
        out.writeInt(width);
        out.writeInt(height);
        out.writeBoolean(unblock);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        left = in.readInt();
        top = in.readInt();
        width = in.readInt();
        height = in.readInt();
        unblock = in.readBoolean();
    }
}
