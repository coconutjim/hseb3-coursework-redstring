package rslib.cs.protocol.requests.to_lobby.user;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents getting user list request from user
 */
public class GetUserListRequest implements UserLobbyRequest {

    /** For better parsing */
    public static final long serialVersionUID = 823558260376757L;

    @Override
    public UserLobbyRequestType getIndex() {
        return UserLobbyRequestType.GET_USER_LIST_R;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
