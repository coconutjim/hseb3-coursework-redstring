package client_server.protocol.request_to_node.to_lobby;

import client_server.UserConnection;
import client_server.server.UserLobby;

import java.nio.channels.SelectionKey;

/**
 * Represents a kick request from user
 */
public class KickRequest extends UserLobbyRequest {

    /** User to kick */
    private String username;

    /***
     * Constructor
     * @param username user to kick
     */
    public KickRequest(String username) {
        if (username == null) {
            throw new NullPointerException("KickRequest: username is null!");
        }
        this.username = username;
    }

    @Override
    public void handleRequest(UserLobby userLobby, SelectionKey sender, UserConnection connection) {
        super.handleRequest(userLobby, sender, connection);
        userLobby.kickUser(username, sender, connection);
    }
}
