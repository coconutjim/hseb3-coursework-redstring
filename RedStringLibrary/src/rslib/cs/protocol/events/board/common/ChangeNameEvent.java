package rslib.cs.protocol.events.board.common;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents set name event
 */
public class ChangeNameEvent extends ComponentEvent {

    /** For better parsing */
    public static final long serialVersionUID = 872537822342111L;

    /** New name */
    private String name;

    /***
     * Constructor
     * @param hash board hash
     * @param id component id
     * @param name new name
     */
    public ChangeNameEvent(int hash, int id, String name) {
        super(hash, id);
        if (name == null) {
            throw new IllegalArgumentException("ChangeNameEvent: name is null!");
        }
        this.name = name;
    }

    /***
     * Constructor for externalization
     */
    public ChangeNameEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CHANGE_NAME_E;
    }

    public String getName() {
        return name;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeUTF(name);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        name = in.readUTF();
    }
}
