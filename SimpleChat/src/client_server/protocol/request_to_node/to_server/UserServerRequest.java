package client_server.protocol.request_to_node.to_server;


import client_server.UserConnection;
import client_server.protocol.ChatInfo;
import client_server.server.UserServer;

import java.nio.channels.SelectionKey;

/***
 * Represents a request to user server
 */
public abstract class UserServerRequest implements ChatInfo {

    /***
     * Handles request on server
     * @param userServer link to server
     * @param sender command sender
     * @param connection associated connection
     */
    public void handleRequest(UserServer userServer, SelectionKey sender, UserConnection connection) {
        if (userServer == null) {
            throw new NullPointerException("UserServerRequest: userServer is null!");
        }
        if (sender == null) {
            throw new NullPointerException("UserServerRequest: key is null!");
        }
        if (connection == null) {
            throw new NullPointerException("UserServerRequest: connection is null!");
        }
    }
}
