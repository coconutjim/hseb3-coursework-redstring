package rslib.cs.protocol.requests.to_lobby.user;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents changing username request from user
 */
public class ChangeUsernameRequest implements UserLobbyRequest {

    /** For better parsing */
    public static final long serialVersionUID = 333681653424321L;

    /** New username */
    private String username;

    /***
     * Constructor
     * @param username new lobby name
     */
    public ChangeUsernameRequest(String username) {
        if (username == null) {
            throw new IllegalArgumentException("ChangeUsernameRequest: username is null!");
        }
        this.username = username;
    }

    /***
     * Constructor for externalization
     */
    public ChangeUsernameRequest() {
    }

    @Override
    public UserLobbyRequestType getIndex() {
        return UserLobbyRequestType.CHANGE_USERNAME_R;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(username);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        username = in.readUTF();
    }
}

