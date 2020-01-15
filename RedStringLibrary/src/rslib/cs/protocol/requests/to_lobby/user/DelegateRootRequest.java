package rslib.cs.protocol.requests.to_lobby.user;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents delegating root status request from user
 */
public class DelegateRootRequest implements UserLobbyRequest {

    /** For better parsing */
    public static final long serialVersionUID = 54261116265165749L;

    /** User to make route */
    private String username;

    /***
     * Constructor
     * @param username username to make route
     */
    public DelegateRootRequest(String username) {
        if (username == null) {
            throw new IllegalArgumentException("DelegateRouteRequest: username is null!");
        }
        this.username = username;
    }

    /***
     * Constructor for externalization
     */
    public DelegateRootRequest() {
    }

    @Override
    public UserLobbyRequestType getIndex() {
        return UserLobbyRequestType.DELEGATE_ROOT_R;
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
