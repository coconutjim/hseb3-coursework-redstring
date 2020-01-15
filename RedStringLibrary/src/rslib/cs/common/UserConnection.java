package rslib.cs.common;

import rslib.cs.protocol.ProtocolConstants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.AbstractQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/***
 * Represents a user connection the client or server
 */
public class UserConnection {

    /** Associated user */
    private final User user;

    /** Socket channel of connection */
    private final SocketChannel socketChannel;

    /** Associated key in selector */
    private SelectionKey key;

    /** Commands to write */
    private final AbstractQueue<CommandInfo> writeCommands;

    /** Read command type */
    private byte readCommandType;

    /** Write command type */
    private byte writeCommandType;

    /** Write command class */
    private String writeCommandClass;

    /** Read command length */
    private int readCommandLength;

    /** Read command length */
    private int writeCommandLength;

    /** Counter for already read bytes */
    private int bytesRead;

    /** Counter for already read bytes */
    private int bytesWritten;

    /** Buffer to read the data */
    private ByteBuffer readBuffer;

    /** Buffer to write the data */
    private ByteBuffer writeBuffer;

    /** Unique id */
    private int id;

    /***
     * Constructor
     * @param user associated user (can be null if connection is not still registered)
     * @param socketChannel associated channel
     */
    public UserConnection(User user, SocketChannel socketChannel) {
        id = -1;
        this.user = user;
        this.socketChannel = socketChannel;
        writeCommands = new ConcurrentLinkedQueue<>(); // concurrency
        readCommandLength = -1;
        writeCommandLength = -1;
        bytesRead = 0;
        bytesWritten = 0;
        readCommandType = -1;
        writeCommandType = -1;
    }

    /***
     * Defines if channel is free to write and read info
     * @return true if channel id free to write and read info, false otherwise
     */
    public boolean isFree() {
        return writeCommandLength == -1 && readCommandLength == -1;
    }

    /***
     * Tries to read command. Can be not completed for several times
     * @return completely read buffered command, null otherwise
     * @throws IOException if something went wrong
     * @throws IllegalStateException if received data is wrong
     */
    public CommandInfo readCommand() throws IOException,
            IllegalStateException {
        if (readCommandLength == -1) {
            // if message is being processed first time, read and set meta data
            int metaDataLength = ProtocolConstants.COMMAND_LENGTH + ProtocolConstants.INFO_INDEX_LENGTH;
            ByteBuffer metaDataBuffer = ByteBuffer.allocate(metaDataLength);
            int read = socketChannel.read(metaDataBuffer);
            if (read == -1) {
                clearReadData();
                throw new IOException("Could not read meta data!");
            }
            if (read != metaDataLength) {
                return null;
            }
            metaDataBuffer.flip();
            int length = metaDataBuffer.getInt();
            byte type = metaDataBuffer.get();
            if (! (type >= ProtocolConstants.MIN_INDEX && type <= ProtocolConstants.MAX_INDEX)) { // data validation
                clearReadData();
                throw new IllegalStateException();
            }
            readCommandLength = length;
            readCommandType = type;
            try {
                readBuffer = ByteBuffer.allocate(readCommandLength);
            }
            catch (OutOfMemoryError error) {
                clearReadData();
                throw new IllegalStateException();
            }
        }
        int read = socketChannel.read(readBuffer);
        if (read == -1) {
            clearReadData();
            throw new IOException("Could not read command content!");
        }
        bytesRead += read;
        if (bytesRead == readCommandLength) {
            // if all data has been read
            readBuffer.flip();
            ByteBuffer result = readBuffer;
            byte commandType = readCommandType;
            clearReadData();
            return new CommandInfo(result, commandType, null);
        }
        return null;
    }

    /***
     * Clears reading data
     */
    private void clearReadData() {
        readBuffer = null;
        readCommandLength = -1;
        bytesRead = 0;
        readCommandType = -1;
    }

    /***
     * Tries to write command. Can be not completed for several times
     * @return true if was written completely, false otherwise
     * @throws IOException if something went wrong
     */
    public boolean writeCommand() throws IOException {
        if (bytesWritten == 0) {
            // if the writing process occurs first time, write meta data
            int metaDataLength = ProtocolConstants.COMMAND_LENGTH +
                    ProtocolConstants.INFO_INDEX_LENGTH;
            ByteBuffer metaDataBuffer = ByteBuffer.allocate(metaDataLength);
            metaDataBuffer.putInt(writeCommandLength);
            metaDataBuffer.put(writeCommandType);
            metaDataBuffer.flip();
            int written = socketChannel.write(metaDataBuffer);
            if (written == -1) {
                clearWriteData();
                throw new IOException("Could not write meta data (command " + writeCommandClass +  ")!");
            }
            if (written != metaDataLength) {
                return false;
            }
        }
        int position = writeBuffer.position(); // remembering buffer position
        writeBuffer.position(bytesWritten); // set it to the right point
        int written = socketChannel.write(writeBuffer);
        if (written == -1) {
            clearWriteData();
            throw new IOException("Could not write command content (command " + writeCommandClass +  ")!");
        }
        writeBuffer.position(position); // returning to old position
        bytesWritten += written;
        // checking if all info was written
        if (bytesWritten == writeCommandLength) {
            key.interestOps(SelectionKey.OP_READ);
            return true;
        }
        return false;
    }

    /***
     * Clears writing data
     */
    public void clearWriteData() {
        writeBuffer = null;
        bytesWritten = 0;
        writeCommandLength = -1;
        writeCommandType = -1;
        writeCommandClass = null;
    }

    /***
     * Registers this connection on a selector
     * @param selector selector
     * @return selection key
     * @throws IOException if something went wrong
     */
    public SelectionKey registerOnSelector(Selector selector) throws IOException {
        key = socketChannel.register(selector, SelectionKey.OP_READ);
        return key;
    }

    /***
     * Links command to write to the connection
     * @param command command itself
     * @param commandType command type
     */
    private void linkCommandToConnection(ByteBuffer command, byte commandType, String commandClass) {
        writeBuffer = command;
        writeCommandLength = command.capacity();
        writeCommandType = commandType;
        writeCommandClass = commandClass;
        key.interestOps(SelectionKey.OP_WRITE);
    }

    /***
     * Links command to write to the connection
     * @param commandInfo command to write
     */
    private void linkCommandToConnection(CommandInfo commandInfo) {
        linkCommandToConnection(commandInfo.getCommand(), commandInfo.getCommandType(), commandInfo.getCommandClass());
    }

    /***
     * Adds new command to write
     */
    private void linkNewCommandToWrite() {
        CommandInfo commandInfo = writeCommands.poll();
        if (commandInfo != null) {
            linkCommandToConnection(commandInfo);
        }
    }

    /***
     * Checks if there are anything to write
     * Checks if channel can write right now
     * Adds new command to write if ok
     */
    public void checkWriteCommands() {
        if (! writeCommands.isEmpty()) {
            if (isFree()) {
                linkNewCommandToWrite();
            }
        }
    }

    /***
     * Adds command to write
     * @param commandInfo command info
     */
    public void addCommandToWrite(CommandInfo commandInfo) {
        if (isFree()) { // if channel is ready to write
            linkCommandToConnection(commandInfo.getCommand(),
                    commandInfo.getCommandType(), commandInfo.getCommandClass());
        }
        else {
            writeCommands.add(commandInfo);
        }
    }

    @Override
    public String toString() {
        if (user == null && socketChannel == null) {
            return "unrecognized connection";
        }
        if (user == null) {
            return "host " + socketChannel.socket().getInetAddress().getHostAddress();
        }
        return "User " + user.getUsername() + " (" + user.getStatus().toString() + ")";

    }

    public void setUsername(String username) {
        user.setUsername(username);
    }

    public void setStatus(Status status) {
        user.setStatus(status);
    }

    public void setWriteCommandClass(String writeCommandClass) {
        this.writeCommandClass = writeCommandClass;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWriteCommandClass() {
        return writeCommandClass;
    }

    public SelectionKey getKey() {
        return key;
    }

    public String getUsername() {
        return user.getUsername();
    }

    public Status getStatus() {
        return user.getStatus();
    }

    public String getHost() {
        return socketChannel.socket().getInetAddress().getHostAddress();
    }

    public User getUser() {
        return user;
    }

    public int getReadCommandLength() {
        return readCommandLength;
    }

    public int getWriteCommandLength() {
        return writeCommandLength;
    }

    public int getId() {
        return id;
    }

    public AbstractQueue<CommandInfo> getWriteCommands() {
        return writeCommands;
    }
}
