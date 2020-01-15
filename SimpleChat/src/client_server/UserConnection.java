package client_server;

import client_server.protocol.ChatInfo;
import client_server.protocol.ProtocolConstants;
import client_server.server.util.Status;
import util.DataManagement;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Represents a user connection on the lobby or server.
 */
public class UserConnection {

    /** Associated user */
    private User user;

    /** Associated key in selector */
    private SelectionKey key;

    /** Socket channel of connection */
    private SocketChannel socketChannel;

    /** Read command length */
    private int readCommandLength;

    /** Read command length */
    private int writeCommandLength;

    /** Read command type */
    private byte readCommandType;

    /** Write command type (for logs)*/
    private String writeCommandType;

    /** Counter for already read bytes */
    private int bytesRead;

    /** Counter for already read bytes */
    private int bytesWritten;

    /** Buffer to read the data */
    private ByteBuffer readBuffer;

    /** Buffer to write the data */
    private ByteBuffer writeBuffer;

    /** Commands to write */
    private Map<ByteBuffer, String> writeCommands;

    /***
     * Constructor
     * @param user associated user (can be null if connection is not still registered)
     */
    public UserConnection(User user, SocketChannel socketChannel) {
        this.user = user;
        this.socketChannel = socketChannel;
        readCommandLength = -1;
        writeCommandLength = -1;
        readCommandType = -1;
        bytesRead = 0;
        bytesWritten = 0;
        writeCommands = new ConcurrentHashMap<ByteBuffer, String>(); // concurrency
    }

    private boolean canLink() {
        return writeCommandLength == -1 && readCommandLength == -1;
    }

    /***
     * Links command to write to the connection
     * @param buffer buffered command
     * @param commandType command type
     */
    private void linkCommandToConnection(ByteBuffer buffer, String commandType) {
        writeBuffer = buffer;
        writeCommandLength = buffer.capacity();
        writeCommandType = commandType;
        key.interestOps(SelectionKey.OP_WRITE);
    }

    /***
     * Adds command to write (links it if the channel is ready to write)
     * @param command command
     * @throws IOException if something went wrong
     */
    public void addCommandToWrite(ChatInfo command)
            throws IOException {
        byte[] serialized = DataManagement.serialize(command);
        int length = serialized.length;
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(serialized);
        buffer.flip();
        addCommandToWrite(buffer, command.getClass().getSimpleName());
    }

    /***
     * Adds command to write (links it if the channel is ready to write)
     * @param buffer buffered command
     */
    public void addCommandToWrite(ByteBuffer buffer) {
        addCommandToWrite(buffer, "Client command");
    }

    /***
     * Adds command to write (links it if the channel is ready to write)
     * @param buffer buffered command
     * @param commandType command type
     */
    private void addCommandToWrite(ByteBuffer buffer, String commandType) {
        if (canLink()) { // if channel is ready to write
            linkCommandToConnection(buffer, commandType);
        }
        else {
            writeCommands.put(buffer, commandType);
        }
    }

    /***
     * Adds new command to write
     */
    private void addNewCommandToWrite() {
        Set<Map.Entry<ByteBuffer, String>> entries = writeCommands.entrySet();
        ByteBuffer buffer;
        for (Map.Entry<ByteBuffer, String> entry : entries) {
            buffer = entry.getKey();
            linkCommandToConnection(buffer, entry.getValue());
            writeCommands.remove(buffer);
            break;
        }
    }

    /***
     * Checks if there are anything to write
     * Checks if channel can write right now
     */
    public void checkWriteCommands() {
        if (! writeCommands.isEmpty()) {
            if (canLink()) {
                addNewCommandToWrite();
            }
        }
    }

    /***
     * Tries to read command from client. Can be not completed for several times
     * Clears fields after reading (except command type)
     * @return completely read buffered command, null otherwise
     * @throws java.io.IOException if something went wrong
     * @throws IllegalStateException if received data is wrong
     */
    public ByteBuffer readCommandFromClient() throws IOException,
            IllegalStateException {
        //TODO: check comparison
        if (readCommandLength == -1) {
            // if message is being processed first time, read and set meta data
            int metaDataLength = ProtocolConstants.COMMAND_LENGTH + ProtocolConstants.INFO_INDEX_LENGTH;
            ByteBuffer metaDataBuffer = ByteBuffer.allocate(metaDataLength);
            int read = socketChannel.read(metaDataBuffer);
            if (read != metaDataLength) {
                throw new IOException("Could not read meta data!");
            }
            metaDataBuffer.flip();
            int length = metaDataBuffer.getInt();
            byte type = metaDataBuffer.get();
            if (type != ProtocolConstants.CLIENT_COMMAND_INDEX &&
                    type != ProtocolConstants.SERVER_COMMAND_INDEX &&
                    type != ProtocolConstants.LOBBY_COMMAND_INDEX) { // data validation
                throw new IllegalStateException();
            }
            readCommandLength = length;
            readCommandType = type;
            //TODO: mb handle OutOfMemory
            readBuffer = ByteBuffer.allocate(readCommandLength);
        }
        int read = socketChannel.read(readBuffer);
        if (read == 0 || read == -1) {
            throw new IOException("Could not read command content!");
        }
        bytesRead += read;
        if (bytesRead == readCommandLength) {
            // if all data has been read
            readBuffer.flip();
            ByteBuffer result = readBuffer;
            readBuffer = null;
            readCommandLength = -1;
            bytesRead = 0;
            return result;
        }
        return null;
    }

    /***
     * Tries to write command to client. Can be not completed for several times
     * Clears fields after writing
     * @return true if was written completely, false otherwise
     * @throws IOException if something went wrong
     */
    public boolean writeCommandToClient() throws IOException {
        if (bytesWritten == 0) {
            // if the writing process occurs first time, write meta data
            ByteBuffer metaDataBuffer = ByteBuffer.allocate(ProtocolConstants.COMMAND_LENGTH);
            metaDataBuffer.putInt(writeCommandLength);
            metaDataBuffer.flip();
            int written = socketChannel.write(metaDataBuffer);
            if (written != ProtocolConstants.COMMAND_LENGTH) {
                //TODO: feedback
                throw new IOException("Could not write meta data!");
            }
        }
        int position = writeBuffer.position(); // remembering buffer position
        writeBuffer.position(bytesWritten); // set it to the right point
        int written = socketChannel.write(writeBuffer);
        if (written == 0 || written == -1) {
            //TODO: feedback
            throw new IOException("Could not write command content!");
        }
        writeBuffer.position(position); // returning to old position
        bytesWritten += written;
        // checking if all info was written
        if (bytesWritten == writeCommandLength) {
            writeBuffer = null;
            bytesWritten = 0;
            writeCommandLength = -1;
            key.interestOps(SelectionKey.OP_READ);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        if (user == null && socketChannel == null) {
            return "unrecognized connection";
        }
        if (user == null) {
            return "host " + socketChannel.socket().getInetAddress().getHostAddress();
        }
        // case when channel is null and user is not can not occur
        String statusString = Status.STATUS_STRINGS.get(user.getStatus());
        return "User " + user.getUsername() + " (" + statusString + ")";

    }

    public void setUsername(String username) {
        user.setUsername(username);
    }

    public void setStatus(byte status) {
        user.setStatus(status);
    }

    public void setKey(SelectionKey key) {
        this.key = key;
    }

    public void setWriteCommandType(String writeCommandType) {
        this.writeCommandType = writeCommandType;
    }

    public void setReadCommandType(byte readCommandType) {
        this.readCommandType = readCommandType;
    }

    public User getUser() {
        return user;
    }

    public String getUsername() {
        return user.getUsername();
    }

    public byte getStatus() {
        return user.getStatus();
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public byte getReadCommandType() {
        return readCommandType;
    }

    public String getWriteCommandType() {
        return writeCommandType;
    }
}
