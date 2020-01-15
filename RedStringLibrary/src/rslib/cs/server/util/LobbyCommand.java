package rslib.cs.server.util;

import rslib.cs.common.CommandInfo;

/***
 * Represents a lobby command
 */
public class LobbyCommand {

    /** Command */
    private CommandInfo commandInfo;

    /** Command receiver */
    private CommandReceiver commandReceiver;

    /***
     * Constructor
     * @param commandInfo command
     * @param commandReceiver command receiver
     */
    public LobbyCommand(CommandInfo commandInfo, CommandReceiver commandReceiver) {
        if (commandInfo == null) {
            throw new IllegalArgumentException("LobbyCommand: " +
                    "commandInfo is null!");
        }
        if (commandReceiver == null) {
            throw new IllegalArgumentException("LobbyCommand: " +
                    "commandReceiver is null!");
        }
        this.commandInfo = commandInfo;
        this.commandReceiver = commandReceiver;
    }

    public CommandInfo getCommandInfo() {
        return commandInfo;
    }

    public CommandReceiver getCommandReceiver() {
        return commandReceiver;
    }
}
