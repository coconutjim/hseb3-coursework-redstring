package client_server.protocol.command_to_client.from_node.from_lobby;

import client_server.client.UserClient;
import client_server.User;
import client_server.protocol.command_to_client.ClientCommand;

import java.util.Map;

/***
 * Represents a setting user list command
 */
public class SetUserListCommand extends ClientCommand {

    /** User list (user and host) */
    private Map<User, String> users;

    /***
     * Constructor
     * @param users user list
     */
    public SetUserListCommand(Map<User, String> users) {
        if (users == null) {
            throw new NullPointerException("SetUserListCommand: users is null!");
        }
        this.users = users;
    }

    @Override
    public void execute(UserClient userClient) {
        super.execute(userClient);
        userClient.setUsers(users);
    }
}
