package rslib.cs.protocol.requests.to_lobby.admin;

import rslib.cs.protocol.RedStringInfo;

/***
 * Represents a request to admin lobby
 */
public interface AdminLobbyRequest extends RedStringInfo {

    /** For better parsing */
    public static final long serialVersionUID = 73647267834657L;

    /** User lobby request type */
    public enum UserLobbyRequestType {
        INTERNAL_R
    }

    public UserLobbyRequestType getIndex();
}
