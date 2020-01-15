package client_server.protocol.request_to_node.to_lobby;

import client_server.UserConnection;
import client_server.server.UserLobby;
import client_server.server.util.Status;

import java.nio.channels.SelectionKey;

/***
 * Represents changing user status request from user
 */
public class ChangeUserStatusRequest extends UserLobbyRequest {

    /** User to change status */
    private String username;

    /** New status*/
    private byte status;

    /***
     * Constructor
     * @param username user to change status
     * @param status new status
     */
    public ChangeUserStatusRequest(String username, byte status) {
        if (username == null) {
            throw new NullPointerException("ChangeUserStatusRequest: username is null!");
        }
        this.username = username;
        if (! (status >= Status.USER_STATUS_READONLY && status <= Status.USER_STATUS_MODERATOR)) {
            throw new IllegalArgumentException("ChangeUserStatusRequest: wrong status!");
        }
        this.status = status;
    }

    @Override
    public void handleRequest(UserLobby userLobby, SelectionKey sender, UserConnection connection) {
        super.handleRequest(userLobby, sender, connection);
        userLobby.changeUserStatus(username, status, sender, connection);
    }
}
