package client_server.protocol.request_to_node.to_lobby;

import client_server.UserConnection;
import client_server.protocol.ChatInfo;
import client_server.server.UserLobby;

import java.nio.channels.SelectionKey;

/***
 * Represents a request to user lobby
 */
public abstract class UserLobbyRequest implements ChatInfo {

    /***
     * Handles request on lobby
     * @param userLobby link to lobby
     * @param sender command sender
     * @param connection associated connection
     */
    public void handleRequest(UserLobby userLobby, SelectionKey sender, UserConnection connection) {
        if (userLobby == null) {
            throw new NullPointerException("UserLobbyRequest: userLobby is null!");
        }
        if (sender == null) {
            throw new NullPointerException("UserLobbyRequest: sender is null!");
        }
        if (connection == null) {
            throw new NullPointerException("UserLobbyRequest: connection is null!");
        }
    }
}
