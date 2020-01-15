package rslib.cs.protocol.events.board;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a point event
 */
public class PointEvent extends BoardEvent {

    /** X-coordinate */
    private int x;

    /** Y-coordinate */
    private int y;

    /** Point author */
    private String username;

    /***
     * Constructor
     * @param hash board hash
     * @param x x-coordinate
     * @param y y-coordinate
     * @param username point author
     */
    public PointEvent(int hash, int x, int y, String username) {
        super(hash);
        if (username == null) {
            throw new IllegalArgumentException("PointEvent: username is null!");
        }
        this.x = x;
        this.y = y;
        this.username = username;
    }

    /***
     * Constructor for externalization
     */
    public PointEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.POINT_E;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(x);
        out.writeInt(y);
        out.writeUTF(username);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        x = in.readInt();
        y = in.readInt();
        username = in.readUTF();
    }
}
