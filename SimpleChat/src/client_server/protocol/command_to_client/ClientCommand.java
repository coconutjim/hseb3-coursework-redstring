package client_server.protocol.command_to_client;

import client_server.client.UserClient;
import client_server.protocol.ChatInfo;

/***
 * Represents a command to clients
 */
public abstract class ClientCommand implements ChatInfo {

    /***
     * Executes command
     * @param userClient the client
     */
    public void execute(UserClient userClient) {
        if (userClient == null) {
            throw new NullPointerException("ClientCommand: client is null!");
        }
    }
}
