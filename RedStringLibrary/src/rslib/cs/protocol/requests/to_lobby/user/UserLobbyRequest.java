package rslib.cs.protocol.requests.to_lobby.user;

import rslib.cs.protocol.RedStringInfo;

/***
 * Represents a request to user lobby
 */
public interface UserLobbyRequest extends RedStringInfo {

    /** For better parsing */
    public static final long serialVersionUID = 7861684798675626L;

    /** User lobby request type */
    public enum UserLobbyRequestType {
        CHANGE_LOBBY_NAME_R,
        CHANGE_LOBBY_PASSWORD_R,
        CHANGE_USERNAME_R,
        CHANGE_USER_STATUS_R,
        DELEGATE_ROOT_R,
        GET_BAN_LIST_R,
        GET_USER_LIST_R,
        KICK_R,
        BAN_R,
        UNBAN_R,
    }

    public UserLobbyRequestType getIndex();
}
