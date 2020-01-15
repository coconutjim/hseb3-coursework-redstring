package rslib.cs.protocol.events.main_client;

import rslib.cs.common.Status;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a changing user status event
 */
public class ChangeUserStatusEvent extends MainClientEvent {

    /** For better parsing */
    public static final long serialVersionUID = 91115478397333L;

    /** New status */
    private Status status;

    /***
     * Constructor
     * @param status new status
     */
    public ChangeUserStatusEvent(Status status) {
        if (status.ordinal() < Status.READONLY.ordinal() && status.ordinal() > Status.MODERATOR.ordinal()) {
            throw new IllegalArgumentException("ChangeUserStatusEvent: illegal status!");
        }
        this.status = status;
    }

    /***
     * Constructor for externalization
     */
    public ChangeUserStatusEvent() {
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public MainClientEventType getIndex() {
        return MainClientEventType.CHANGE_USER_STATUS_E;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(status);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        status = (Status) in.readObject();
    }
}
