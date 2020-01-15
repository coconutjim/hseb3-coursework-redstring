package rslib.cs.protocol.requests.to_server.user;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Sends list of servers to the user
 */
public class SendLobbyListRequest implements UserServerRequest {

    /** For better parsing */
    public static final long serialVersionUID = 834992456395654267L;

    @Override
    public UserServerRequestType getIndex() {
        return UserServerRequestType.SEND_LOBBY_LIST_R;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
