package client_server.protocol.command_to_client.from_node.from_lobby;

import client_server.client.UserClient;
import client_server.protocol.command_to_client.ClientCommand;
import client_server.server.util.Status;

/***
 * Represents a changing user status command
 */
public class ChangeUserStatusCommand extends ClientCommand {

    /** New status */
    private byte status;

    /***
     * Constructor
     * @param status new status
     */
    public ChangeUserStatusCommand(byte status) {
        if (status < Status.USER_STATUS_READONLY && status > Status.USER_STATUS_MODERATOR) {
            throw new IllegalArgumentException("ChangeUserStatusCommand: illegal status!");
        }
        this.status = status;
    }

    @Override
    public void execute(UserClient userClient) {
        super.execute(userClient);
        userClient.changeUserStatus(status);
    }
}
