package client_server.protocol.request_to_node.to_lobby;

import client_server.UserConnection;
import client_server.server.UserLobby;

import java.nio.channels.SelectionKey;

/***
 * Represents changing username request from user
 */
public class ChangeUsernameRequest extends UserLobbyRequest {

    /** New username */
    private String username;

    /***
     * Constructor
     * @param username new lobby name
     */
    public ChangeUsernameRequest(String username) {
        if (username == null) {
            throw new NullPointerException("ChangeUsernameRequest: username is null!");
        }
        this.username = username;
    }

    @Override
    public void handleRequest(UserLobby userLobby, SelectionKey sender, UserConnection connection) {
        super.handleRequest(userLobby, sender, connection);
        userLobby.changeUsername(username, sender, connection);
    }
}

