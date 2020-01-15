package rslib.cs.protocol.events.main_client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

/***
 * Represents a setting ban list event
 */
public class SetBanListEvent extends MainClientEvent {

    /** For better parsing */
    public static final long serialVersionUID = 698737522222344L;

    /** User list */
    private Map<String, String> users;

    /***
     * Constructor
     * @param users ban list
     */
    public SetBanListEvent(Map<String, String> users) {
        if (users == null) {
            throw new IllegalArgumentException("SetBanListEvent: users is null!");
        }
        this.users = users;
    }

    /***
     * Constructor for externalization
     */
    public SetBanListEvent() {
    }

    public Map<String, String> getUsers() {
        return users;
    }

    @Override
    public MainClientEventType getIndex() {
        return MainClientEventType.BAN_LIST_E;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(users);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        users = (Map<String, String>) in.readObject();
    }
}
