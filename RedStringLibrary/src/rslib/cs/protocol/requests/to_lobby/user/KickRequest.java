package rslib.cs.protocol.requests.to_lobby.user;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents a kick request from user
 */
public class KickRequest implements UserLobbyRequest {

    /** For better parsing */
    public static final long serialVersionUID = 538361442372902L;

    /** User to kick */
    private String username;

    /***
     * Constructor
     * @param username user to kick
     */
    public KickRequest(String username) {
        if (username == null) {
            throw new IllegalArgumentException("KickRequest: username is null!");
        }
        this.username = username;
    }

    /***
     * Constructor for externalization
     */
    public KickRequest() {
    }

    @Override
    public UserLobbyRequestType getIndex() {
        return UserLobbyRequestType.KICK_R;
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
