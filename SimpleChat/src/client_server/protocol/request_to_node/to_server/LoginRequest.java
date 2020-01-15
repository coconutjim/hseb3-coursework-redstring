package client_server.protocol.request_to_node.to_server;

import client_server.UserConnection;
import client_server.server.UserServer;
import client_server.LobbyInfo;

import java.nio.channels.SelectionKey;

/***
 * Login request
 */
public class LoginRequest extends UserServerRequest {

    /** User name */
    private String username;

    /** Info about server */
    private LobbyInfo lobbyInfo;

    /***
     * Constructor
     * @param username username
     * @param lobbyInfo info about server
     */
    public LoginRequest(String username, LobbyInfo lobbyInfo) {
        if (username == null) {
            throw new NullPointerException("LoginRequest: username is null!");
        }
        if (lobbyInfo == null) {
            throw new NullPointerException("LoginRequest: lobby info is null!");
        }
        this.username = username;
        this.lobbyInfo = lobbyInfo;
    }

    @Override
    public void handleRequest(UserServer userServer, SelectionKey key, UserConnection connection) {
        super.handleRequest(userServer, key, connection);
        userServer.login(key, connection, username, lobbyInfo);
    }
}
