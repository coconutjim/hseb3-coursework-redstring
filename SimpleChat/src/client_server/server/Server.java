package client_server.server;

import client_server.UserConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Represents a selector server structure with server socket
 */
public abstract class Server implements Runnable {

    /** The main selector */
    protected Selector selector;

    /** Users in this server */
    protected Map<SelectionKey, UserConnection> users;

    /** Server socket channel */
    protected ServerSocketChannel serverSocketChannel;

    /** If users with equal hosts are allowed in the lobby */
    protected boolean equalHostsAllowed;

    /** If logs are enabled */
    protected boolean allLogsEnabled;

    /** If command logs are enabled */
    protected boolean commandLogsEnabled;

    /***
     * Constructor. Launches server socket channel
     * @param port server port
     * @throws IOException if something went wrong
     */
    public Server(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        users = new ConcurrentHashMap<SelectionKey, UserConnection>(); // concurrency
        equalHostsAllowed = false;
        allLogsEnabled = true;
        commandLogsEnabled = true;
    }

    /***
     * Starts working
     */
    public abstract void start();

    /***
     * Logs common message
     * @param message message
     */
    public abstract void log(String message);

    /**
     * Logs error message
     * @param message message
     */
    public abstract void errorLog(String message);

    /***
     * Logs command processing
     * @param message message
     */
    public abstract void commandLog(String message);

    /***
     * Shutdowns GUI
     */
    public abstract void shutdown();

    /***
     * Closes selected user connection
     * @param key key for connection
     */
    protected abstract void closeConnection(SelectionKey key);

    /***
     *
     */
    protected void acceptConnection() {
        try {
            log("Connection try");
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
            users.put(selectionKey, new UserConnection(null, socketChannel));

            log("New connection from " + socketChannel.socket().
                    getInetAddress().getHostAddress() + "!");
        }
        catch (IOException e) {
            errorLog("Connection error " + e.getMessage() + "!");
        }
    }

    /***
     * Processes read info
     * @param buffer buffered info
     * @param key info sender
     * @param connection associated user connection
     * @throws IllegalStateException if info has illegal data
     * @throws ClassCastException if info has illegal data
     * @throws ClassNotFoundException if info has illegal data
     * @throws IOException if something went wrong
     * @return processed command type
     */
    protected abstract String processReadCommand(ByteBuffer buffer, SelectionKey key, UserConnection
            connection) throws IllegalStateException,
            ClassCastException, ClassNotFoundException, IOException;

    /***
     * Processes readable key
     * @param key readable key
     * @param connection associated connection
     */
    protected void processReadableKey(SelectionKey key, UserConnection connection) {
        try {
            // try to read incoming command
            ByteBuffer message = connection.readCommandFromClient();
            if (message != null) { // if message was read completely
                String commandType = processReadCommand(message, key, connection);
                commandLog("Received " + commandType + " from " + connection.getSocketChannel().socket().
                        getInetAddress().getHostAddress() + "!");
                connection.setReadCommandType((byte)-1); // clearing type
            }
        }
        catch (IllegalStateException e) {
            errorLog("Unexpected command (unexpected command index) from "
                    + connection.toString() + " !");
        } catch (ClassNotFoundException e1) {
            errorLog("Unexpected command (class not found) from " + connection.toString() + "!");
        } catch (ClassCastException e2) {
            errorLog("Unexpected command (class cast) from " + connection.toString() + "!");
        } catch (IOException e3) {
            errorLog("Error while reading command from " + connection.toString() + ":"
                    + e3.getMessage());
            closeConnection(key);
        }
    }

    /***
     * The main processing
     */
    @Override
    public void run() {
        while (selector.isOpen()) {
            try {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                for (SelectionKey key : keys) {
                    if (! key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) { // if a connection appears
                        acceptConnection();
                    } else if (key.isReadable()) { // if connection writes something
                        UserConnection connection = users.get(key);
                        processReadableKey(key, connection);
                    }
                }
                keys.clear(); // clean set of keys
            }
            catch (IOException e) {
                errorLog("Fatal error in main selector: " + e.getMessage() + "!");
                shutdown();
            }
            catch (ClosedSelectorException e1) {
                errorLog("Fatal error in main selector: " + e1.getMessage() + "!");
                shutdown();
            }
        }
    }

    public void setEqualHostsAllowed(boolean equalHostsAllowed) {
        this.equalHostsAllowed = equalHostsAllowed;
    }

    public void setAllLogsEnabled(boolean allLogsEnabled) {
        this.allLogsEnabled = allLogsEnabled;
        this.commandLogsEnabled = allLogsEnabled;
    }

    public void setCommandLogsEnabled(boolean commandLogsEnabled) {
        this.commandLogsEnabled = commandLogsEnabled;
    }
}
