package rslib.cs.protocol.events.board.common;

import rslib.cs.protocol.events.board.BoardEvent;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a component event
 */
public abstract class ComponentEvent extends BoardEvent {

    /** For better parsing */
    public static final long serialVersionUID = 128626655145233L;

    /** Component id */
    private int id;

    /***
     * Constructor
     * @param hash board hash
     * @param id component id
     */
    protected ComponentEvent(int hash, int id) {
        super(hash);
        this.id = id;
    }

    /***
     * Constructor for externalization
     */
    public ComponentEvent() {
    }

    public int getId() {
        return id;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(id);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        id = in.readInt();
    }
}
