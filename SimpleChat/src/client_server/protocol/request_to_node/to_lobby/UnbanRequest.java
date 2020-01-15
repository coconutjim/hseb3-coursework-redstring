package client_server.protocol.request_to_node.to_lobby;

import client_server.UserConnection;
import client_server.server.UserLobby;

import java.nio.channels.SelectionKey;

/***
 * Represents an unban request from user
 */
public class UnbanRequest extends UserLobbyRequest{

    /** User to unban */
    private String username;

    /***
     * Constructor
     * @param username user to unban
     */
    public UnbanRequest(String username) {
        if (username == null) {
            throw new NullPointerException("UnbanRequest: username is null!");
        }
        this.username = username;
    }

    @Override
    public void handleRequest(UserLobby userLobby, SelectionKey sender, UserConnection connection) {
        super.handleRequest(userLobby, sender, connection);
        userLobby.unbanUser(username, sender, connection);
    }
}
