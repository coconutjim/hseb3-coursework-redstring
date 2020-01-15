package client_server.protocol.request_to_node.to_lobby;

import client_server.UserConnection;
import client_server.server.UserLobby;
import util.DataManagement;

import java.nio.channels.SelectionKey;

/***
 * Represents changing lobby password request from user
 */
public class ChangeLobbyPasswordRequest extends UserLobbyRequest {

    /** New lobby password */
    private byte[] password;

    /***
     * Constructor
     * @param password new lobby password
     */
    public ChangeLobbyPasswordRequest(String password) {
        if (password == null) {
            throw new NullPointerException("ChangeLobbyPasswordCommand: lobbyName is null!");
        }
        this.password = DataManagement.toHashMD5(password);
    }

    @Override
    public void handleRequest(UserLobby userLobby, SelectionKey sender, UserConnection connection) {
        super.handleRequest(userLobby, sender, connection);
        userLobby.changeLobbyPassword(password, connection);
    }
}

