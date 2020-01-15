package client_server;

import java.io.Serializable;

/***
 * Holds info about user
 */
public class User implements Serializable {

    /** User name */
    private String username;

    /** User status */
    private byte status;

    /***
     * Constructor.
     * @param username username;
     * @param status user status
     */
    public User(String username, byte status) {
        if (username == null) {
            throw new NullPointerException("UserConnection: username is null!");
        }
        this.username = username;
        this.status = status;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public byte getStatus() {
        return status;
    }
}
