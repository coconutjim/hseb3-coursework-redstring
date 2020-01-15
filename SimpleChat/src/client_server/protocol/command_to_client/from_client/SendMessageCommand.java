package client_server.protocol.command_to_client.from_client;

import client_server.client.UserClient;
import client_server.protocol.command_to_client.ClientCommand;

/***
 * Represents a new chat message command
 */
public class SendMessageCommand extends ClientCommand {

    /** Username */
    private String username;

    /** Message to send */
    private final String message;

    /***
     * Constructor
     * @param message message to sent
     * @param username username
     */
    public SendMessageCommand(String username, String message) {
        if (message == null) {
            throw new NullPointerException("SendMessageCommand: message is null!");
        }
        if (username == null) {
            throw new NullPointerException("SendMessageCommand: username is null!");
        }
        this.username = username;
        this.message = message;
    }

    @Override
    public void execute(UserClient userClient) {
        super.execute(userClient);
        userClient.messageReceived(username, message);
    }
}