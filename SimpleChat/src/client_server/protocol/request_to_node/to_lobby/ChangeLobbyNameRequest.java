package client_server.protocol.request_to_node.to_lobby;

import client_server.UserConnection;
import client_server.server.UserLobby;

import java.nio.channels.SelectionKey;

/***
 * Represents changing lobby name request from user
 */
public class ChangeLobbyNameRequest extends UserLobbyRequest {

    /** New lobby name */
    private String lobbyName;

    /***
     * Constructor
     * @param lobbyName new lobby name
     */
    public ChangeLobbyNameRequest(String lobbyName) {
        if (lobbyName == null) {
            throw new NullPointerException("ChangeLobbyNameRequest: lobbyName is null!");
        }
        this.lobbyName = lobbyName;
    }

    @Override
    public void handleRequest(UserLobby userLobby, SelectionKey sender, UserConnection connection) {
        super.handleRequest(userLobby, sender, connection);
        userLobby.changeLobbyName(lobbyName, connection);
    }
}
