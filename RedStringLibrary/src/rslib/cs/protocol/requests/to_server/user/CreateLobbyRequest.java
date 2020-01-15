package rslib.cs.protocol.requests.to_server.user;

import rslib.cs.common.LobbyInfo;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Create lobby request
 */
public class CreateLobbyRequest implements UserServerRequest {

    /** For better parsing */
    public static final long serialVersionUID = 76557586543323456L;

    /** Info about server */
    private LobbyInfo lobbyInfo;

    /** User name */
    private String username;

    /***
     * Constructor
     * @param lobbyInfo info of the server to be created
     * @param username command sender
     */
    public CreateLobbyRequest(String username, LobbyInfo lobbyInfo) {
        if (lobbyInfo == null) {
            throw new IllegalArgumentException("CreateServerRequest: lobbyInfo is null!");
        }
        if (username == null) {
            throw new IllegalArgumentException("CreateServerRequest: username is null!");
        }
        this.username = username;
        this.lobbyInfo = lobbyInfo;
    }

    /***
     * Constructor for externalization
     */
    public CreateLobbyRequest() {
    }

    @Override
    public UserServerRequestType getIndex() {
        return UserServerRequestType.CREATE_LOBBY_R;
    }

    public LobbyInfo getLobbyInfo() {
        return lobbyInfo;
    }

    public String getUsername() {
        return username;
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
