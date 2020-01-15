package client_server.client;

import client_server.User;
import client_server.protocol.ChatInfo;
import client_server.protocol.ProtocolConstants;
import util.DataManagement;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * Represents client in networking
 */
public abstract class Client {

    /** Timeout for waiting server answer */
    protected final static long TIMEOUT = 3000;

    /** Socket channel*/
    private SocketChannel socketChannel;


    /** Commands to send to server. ChatInfo - command itself, byte - command type */
    //TODO: see key value
    private Map<ChatInfo, Byte> commandsToSend;

    /** Commands to execute on the client */
    private List<ChatInfo> incomingCommands;

    /** If the client is connected to server */
    protected boolean connected;

    /** User */
    protected User user;

    /** If command logs are enabled */
    protected boolean commandLogsEnabled;

    /**
     * Constructor (protected because of factory pattern)
     */
    protected Client() {
        commandsToSend = new ConcurrentHashMap<ChatInfo, Byte>(); // concurrency
        incomingCommands = new CopyOnWriteArrayList<ChatInfo>(); // concurrency
        commandLogsEnabled = true;
    }

    /***
     * Tries to connect to the server and retrieves server info
     * @param host server host
     * @param port server ip
     * @return true info if connection succeeded, false otherwise
     */
    protected boolean connect(String host, int port) {
        try {
            if (connected) {
                disconnect(null);
                connected = false;
            }
            log("Trying to connect to the server...");
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(host, port));
            //TODO: norm timeout
            long time = System.currentTimeMillis();
            while (! socketChannel.finishConnect()) {
                if (System.currentTimeMillis() - time > 1000) {
                    throw new NotYetConnectedException();
                }
            }
            connected = true;
            log("Connected to server!");
            return true;
        }
        //TODO: provide normal feedback
        catch (IllegalArgumentException e0) {
            String message = "Could not connect to server: No server was found!";
            log(message);
            showMessageToUser(message);
        }
        catch (NotYetConnectedException e) {
            String message = "Could not connect to server: No server was found!";
            log(message);
            showMessageToUser(message);
        }
        catch (ConnectException e1) {
            String message = "Could not connect to server: No server was found!";
            log(message);
            showMessageToUser(message);
        }
        catch (IOException e2) {
            String message = "Could not connect to server: " + e2.getMessage();
            log(message);
            showMessageToUser(message);
        }
        disconnect(null);
        return false;
    }

    /***
     * Sends info to server (message, client command or server command)
     * @param info info
     * @param infoIndex data index (if no data index needed, it is -1)
     * @throws NotYetConnectedException if not connected to server
     * @throws IOException if something went wrong
     */
    protected void sendInfoToServer(ChatInfo info, byte infoIndex) throws IOException {
        byte[] serialized = DataManagement.serialize(info);
        // add meta data: command length and command index (if needed)
        int metaDataLength = ProtocolConstants.COMMAND_LENGTH + ProtocolConstants.INFO_INDEX_LENGTH;
        //TODO: mb handle OutOfMemory
        ByteBuffer byteBuffer = ByteBuffer.allocate(metaDataLength + serialized.length);
        byteBuffer.putInt(serialized.length);
        byteBuffer.put(infoIndex);
        byteBuffer.put(serialized);
        byteBuffer.flip();
        while(byteBuffer.hasRemaining()) {
            socketChannel.write(byteBuffer);
        }
    }

    /***
     * Gets answer from server
     * @param timeout timeout in milliseconds
     * @return server answer
     * @throws IOException ConnectedException if time is out or IOException if something went wrong
     * @throws ClassNotFoundException if problems with reading occurred
     * @throws ClassCastException if problems with reading occurred
     */
    protected ChatInfo getAnswerFromServer(long timeout) throws IOException,
            ClassNotFoundException, ClassCastException {
        //TODO: norm timeout
        ChatInfo command;
        long time = System.currentTimeMillis();
        while ((command = readCommandFromServer()) == null) {
            if (System.currentTimeMillis() - time > timeout) {
                throw new ConnectException();
            }
        }
        return command;
    }

    /***
     * Read command from server
     * @return command
     * @throws IOException if something went wrong
     * @throws ClassNotFoundException if problems with reading occurred
     * @throws ClassCastException if problems with reading occurred
     */
    protected ChatInfo readCommandFromServer() throws IOException, ClassNotFoundException, ClassCastException {
        ByteBuffer buffer = ByteBuffer.allocate(ProtocolConstants.COMMAND_LENGTH);
        //TODO: norm checkLobbyInfo if was read
        int bytes = socketChannel.read(buffer);
        if (bytes == 0 || bytes == -1) {
            return null;
        }
        buffer.flip();
        ByteBuffer buffer1 = ByteBuffer.allocate(buffer.getInt());
        //TODO: see block
        while(buffer1.hasRemaining()) {
            socketChannel.read(buffer1);
        }
        buffer1.flip();
        return (ChatInfo) DataManagement.inflate(buffer1);
    }

    /***
     * Runs the client
     */
    protected void runClient() {
        /*//TODO: see it
        // Wait for GUI setting
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            String message = "Fatal error: " + e.getMessage() + "!";
            showMessageToUser(message);
            log(message);
        }*/
        new Thread(new CommandExecutor()).start();
        new Thread(new Receiver()).start();
        new Thread(new Sender()).start();

        log("The client was started!");
    }

    /***
     * Disconnects the client
     * @param message message to user
     */
    protected synchronized void disconnect(String message) {
        if (socketChannel.isConnected()) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                log("Disconnection fail: " + e.getMessage() + "!");
            }
        }
        connected = false;
        user = null;
        log("Disconnected!");
    }

    /***
     * Adds client command to send
     * @param command command to send
     * @param commandIndex command index
     */
    protected void addCommandToSend(ChatInfo command, byte commandIndex) {
        commandsToSend.put(command, commandIndex);
    }

    /**
     * For reading from server
     */
    private class Receiver implements Runnable{
        public void run() {
            //TODO: see error handling (mb disconnect)
            while (connected) {
                try {
                    ChatInfo command = readCommandFromServer();
                    if (command != null) {
                        if (commandLogsEnabled) {
                            log(command.getClass().getSimpleName() + " was received!");
                        }
                        incomingCommands.add(command);
                    }
                }
                //TODO: provide normal feedback
                catch (NotYetConnectedException e) {
                    log("Error while reading command: connection failed!");
                    disconnect(null);
                }
                catch (IOException e1) {
                    log("Error while reading command: " + e1.getMessage());
                    disconnect(null);
                }
                catch (ClassNotFoundException e2) {
                    log("Error while reading command: unexpected info from server!");
                }
                catch (ClassCastException e3) {
                    log("Error while reading command: unexpected info from server!");
                }
            }
        }
    }

    private class CommandExecutor implements Runnable {
        public void run() {
            while (connected) {
                if (! incomingCommands.isEmpty()) {
                    ChatInfo command = incomingCommands.get(0);
                    try {
                        executeCommand(command);
                    }
                    catch (ClassNotFoundException e2) {
                        log("Error while executing command: unexpected info from server!");
                    }
                    catch (ClassCastException e3) {
                        log("Error while executing command: unexpected info from server!");
                    }
                    incomingCommands.remove(0);
                }
            }
        }
    }

    /**
     * For writing to server
     */
    private class Sender implements Runnable {
        public void run() {
            while (connected) {
                //TODO: mb observer
                Set<Map.Entry<ChatInfo, Byte>> entries = commandsToSend.entrySet();
                for (Map.Entry<ChatInfo, Byte> entry : entries) {
                    try {
                        sendInfoToServer(entry.getKey(), entry.getValue());
                        if (commandLogsEnabled) {
                            log(entry.getKey().getClass().getSimpleName() + " was written!");
                        }
                    }
                    //TODO: provide normal feedback
                    catch (NotYetConnectedException e) {
                        log("Error while writing command: connection failed!");
                        disconnect(null);
                    } catch (IOException e1) {
                        log("Error while writing command: " + e1.getMessage());
                        disconnect(null);
                    }
                    commandsToSend.remove(entry.getKey());
                }
            }
        }
    }

    /***
     * Checks if command can be executed
     * @return true if it can
     */
    protected abstract boolean canExecute();

    /***
     * Executes command
     * @param command command to execute
     * @throws ClassCastException in case of unexpected command
     * @throws ClassNotFoundException in case of unexpected command
     */
    protected abstract void executeCommand(ChatInfo command) throws ClassCastException, ClassNotFoundException;


    /***
     * Shows user important messages about networking process
     * @param message info message
     */
    protected abstract void showMessageToUser(String message);

    /***
     * Shows logs about networking process
     * @param message info message
     */
    protected abstract void log(String message);
}
