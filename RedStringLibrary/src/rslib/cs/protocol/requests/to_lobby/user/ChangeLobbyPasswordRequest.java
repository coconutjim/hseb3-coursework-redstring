package rslib.cs.protocol.requests.to_lobby.user;

import rslib.util.DataManagement;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents changing lobby password request from user
 */
public class ChangeLobbyPasswordRequest implements UserLobbyRequest {

    /** For better parsing */
    public static final long serialVersionUID = 98272526528222211L;

    /** New lobby password */
    private byte[] password;

    /***
     * Constructor
     * @param password new lobby password
     */
    public ChangeLobbyPasswordRequest(String password) {
        if (password != null) {
            this.password = DataManagement.digest(password);
        }
    }

    /***
     * Constructor for externalization
     */
    public ChangeLobbyPasswordRequest() {
    }

    @Override
    public UserLobbyRequestType getIndex() {
        return UserLobbyRequestType.CHANGE_LOBBY_PASSWORD_R;
    }

    public byte[] getPassword() {
        return password;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        //TODO: optimize
        out.writeObject(password);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        password = (byte[]) in.readObject();
    }
}

