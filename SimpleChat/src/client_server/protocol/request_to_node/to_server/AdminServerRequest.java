package client_server.protocol.request_to_node.to_server;

import client_server.UserConnection;
import client_server.protocol.ChatInfo;
import client_server.server.AdminServer;

import java.nio.channels.SelectionKey;

/***
 * Represents a request to admin server
 */
public abstract class AdminServerRequest implements ChatInfo {

    /***
     * Handles request on server
     * @param adminServer link to server
     * @param sender command sender
     * @param connection associated connection
     */
    public void handleRequest(AdminServer adminServer, SelectionKey sender, UserConnection connection) {
        if (adminServer == null) {
            throw new NullPointerException("AdminServerRequest: adminServer is null!");
        }
        if (sender == null) {
            throw new NullPointerException("AdminServerRequest: key is null!");
        }
        if (connection == null) {
            throw new NullPointerException("AdminServerRequest: connection is null!");
        }
    }
}
