package rslib.cs.protocol.requests.to_lobby.user;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents an unban request from user
 */
public class UnbanRequest implements UserLobbyRequest{

    /** For better parsing */
    public static final long serialVersionUID = 118467675656522324L;

    /** User to unban */
    private String username;

    /***
     * Constructor
     * @param username user to unban
     */
    public UnbanRequest(String username) {
        if (username == null) {
            throw new IllegalArgumentException("UnbanRequest: username is null!");
        }
        this.username = username;
    }

    /***
     * Constructor for externalization
     */
    public UnbanRequest() {
    }

    @Override
    public UserLobbyRequestType getIndex() {
        return UserLobbyRequestType.UNBAN_R;
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
