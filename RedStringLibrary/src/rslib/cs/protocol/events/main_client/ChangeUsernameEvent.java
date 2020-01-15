package rslib.cs.protocol.events.main_client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a changing newName event
 */
public class ChangeUsernameEvent extends MainClientEvent {

    /** For better parsing */
    public static final long serialVersionUID = 920046829473L;

    /** New name */
    private String newName;

    /***
     * Constructor
     * @param newName new name
     */
    public ChangeUsernameEvent(String newName) {
        if (newName == null) {
            throw new IllegalArgumentException("ChangeUsernameEvent: newName is null!");
        }
        this.newName = newName;
    }

    /***
     * Constructor for externalization
     */
    public ChangeUsernameEvent() {
    }

    @Override
    public MainClientEventType getIndex() {
        return MainClientEventType.CHANGE_USERNAME_E;
    }

    public String getNewName() {
        return newName;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(newName);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        newName = in.readUTF();
    }
}
