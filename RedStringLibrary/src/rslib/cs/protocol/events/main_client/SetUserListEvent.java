package rslib.cs.protocol.events.main_client;

import rslib.cs.common.User;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

/***
 * Represents a setting user list event
 */
public class SetUserListEvent extends MainClientEvent {

    /** For better parsing */
    public static final long serialVersionUID = 8245516522244124L;

    /** User list (user and host) */
    private Map<User, String> users;

    /***
     * Constructor
     * @param users user list
     */
    public SetUserListEvent(Map<User, String> users) {
        if (users == null) {
            throw new IllegalArgumentException("SetUserListEvent: users is null!");
        }
        this.users = users;
    }

    /***
     * Constructor for externalization
     */
    public SetUserListEvent() {
    }

    public Map<User, String> getUsers() {
        return users;
    }

    @Override
    public MainClientEventType getIndex() {
        return MainClientEventType.USER_LIST_E;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(users);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        users = (Map<User, String>) in.readObject();
    }
}
