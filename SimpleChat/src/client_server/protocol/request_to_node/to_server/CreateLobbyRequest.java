package client_server.protocol.request_to_node.to_server;

import client_server.UserConnection;
import client_server.server.UserServer;
import client_server.LobbyInfo;

import java.nio.channels.SelectionKey;

/***
 * Create lobby request
 */
public class CreateLobbyRequest extends UserServerRequest {

    /** Info about server */
    private final LobbyInfo lobbyInfo;

    /** User name */
    private final String username;

    /***
     * Constructor
     * @param lobbyInfo info of the server to be created
     * @param username command sender
     */
    public CreateLobbyRequest(String username, LobbyInfo lobbyInfo) {
        if (username == null) {
            throw new NullPointerException("CreateServerRequest: username is null!");
        }
        if (lobbyInfo == null) {
            throw new NullPointerException("CreateServerRequest: serverInfo is null!");
        }
        this.username = username;
        this.lobbyInfo = lobbyInfo;
    }

    @Override
    public void handleRequest(UserServer userServer, SelectionKey key, UserConnection connection) {
        super.handleRequest(userServer, key, connection);
        userServer.createLobby(key, connection, username, lobbyInfo);
    }
}
