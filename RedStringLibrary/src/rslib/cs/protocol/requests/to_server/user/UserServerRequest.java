package rslib.cs.protocol.requests.to_server.user;

import rslib.cs.protocol.RedStringInfo;

/***
 * Represents a request to user server
 */
public interface UserServerRequest extends RedStringInfo {

    /** For better parsing */
    public static final long serialVersionUID = 85434328465427493L;

    /** User server request type */
    public enum UserServerRequestType {
        CREATE_LOBBY_R,
        LOGIN_R,
        SEND_LOBBY_LIST_R
    }

    public UserServerRequestType getIndex();
}
