package rslib.cs.protocol.requests.to_lobby.user;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents changing lobby name request from user
 */
public class ChangeLobbyNameRequest implements UserLobbyRequest {

    /** For better parsing */
    public static final long serialVersionUID = 827515155633331L;

    /** New lobby name */
    private String lobbyName;

    /***
     * Constructor
     * @param lobbyName new lobby name
     */
    public ChangeLobbyNameRequest(String lobbyName) {
        if (lobbyName == null) {
            throw new IllegalArgumentException("ChangeLobbyNameRequest: lobbyName is null!");
        }
        this.lobbyName = lobbyName;
    }

    /***
     * Constructor for externalization
     */
    public ChangeLobbyNameRequest() {
    }

    @Override
    public UserLobbyRequestType getIndex() {
        return UserLobbyRequestType.CHANGE_LOBBY_NAME_R;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(lobbyName);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        lobbyName = in.readUTF();
    }
}
