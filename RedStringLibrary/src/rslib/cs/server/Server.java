package rslib.cs.server;

import rslib.cs.common.CommandInfo;
import rslib.cs.common.UserConnection;
import rslib.cs.server.util.IncomingCommand;
import rslib.cs.common.ConnectConfiguration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.AbstractQueue;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    /** Command executor */
    protected Executor executor;

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
        users = new ConcurrentHashMap<>(); // concurrency
        equalHostsAllowed = true;
        allLogsEnabled = true;
        commandLogsEnabled = true;
        executor = new Executor();
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
     * Actions when new client connects
     */
    protected void acceptConnection() {
        try {
            log("Connection try");
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            UserConnection userConnection = new UserConnection(null, socketChannel);
            SelectionKey selectionKey = userConnection.registerOnSelector(selector);
            users.put(selectionKey, userConnection);

            log("New connection from " + socketChannel.socket().
                    getInetAddress().getHostAddress() + "!");
        }
        catch (IOException e) {
            errorLog("Connection error " + e.getMessage() + "!");
        }
    }

    /***
     * Processes read command
     * @param command command
     * @param connection associated user connection
     * @throws IllegalStateException if info has illegal data
     * @throws ClassCastException if info has illegal data
     * @throws ClassNotFoundException if info has illegal data
     * @throws IOException if something went wrong
     */
    protected abstract void processReadCommand(CommandInfo command, UserConnection
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
            CommandInfo command = connection.readCommand();
            if (command != null) { // if message was read completely
                executor.addCommand(new IncomingCommand(command, connection));
            }
            return;
        }
        catch (IllegalStateException e) {
            errorLog("Unexpected command (unexpected meta data) from "
                    + connection.toString() + " !");
        }
        catch (IOException e3) {
            // Basically, here is the end of connection
            /*errorLog("Error while reading command from " + connection.toString() + ":"
                    + e3.getMessage());*/
        }
        closeConnection(key);
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
                keys.clear(); // clear set of keys
            }
            catch (IOException | ClosedSelectorException e) {
                errorLog("Fatal error in main selector: " + e.getMessage() + "!");
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


    /***
     * For processing incoming commands
     */
    class Executor implements Runnable {

        /** Command queue */
        private final AbstractQueue<IncomingCommand> commands;

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
                        errorLog("Fatal error in executor thread: " + e.getMessage() + "!");
                        shutdown();
                    }
                }
                else {
                    while (! commands.isEmpty()) {
                        IncomingCommand command = commands.poll();
                        CommandInfo commandInfo = command.getCommandInfo();
                        UserConnection connection = command.getConnection();
                        try {
                            processReadCommand(commandInfo, connection);
                            continue;
                        }
                        catch (IllegalStateException e) {
                            errorLog("Unexpected command (unexpected command index) from " + connection.toString() + "!");
                        }
                        catch (ClassNotFoundException e1) {
                            errorLog("Unexpected command (class not found) from " + connection.toString() + "!");
                        }
                        catch (ClassCastException e2) {
                            errorLog("Unexpected command (class cast) from " + connection.toString() + "!");
                        }
                        catch (IOException e3) {
                            errorLog("Error while processing command from " + connection.toString() + ":"
                                    + e3.getMessage());
                        }
                        closeConnection(connection.getKey());
                    }
                }
            }
        }

        /***
         * Adds command to executor
         * @param command command
         */
        public void addCommand(IncomingCommand command) {
            commands.add(command);
            try {
                synchronized (commands) {
                    commands.notify();
                }
            }
            catch (IllegalMonitorStateException e) {
                // can not be
                errorLog("Fatal error while adding command to execute: " + e.getMessage() + "!");
                shutdown();
            }
        }
    }
}
