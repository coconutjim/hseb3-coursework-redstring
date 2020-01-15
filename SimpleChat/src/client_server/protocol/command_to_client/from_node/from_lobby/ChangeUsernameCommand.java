package client_server.protocol.command_to_client.from_node.from_lobby;

import client_server.client.UserClient;
import client_server.protocol.command_to_client.ClientCommand;

/***
 * Represents a changing username command
 */
public class ChangeUsernameCommand extends ClientCommand {

    /** New name */
    private String username;

    /***
     * Constructor
     * @param username new name
     */
    public ChangeUsernameCommand(String username) {
        if (username == null) {
            throw new NullPointerException("ChangeUsernameCommand: username is null!");
        }
        this.username = username;
    }

    @Override
    public void execute(UserClient userClient) {
        super.execute(userClient);
        userClient.changeUsername(username);
    }
}
