package client_server.protocol.command_to_server_gui;

import client_server.client.AdminClient;
import client_server.protocol.ChatInfo;

/***
 * Log command to server GUI
 */
public class LogCommand implements ChatInfo {

    /** Log message */
    private String message;

    /***
     * Constructor
     * @param message log message
     */
    public LogCommand(String message) {
        if (message == null) {
            throw new NullPointerException("LogCommand: message is null!");
        }
        this.message = message;
    }

    /***
     * Executes logging command
     * @param client client
     */
    public void execute(AdminClient client) {
        client.log(message);
    }
}
