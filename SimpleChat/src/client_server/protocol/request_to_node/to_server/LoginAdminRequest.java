package client_server.protocol.request_to_node.to_server;

import client_server.UserConnection;
import client_server.protocol.request_to_node.to_server.AdminServerRequest;
import client_server.server.AdminServer;

import java.nio.channels.SelectionKey;

/***
 * Represents logging in to server API adapter
 */
public class LoginAdminRequest extends AdminServerRequest {

    /** User name */
    private String username;

    /** Password */
    private byte[] password;

    /***
     * Constructor
     * @param username username
     * @param password password
     */
    public LoginAdminRequest(String username, byte[] password) {
        if (username == null) {
            throw new NullPointerException("LoginToAPIRequest: username is null!");
        }
        if (password == null) {
            throw new NullPointerException("LoginToAPIRequest: password is null!");
        }
        this.username = username;
        this.password = password;
    }


    /***
     * Handles login request
     * @param adminServer API adapter
     * @param sender request sender
     * @param connection associated connection
     */
    public void handleRequest(AdminServer adminServer, SelectionKey sender, UserConnection connection) {
        super.handleRequest(adminServer, sender, connection);
        adminServer.login(sender, connection, username, password);
    }
}
