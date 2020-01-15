package rslib.cs.protocol.requests.to_server.admin;

import rslib.cs.protocol.RedStringInfo;

/***
 * Represents a request to admin server
 */
public interface AdminServerRequest extends RedStringInfo {

    /** For better parsing */
    public static final long serialVersionUID = 23422899934785L;

    /** Admin server request type */
    public enum AdminServerRequestType {
        LOGIN_R,
    }

    public AdminServerRequestType getIndex();
}
