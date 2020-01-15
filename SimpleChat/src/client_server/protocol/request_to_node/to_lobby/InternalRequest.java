package client_server.protocol.request_to_node.to_lobby;

/***
 * Represents internal command to admin lobby
 */
public class InternalRequest extends AdminLobbyRequest {

    private String command;

    /***
     * Constructor
     * @param command command
     */
    public InternalRequest(String command) {
        if (command == null) {
            throw new NullPointerException("LogCommand: command is null!");
        }
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
