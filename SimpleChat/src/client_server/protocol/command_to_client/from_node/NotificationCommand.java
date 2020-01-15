package client_server.protocol.command_to_client.from_node;

import client_server.client.UserClient;
import client_server.protocol.command_to_client.ClientCommand;

/***
 * Represents a notification
 */
public class NotificationCommand extends ClientCommand {

    /** Notification message */
    private String message;

    /***
     * Constructor
     * @param message message itself
     */
    public NotificationCommand(String message) {
        if (message == null) {
            throw new NullPointerException("NotificationCommand: message is null");
        }
        this.message = message;
    }

    @Override
    public void execute(UserClient userClient) {
        super.execute(userClient);
        userClient.notificationReceived(message);
    }
}
