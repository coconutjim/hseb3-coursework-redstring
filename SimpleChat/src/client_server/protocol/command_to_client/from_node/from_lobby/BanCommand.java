package client_server.protocol.command_to_client.from_node.from_lobby;

import client_server.client.UserClient;
import client_server.protocol.command_to_client.ClientCommand;

/***
 * Represents a banning command
 */
public class BanCommand extends ClientCommand {

    @Override
    public void execute(UserClient userClient) {
        super.execute(userClient);
        userClient.ban();
    }
}
