package rslib.cs.protocol.events.board.common;

import rslib.cs.common.Status;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents change component status event
 */
public class ChangeStatusEvent extends ComponentEvent {

    /** New component status */
    private Status newStatus;

    /***
     * Constructor
     * @param hash board hash
     * @param id component id
     * @param newStatus new component status
     */
    public ChangeStatusEvent(int hash, int id, Status newStatus) {
        super(hash, id);
        if (newStatus.ordinal() > Status.LOBBY_ROOT.ordinal() || newStatus.ordinal() < Status.READONLY.ordinal()) {
            throw new IllegalArgumentException("ChangeComponentOwnerEvent: illegal newStatus!");
        }
        this.newStatus = newStatus;
    }

    /***
     * Constructor for externalization
     */
    public ChangeStatusEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CHANGE_STATUS_E;
    }

    public Status getNewStatus() {
        return newStatus;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(newStatus);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        newStatus = (Status) in.readObject();
    }
}