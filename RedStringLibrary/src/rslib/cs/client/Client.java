package rslib.cs.client;

import rslib.cs.common.CommandInfo;
import rslib.cs.common.UserConnection;
import rslib.cs.common.ConnectConfiguration;
import rslib.cs.protocol.RedStringInfo;
import rslib.cs.protocol.ProtocolConstants;
import rslib.cs.protocol.events.message.ShowMessageEvent;
import rslib.listeners.DisconnectListener;
import rslib.listeners.LogListener;
import rslib.listeners.MessageListener;
import rslib.util.DataManagement;
import rslib.util.FileWorking;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.AbstractQueue;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * Represents client in networking
 */
public abstract class Client implements Runnable {

    /** Default settings */
    protected static final String DEFAULT_HOST = ConnectConfiguration.REMOTE_HOST;

    /** Server host */
    protected String host;

    /** Server port */
    protected int port;

    /** Timeout for waiting server answer */
    protected final static long TIMEOUT = 3000;

    /** Client selector */
    private Selector selector;

    /** User connection */
    protected UserConnection connection;

    /** If connected */
    private boolean connected;

    /** Command executor */
    protected Executor executor;

    /** Disconnect listeners */
    private List<DisconnectListener> disconnectListeners;

    /** Log listeners */
    private List<LogListener> logListeners;

    /** Message listeners */
    protected List<MessageListener> messageListeners;

    /**
     * Constructor (protected because of factory pattern)
     * @param host server host
     * @param port server port
     */
    protected Client(String host, int port) {
        this.host = host;
        this.port = port;
        connected = false;
        disconnectListeners = new CopyOnWriteArrayList<>(); // concurrency
        logListeners = new CopyOnWriteArrayList<>(); // concurrency
        messageListeners = new CopyOnWriteArrayList<>(); // concurrency
        executor = new Executor();
    }

    /***
     * Adds listener to observer
     * @param listener listener
     */
    public void addMessageListener(MessageListener listener) {
        if (listener != null) {
            messageListeners.add(listener);
        }
    }

    /***
     * Adds listener to observer
     * @param listener listener
     */
    public void addLogListener(LogListener listener) {
        if (listener != null) {
            logListeners.add(listener);
        }
    }

    /***
     * Adds listener to observer
     * @param listener listener
     */
    public void addDisconnectListener(DisconnectListener listener) {
        if (listener != null) {
            disconnectListeners.add(listener);
        }
    }

    /***
     * Removes listener from observer
     * @param listener listener
     */
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    /***
     * Removes listener from observer
     * @param listener listener
     */
    public void removeLogListener(LogListener listener) {
        logListeners.remove(listener);
    }

    /***
     * Removes listener from observer
     * @param listener listener
     */
    public void removeDisconnectListener(DisconnectListener listener) {
        disconnectListeners.remove(listener);
    }

    /***
     * Tries to connect to the server and retrieves server info
     * @param host server host
     * @param port server ip
     * @return socket channel if connection succeeded, null otherwise
     */
    protected SocketChannel connect(String host, int port) {
        try {
            if (connected) {
                disconnect("");
            }
            log("Trying to connect to the server...");
            SocketChannel socketChannel = SocketChannel.open();
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
            return socketChannel;
        }
        catch (IllegalArgumentException | NotYetConnectedException | ConnectException e) {
            String message = "Could not connect to " +
                    ((host != null && host.equals(ConnectConfiguration.REMOTE_HOST))?
            "remote server" : host) + ": No server was found!";
            log(message);
            disconnect(""); // no need of showing message, it is done in GUI
        }
        catch (IOException e1) {
            String message = "Could not connect to server: " + e1.getMessage();
            log(message);
            disconnect(message);
        }
        return null;
    }

    /***
     * Sends short info
     * @param command command
     * @param socketChannel socket channel
     * @throws NotYetConnectedException if not connected to server
     * @throws IOException if something went wrong
     */
    protected void sendInfoToServer(RedStringInfo command, SocketChannel socketChannel) throws IOException {
        byte commandType = ProtocolConstants.CONNECT_INDEX;
        byte[] serialized = DataManagement.serialize(command);
        // add meta data: command length and command index (if needed)
        int metaDataLength = ProtocolConstants.COMMAND_LENGTH + ProtocolConstants.INFO_INDEX_LENGTH;
        //TODO: mb handle OutOfMemory
        ByteBuffer byteBuffer = ByteBuffer.allocate(metaDataLength + serialized.length);
        byteBuffer.putInt(serialized.length);
        byteBuffer.put(commandType);
        byteBuffer.put(serialized);
        byteBuffer.flip();
        while(byteBuffer.hasRemaining()) {
            socketChannel.write(byteBuffer);
        }
    }

    /***
     * Gets short answer from server
     * @param socketChannel socket channel
     * @param timeout timeout in milliseconds
     * @return server answer
     * @throws IOException ConnectedException if time is out or IOException if something went wrong
     * @throws ClassNotFoundException if data is wrong
     * @throws IllegalStateException if data is wrong
     * @throws ClassCastException if data is wrong
     */
    protected RedStringInfo getAnswerFromServer(SocketChannel socketChannel, long timeout) throws IOException,
            ClassNotFoundException, ClassCastException, IllegalStateException {
        //TODO: norm timeout
        RedStringInfo info;
        long time = System.currentTimeMillis();
        while ((info = readInfoFromServer(socketChannel)) == null) {
            if (System.currentTimeMillis() - time > timeout) {
                throw new ConnectException();
            }
        }
        return info;
    }

    /***
     * Reads short command from server (connected with the connection)
     * @param socketChannel socket channel
     * @return command
     * @throws IOException if something went wrong
     * @throws ClassNotFoundException if data is wrong
     * @throws ClassCastException if data is wrong
     * @throws IllegalStateException if data is wrong
     */
    protected RedStringInfo readInfoFromServer(SocketChannel socketChannel) throws IOException,
            ClassNotFoundException, ClassCastException,
            IllegalStateException {
        int metaDataLength = ProtocolConstants.COMMAND_LENGTH + ProtocolConstants.INFO_INDEX_LENGTH;
        ByteBuffer metaDataBuffer = ByteBuffer.allocate(metaDataLength);
        int read = socketChannel.read(metaDataBuffer);
        if (read != metaDataLength) {
            return null;
        }
        metaDataBuffer.flip();
        int length = metaDataBuffer.getInt();
        byte index = metaDataBuffer.get();
        if (index != ProtocolConstants.CONNECT_INDEX) {
            throw new IllegalStateException("Wrong command index");
        }
        ByteBuffer buffer;
        try {
            buffer = ByteBuffer.allocate(length);
        }
        catch (OutOfMemoryError error) {
            throw new IllegalStateException("Too big message");
        }
        while(buffer.hasRemaining()) {
            socketChannel.read(buffer);
        }
        buffer.flip();
        return (RedStringInfo) DataManagement.inflate(buffer);
    }

    /***
     * Starts the client
     * @param connection user connection
     * @param socketChannel channel to close it manually if starting failed
     */
    protected void start(UserConnection connection, SocketChannel socketChannel) {
        try {
            this.connection = connection;
            selector = Selector.open();
            //TODO: selection key??
            connection.registerOnSelector(selector);
            new Thread(this).start();
            new Thread(executor).start();
            log("The client was started!");
        }
        catch (IOException e) {
            disconnect(socketChannel, "Unable to start client: Fatal: " + e.getMessage() + "!");
        }

    }

    /***
     * Disconnects the client and shuts up separated socket channel that was created
     * @param socketChannel socket channel
     * @param message message to user
     */
    protected synchronized void disconnect(SocketChannel socketChannel, String message) {
        try {
            if (socketChannel.isConnected()) {
                socketChannel.close();
            }
        }
        catch (IOException e) {
            log("Disconnection fail: " + e.getMessage() + "!");
        }
        disconnect(message);
    }

    /***
     * Disconnects the client
     * @param message message to user
     */
    protected synchronized void disconnect(String message) {
        connected = false;
        try {
            if (selector != null) {
                if (connection != null) {
                    SelectionKey key = connection.getKey();
                    key.cancel();
                    SocketChannel socketChannel = (SocketChannel)key.channel();
                    if (socketChannel.isConnected()) {
                        socketChannel.close();
                    }
                }
                selector.close();
            }
        }
        catch (IOException e) {
            log("Disconnection fail: " + e.getMessage() + "!");
        }
        if (message == null) {
            showMessage("Disconnection by server!", ShowMessageEvent.MessageType.INFO);
        }
        else {
            if (! message.equals("") && ! message.equals("Disconnection by user!")) {
                showMessage(message, ShowMessageEvent.MessageType.INFO);
            }
        }
        log("Disconnected!");
        for (DisconnectListener listener : disconnectListeners) {
            listener.hearDisconnection();
        }
        messageListeners.clear();
        logListeners.clear();
        disconnectListeners.clear();
    }

    /***
     * Adds client command to send
     * @param command command to send
     * @param commandIndex command index
     */
    protected void addCommandToSend(RedStringInfo command, byte commandIndex) {
        try {
            connection.addCommandToWrite(new CommandInfo(command, commandIndex));
            selector.wakeup();
        }
        catch (IOException e) {
            log("Unable to write command: " + e.getMessage());
        }
    }

    /***
     * The main processing
     */
    @Override
    public void run() {
        while (selector.isOpen()) {
            try {
                connection.checkWriteCommands();
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                for (SelectionKey key : keys) {
                    if (! key.isValid()) {
                        continue;
                    }
                    if (! connection.getKey().equals(key)) {
                        return;
                    }
                    if (key.isReadable()) {
                        processReadableKey(connection);
                    } else if (key.isWritable()) {
                        processWritableKey(connection);
                    }
                }
                keys.clear(); // clear keys
            }
            catch (IOException | ClosedSelectorException e) {
                log("Fatal error in client selector: " + e.getMessage() + "!");
                disconnect(null);
            }
        }
    }

    /***
     * Processes readable key
     * @param connection associated connection
     */
    private void processReadableKey(UserConnection connection) {
        try {
            // try to read incoming command
            CommandInfo command = connection.readCommand();
            if (command != null) { // if message was read completely4
                executor.addCommand(command);
            }
            return;
        }
        catch (IllegalStateException e) {
            log("Received unexpected command (unexpected meta data)!");
        } catch (IOException e3) {
            log("Error while reading command: " + e3.getMessage() + "!");
        }
        disconnect(null);
    }

    /***
     * Processes writable key
     * @param connection associated connection
     */
    private void processWritableKey(UserConnection connection) {
        try {
            if (connection.writeCommand()) {
                log(connection.getWriteCommandClass() + " was written (" +
                        connection.getWriteCommandLength() +" bytes)!");
                connection.clearWriteData();
                connection.checkWriteCommands(); // checking for remaining commands
            }
        }
        catch (IOException e) {
            log("Error while writing command: " + e.getMessage() + "!");
            disconnect(null);
        }
    }

    /***
     * Handles command
     * @param command command
     * @throws IOException if something went wrong
     * @throws IllegalStateException in case of unexpected command
     * @throws ClassCastException in case of unexpected command
     * @throws ClassNotFoundException in case of unexpected command
     */
    protected abstract void processReadCommand(CommandInfo command)
            throws IllegalStateException, IOException, ClassCastException, ClassNotFoundException;

    /***
     * Shows logs about networking process
     * @param message info message
     */
    protected void log(String message) {
        FileWorking.logToFile("logs.txt", message);
        for (LogListener listener : logListeners) {
            listener.hearLog(message);
        }
    }

    /***
     * Shows message to user
     * @param message info message
     */
    protected void showMessage(String message, ShowMessageEvent.MessageType type) {
        for (MessageListener listener : messageListeners) {
            listener.hear(new ShowMessageEvent(message, type));
        }
    }

    public boolean isConnected() {
        return connected;
    }


    /***
     * For processing incoming commands
     */
    class Executor implements Runnable {

        /** Command queue */
        private final AbstractQueue<CommandInfo> commands;

        /***
         * Constructor
         */
        Executor() {
            commands = new ConcurrentLinkedQueue<>();
        }

        @Override
        public void run() {
            while (selector.isOpen()) {
                if (commands.isEmpty()) {
                    try {
                        synchronized (commands) {
                            commands.wait(1000);
                        }
                    }
                    catch (InterruptedException | IllegalMonitorStateException e) {
                        // can not be
                        log("Fatal error in executor thread: " + e.getMessage() + "!");
                        disconnect(null);
                    }
                }
                else {
                    while (! commands.isEmpty()) {
                        CommandInfo command = commands.poll();
                        try {
                            processReadCommand(command);
                            continue;
                        }
                        catch (IllegalStateException e) {
                            log("Received unexpected command (unexpected index)!");
                        } catch (ClassNotFoundException | ClassCastException e1) {
                            log("Received unexpected command: " + e1.getMessage() + "!");
                        } catch (IOException e3) {
                            log("Error while processing command: " + e3.getMessage() + "!");
                        }
                        disconnect(null);
                    }
                }
            }
        }

        public void addCommand(CommandInfo command) {
            commands.add(command);
            try {
                synchronized (commands) {
                    commands.notify();
                }
            }
            catch (IllegalMonitorStateException e) {
                // can not be
                log("Fatal error while adding command to execute: " + e.getMessage() + "!");
                disconnect(null);
            }
        }
    }
}
