package rslib.cs.server;

import rslib.cs.common.CommandInfo;
import rslib.cs.common.UserConnection;
import rslib.cs.protocol.RedStringInfo;
import rslib.cs.server.util.CommandReceiver;
import rslib.cs.server.util.IncomingCommand;
import rslib.cs.server.util.LobbyCommand;
import rslib.cs.server.util.ServerUtil;

import java.io.IOException;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * Represents selector server structure
 */
public abstract class Lobby implements Runnable {

    /** The main selector */
    protected Selector selector;

    /** Users */
    protected final Map<SelectionKey, UserConnection> users;

    /** Users (another collection) */
    protected final List<UserConnection> userList;

    /** New users */
    protected List<UserConnection> newUsers;

    /** Commands to users */
    private final AbstractQueue<LobbyCommand> lobbyCommands;

    /** Command executor */
    protected Executor executor;

    /** Current processing readable connection id */
    private int currentReadable;
    private final int NO_READABLE = -100;

    /**
     * Constructor. Starts the selector
     *
     * @throws IOException if something went wrong
     */
    public Lobby() throws IOException {
        currentReadable = NO_READABLE;
        users = new ConcurrentHashMap<>(); // concurrency
        userList = new CopyOnWriteArrayList<>(); // concurrency
        newUsers = new CopyOnWriteArrayList<>(); // concurrency
        lobbyCommands = new ConcurrentLinkedQueue<>(); // concurrency
        selector = Selector.open();
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

    /***
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
     * Add user to lobby and send a request to selector to register him
     * @param userConnection user connection
     */
    public synchronized void addUser(UserConnection userConnection) {
        newUsers.add(userConnection);
        selector.wakeup();
    }

    /***
     * Registers all new users in lobby
     * @throws IOException if something went wrong
     */
    protected abstract void registerNewUsers() throws IOException;


    /***
     * Checks if this user already exists in the lobby
     * @param socketChannel requested user
     * @return true if exists, false otherwise
     */
    public boolean checkUserOccurrence(SocketChannel socketChannel) {
        Set<Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, UserConnection> entry : entries) {
            if (entry.getValue().getHost().equals(
                    socketChannel.socket().getInetAddress().getHostAddress())) {
                return true;
            }
        }
        return false;
    }

    /***
     *
     * @param username requested username
     * @return requested name if no collision appears, new name otherwise
     */
    protected synchronized String checkUsername(String username)  {
        //TODO: see map - list converting
        ArrayList<String> names = new ArrayList<>();
        Set<Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, UserConnection> entry : entries) {
            names.add(entry.getValue().getUsername());
        }
        return ServerUtil.checkNameCollisions(username, names);
    }

    /***
     * If channel can read info
     * @param connection current connection
     * @return if can
     */
    protected boolean canRead(UserConnection connection) {
        //System.out.println(currentReadable);
        return currentReadable == NO_READABLE || currentReadable == connection.getId();
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
            //TODO: check
            if (connection == null) {
                return;
            }
            // try to read incoming command
            currentReadable = connection.getId();
            CommandInfo command = connection.readCommand();
            if (command != null) { // if message was read completely
                executor.addCommand(new IncomingCommand(command, connection));
                currentReadable = NO_READABLE;
            }
            return;
        }
        catch (IllegalStateException e) {
            errorLog("Unexpected command (unexpected meta data) from "
                    + connection.toString() + " !");
        }
        catch (IOException e1) {
            // Basically, here is the end of connection
            /*errorLog("Error while reading command from " + connection.toString() + ":"
                    + e1.getMessage());*/
        }
        currentReadable = NO_READABLE;
        closeConnection(key);
    }

    /***
     * Processes writable key
     * @param key writable key
     * @param connection associated connection
     */
    protected void processWritableKey(SelectionKey key, UserConnection connection) {
        try {
            if (connection.writeCommand()) {
                commandLog(connection.getWriteCommandClass() +
                        " was written to " + connection.toString() + "!");
                // if the command was written completely
                connection.clearWriteData();
                connection.checkWriteCommands(); // checking for remaining commands
            }
        }
        catch (IOException e) {
            // Basically, here is the end of connection
            /*errorLog("Error while writing command to " + connection.toString() +  ":"
                    + e.getMessage());*/
            closeConnection(key);
        }
        catch (CancelledKeyException e1) {
            // user is not connected anymore
        }
    }

    /***
     * The main processing
     */
    @Override
    public void run() {
        while (selector.isOpen()) {
            // trying to add new users to selector
            try {
                registerNewUsers();
            }
            catch (IOException e) {
                log("Failed to register new user: " + e.getMessage() + "!");
            }
            // check lobbyCommands
            while (! lobbyCommands.isEmpty()) {
                LobbyCommand lobbyCommand = lobbyCommands.poll();
                try {
                    notifyUsers(lobbyCommand.getCommandInfo(), lobbyCommand.getCommandReceiver());
                }
                catch (IOException e) {
                    errorLog("Error while processing lobby command: " + e.getMessage() + "!");
                }
            }
            try {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                for (SelectionKey key : keys) {
                    if (! key.isValid()) {
                        continue;
                    }
                    UserConnection connection = users.get(key);
                    if (key.isReadable()) {
                        if (canRead(connection)) {
                            processReadableKey(key, connection);
                        }
                    } else if (key.isWritable()) {
                        processWritableKey(key, connection);
                    }
                }
                keys.clear(); // clear keys
            }
            catch (IOException | ClosedSelectorException e) {
                errorLog("Fatal error in lobby selector: " + e.getMessage() + "!");
                shutdown();
            }
        }
    }

    /***
     * Sends all users a notification the command (if it is a notification, the status will be considered)
     * @param info command
     * @param commandReceiver command receivers
     * @throws IOException if something went wrong
     */
    private void notifyUsers(CommandInfo info, CommandReceiver commandReceiver) throws IOException {
        List<UserConnection> receivers = commandReceiver.getReceivers();
        for (UserConnection receiver : receivers) {
            if (receiver.getStatus().ordinal() >= commandReceiver.getMinimumUserStatus().ordinal()) {
                try {
                    receiver.addCommandToWrite(info);
                }
                catch (CancelledKeyException e) {
                    // user is not connected anymore
                }
            }
        }
    }

    /***
     * Adds command to write
     * @param info command
     * @param receiver command receiver
     */
    protected void addLobbyCommand(RedStringInfo info, byte commandType, CommandReceiver receiver) {
        //TODO: see wakeup repetitions
        CommandInfo commandInfo;
        try {
            commandInfo = new CommandInfo(info, commandType);
        }
        catch (IOException e) {
            errorLog("Unable to write lobby command: " + e.getMessage() + "!");
            return;
        }
        lobbyCommands.add(new LobbyCommand(commandInfo, receiver));
        selector.wakeup();
    }

    /***
     * Adds command to write
     * @param commandInfo command
     * @param receiver command receiver
     */
    protected void addLobbyCommand(CommandInfo commandInfo, CommandReceiver receiver) {
        //TODO: see wakeup repetitions
        lobbyCommands.add(new LobbyCommand(commandInfo, receiver));
        selector.wakeup();
    }

    /***
     * Generates unique id for new connection
     * @return unique id
     */
    protected int generateId() {
        Random random = new Random();
        int id = random.nextInt(1000000);
        for (UserConnection connection : userList) {
            if (connection.getId() == id) {
                return generateId();
            }
        }
        return id;
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