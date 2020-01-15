package rslib.cs.protocol.requests.to_lobby.user;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents ban request from user
 */
public class BanRequest implements UserLobbyRequest {

    /** For better parsing */
    public static final long serialVersionUID = 912228777774223L;

    /** User to ban */
    private String username;

    /***
     * Constructor
     * @param username user to ban
     */
    public BanRequest(String username) {
        if (username == null) {
            throw new IllegalArgumentException("BanRequest: username is null!");
        }
        this.username = username;
    }

    /***
     * Constructor for externalization
     */
    public BanRequest() {
    }

    @Override
    public UserLobbyRequestType getIndex() {
        return UserLobbyRequestType.BAN_R;
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

