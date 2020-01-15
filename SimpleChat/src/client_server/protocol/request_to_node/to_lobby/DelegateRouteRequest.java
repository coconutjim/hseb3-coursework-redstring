package client_server.protocol.request_to_node.to_lobby;

import client_server.UserConnection;
import client_server.server.UserLobby;

import java.nio.channels.SelectionKey;

/***
 * Represents delegating route status request from user
 */
public class DelegateRouteRequest extends UserLobbyRequest {

    /** User to make route */
    private String username;

    /***
     * Constructor
     * @param username username to make route
     */
    public DelegateRouteRequest(String username) {
        if (username == null) {
            throw new NullPointerException("DelegateRouteRequest: username is null!");
        }
        this.username = username;
    }

    @Override
    public void handleRequest(UserLobby userServer, SelectionKey sender, UserConnection connection) {
        super.handleRequest(userServer, sender, connection);
        userServer.delegateRoute(username, sender, connection);
    }
}
