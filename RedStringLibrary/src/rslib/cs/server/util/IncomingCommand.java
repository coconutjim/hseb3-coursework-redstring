package rslib.cs.server.util;

import rslib.cs.common.CommandInfo;
import rslib.cs.common.UserConnection;

/***
 * Represents incoming command - command and command sender
 */
public class IncomingCommand {

    /** Command */
    private CommandInfo commandInfo;

    /** Command sender */
    private UserConnection connection;

    /***
     * Constructor
     * @param commandInfo command
     * @param connection command sender
     */
    public IncomingCommand(CommandInfo commandInfo, UserConnection connection) {
        if (commandInfo == null) {
            throw new IllegalArgumentException("IncomingCommand: commandInfo is null!");
        }
        if (connection == null) {
            throw new IllegalArgumentException("IncomingCommand: connection is null!");
        }
        this.commandInfo = commandInfo;
        this.connection = connection;
    }

    public CommandInfo getCommandInfo() {
        return commandInfo;
    }

    public UserConnection getConnection() {
        return connection;
    }
}
