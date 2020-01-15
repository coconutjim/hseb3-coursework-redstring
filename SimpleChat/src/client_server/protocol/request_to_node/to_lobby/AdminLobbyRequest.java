package client_server.protocol.request_to_node.to_lobby;

import client_server.UserConnection;
import client_server.protocol.ChatInfo;
import client_server.server.AdminLobby;

import java.nio.channels.SelectionKey;

/***
 * Represents a request to admin lobby
 */
public abstract class AdminLobbyRequest implements ChatInfo {
    /***
     * Handles request on lobby
     * @param adminLobby link to lobby
     * @param sender command sender
     * @param connection associated connection
     */
    public void handleRequest(AdminLobby adminLobby, SelectionKey sender, UserConnection connection) {
        if (adminLobby == null) {
            throw new NullPointerException("UserLobbyRequest: adminLobby is null!");
        }
        if (sender == null) {
            throw new NullPointerException("UserLobbyRequest: sender is null!");
        }
        if (connection == null) {
            throw new NullPointerException("UserLobbyRequest: connection is null!");
        }
    }
}
