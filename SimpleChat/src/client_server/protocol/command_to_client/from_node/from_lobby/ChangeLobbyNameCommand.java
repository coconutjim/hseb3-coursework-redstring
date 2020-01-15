package client_server.protocol.command_to_client.from_node.from_lobby;

import client_server.client.UserClient;
import client_server.protocol.command_to_client.ClientCommand;

/***
 * Represents a changing lobby name command
 */
public class ChangeLobbyNameCommand extends ClientCommand {

    /** New name */
    private String lobbyName;

    /***
     * Constructor
     * @param lobbyName new name
     */
    public ChangeLobbyNameCommand(String lobbyName) {
        if (lobbyName == null) {
            throw new NullPointerException("ChangeLobbyNameCommand: lobbyName is null!");
        }
        this.lobbyName = lobbyName;
    }

    @Override
    public void execute(UserClient userClient) {
        super.execute(userClient);
        userClient.changeLobbyName(lobbyName);
    }
}
