package client_server.protocol.command_to_client.from_node.from_lobby;

import client_server.client.UserClient;
import client_server.protocol.command_to_client.ClientCommand;

import java.util.Map;

/***
 * Represents a setting ban list command
 */
public class SetBanListCommand extends ClientCommand {

    /** User list */
    private Map<String, String> users;

    /***
     * Constructor
     * @param users ban list
     */
    public SetBanListCommand(Map<String, String> users) {
        if (users == null) {
            throw new NullPointerException("SetBanListCommand: users is null!");
        }
        this.users = users;
    }

    @Override
    public void execute(UserClient userClient) {
        super.execute(userClient);
        userClient.setBanList(users);
    }
}
