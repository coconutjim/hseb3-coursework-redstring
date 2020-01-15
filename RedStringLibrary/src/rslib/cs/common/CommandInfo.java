package rslib.cs.common;

import rslib.cs.protocol.ProtocolConstants;
import rslib.cs.protocol.RedStringInfo;
import rslib.util.DataManagement;

import java.io.IOException;
import java.nio.ByteBuffer;

/***
 * Holds info about command to write to node
 */
public class CommandInfo {

    /** Command itself */
    private ByteBuffer command;

    /** Command type */
    private byte commandType;

    /** Command class */
    private String commandClass;

    /***
     * Constructor
     * @param command command itself
     * @param commandType command type
     */
    public CommandInfo(ByteBuffer command, byte commandType, String commandClass) {
        if (command == null) {
            throw new NullPointerException("CommandInfo: command is null!");
        }
        if (! (commandType >= ProtocolConstants.MIN_INDEX && commandType <= ProtocolConstants.MAX_INDEX)) {
            throw new IllegalArgumentException("CommandInfo: wrong commandType!");
        }
        this.command = command;
        this.commandType = commandType;
        this.commandClass = commandClass;
    }

    /***
     * Constructor
     * @param command command
     * @param commandType command type
     * @throws IOException if something went wrong
     */
    public CommandInfo(RedStringInfo command, byte commandType) throws IOException {
        if (command == null) {
            throw new NullPointerException("CommandInfo: command is null!");
        }
        if (!(commandType >= ProtocolConstants.MIN_INDEX && commandType <= ProtocolConstants.MAX_INDEX)) {
            throw new IllegalArgumentException("CommandInfo: wrong commandType!");
        }
        byte[] serialized = DataManagement.serialize(command);
        int length = serialized.length;
        ByteBuffer buffer;
        try {
            buffer = ByteBuffer.allocate(length);
        }
        catch (OutOfMemoryError e) {
            throw new IOException(e.getMessage());
        }
        buffer.put(serialized);
        buffer.flip();
        this.command = buffer;
        this.commandType = commandType;
        this.commandClass = command.getClass().getSimpleName();
    }

    public ByteBuffer getCommand() {
        return command;
    }

    public byte getCommandType() {
        return commandType;
    }

    public String getCommandClass() {
        return commandClass;
    }
}
