package rslib.cs.protocol.requests.to_server.user;

import rslib.cs.common.LobbyInfo;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Login request
 */
public class LoginRequest implements UserServerRequest {

    /** For better parsing */
    public static final long serialVersionUID = 205753295633515L;

    /** Info about server */
    private LobbyInfo lobbyInfo;

    /** User name */
    private String username;

    /***
     * Constructor
     * @param username username
     * @param lobbyInfo info about server
     */
    public LoginRequest(String username, LobbyInfo lobbyInfo) {
        if (lobbyInfo == null) {
            throw new IllegalArgumentException("LoginRequest: lobbyInfo is null!");
        }
        if (username == null) {
            throw new IllegalArgumentException("LoginRequest: username is null!");
        }
        this.username = username;
        this.lobbyInfo = lobbyInfo;
    }

    /***
     * Constructor for externalization
     */
    public LoginRequest() {
    }

    @Override
    public UserServerRequestType getIndex() {
        return UserServerRequestType.LOGIN_R;
    }

    public String getUsername() {
        return username;
    }

    public LobbyInfo getLobbyInfo() {
        return lobbyInfo;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(lobbyInfo);
        out.writeUTF(username);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        lobbyInfo = (LobbyInfo) in.readObject();
        username = in.readUTF();
    }
}
