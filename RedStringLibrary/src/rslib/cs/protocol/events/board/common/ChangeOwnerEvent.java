package rslib.cs.protocol.events.board.common;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents change component owner event
 */
public class ChangeOwnerEvent extends ComponentEvent {

    /** New component owner */
    private String newOwner;

    /***
     * Constructor
     * @param hash board hash
     * @param id component id
     * @param newOwner new component owner
     */
    public ChangeOwnerEvent(int hash, int id, String newOwner) {
        super(hash, id);
        if (newOwner == null) {
            throw new IllegalArgumentException("ChangeComponentOwnerEvent: newOwner is null!");
        }
        this.newOwner = newOwner;
    }

    /***
     * Constructor for externalization
     */
    public ChangeOwnerEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CHANGE_OWNER_E;
    }

    public String getNewOwner() {
        return newOwner;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeUTF(newOwner);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        newOwner = in.readUTF();
    }
}
