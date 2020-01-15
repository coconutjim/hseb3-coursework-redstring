package client_server.protocol.command_to_client.from_node.from_lobby;

import client_server.client.UserClient;
import client_server.protocol.command_to_client.ClientCommand;

/***
 * Represents a kicking command
 */
public class KickCommand extends ClientCommand {

    @Override
    public void execute(UserClient userClient) {
        super.execute(userClient);
        userClient.kick();
    }
}
