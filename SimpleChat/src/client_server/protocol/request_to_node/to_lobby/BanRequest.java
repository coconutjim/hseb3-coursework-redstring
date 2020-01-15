package client_server.protocol.request_to_node.to_lobby;

import client_server.UserConnection;
import client_server.server.UserLobby;

import java.nio.channels.SelectionKey;

/***
 * Represents ban request from user
 */
public class BanRequest extends UserLobbyRequest {

    /** User to ban */
    private String username;

    /***
     * Constructor
     * @param username user to ban
     */
    public BanRequest(String username) {
        if (username == null) {
            throw new NullPointerException("BanRequest: username is null!");
        }
        this.username = username;
    }

    @Override
    public void handleRequest(UserLobby userLobby, SelectionKey sender, UserConnection connection) {
        super.handleRequest(userLobby, sender, connection);
        userLobby.banUser(username, sender, connection);
    }
}

