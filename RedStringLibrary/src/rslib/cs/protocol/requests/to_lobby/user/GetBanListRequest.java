package rslib.cs.protocol.requests.to_lobby.user;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents getting ban list request from user
 */
public class GetBanListRequest implements UserLobbyRequest {

    /** For better parsing */
    public static final long serialVersionUID = 82294760286491L;

    @Override
    public UserLobbyRequestType getIndex() {
        return UserLobbyRequestType.GET_BAN_LIST_R;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
