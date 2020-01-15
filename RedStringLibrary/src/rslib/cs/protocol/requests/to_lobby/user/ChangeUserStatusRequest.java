package rslib.cs.protocol.requests.to_lobby.user;

import rslib.cs.common.Status;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents changing user status request from user
 */
public class ChangeUserStatusRequest implements UserLobbyRequest {

    /** For better parsing */
    public static final long serialVersionUID = 825411231552382L;

    /** User to change status */
    private String username;

    /** New status*/
    private Status status;

    /***
     * Constructor
     * @param username user to change status
     * @param status new status
     */
    public ChangeUserStatusRequest(String username, Status status) {
        if (username == null) {
            throw new IllegalArgumentException("ChangeUserStatusRequest: username is null!");
        }
        this.username = username;
        if (! (status.ordinal() >= Status.READONLY.ordinal()
                && status.ordinal() <= Status.MODERATOR.ordinal())) {
            throw new IllegalArgumentException("ChangeUserStatusRequest: wrong status!");
        }
        this.status = status;
    }

    /***
     * Constructor for externalization
     */
    public ChangeUserStatusRequest() {
    }

    @Override
    public UserLobbyRequestType getIndex() {
        return UserLobbyRequestType.CHANGE_USER_STATUS_R;
    }

    public String getUsername() {
        return username;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(username);
        out.writeObject(status);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        username = in.readUTF();
        status = (Status) in.readObject();
    }
}
