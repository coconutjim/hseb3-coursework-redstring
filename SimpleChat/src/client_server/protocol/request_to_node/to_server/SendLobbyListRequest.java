package client_server.protocol.request_to_node.to_server;

/***
 * Sends lobby list
 */
import client_server.UserConnection;
import client_server.server.UserServer;

import java.nio.channels.SelectionKey;

/***
 * Sends list of servers to the user
 */
public class SendLobbyListRequest extends UserServerRequest {

    @Override
    public void handleRequest(UserServer userServer, SelectionKey key, UserConnection connection) {
        super.handleRequest(userServer, key, connection);
        userServer.sendLobbyList(key, connection);
    }
}
