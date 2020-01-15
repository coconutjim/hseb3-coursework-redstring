package client_server.server;

import client_server.UserConnection;
import client_server.protocol.ChatInfo;
import client_server.server.util.ServerUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * Represents selector server structure
 */
public abstract class Lobby implements Runnable {

    /**
     * The main selector
     */
    protected Selector selector;

    /**
     * Users in this server
     */
    protected Map<SelectionKey, UserConnection> users;

    /** New users */
    protected List<UserConnection> newUsers;

    /** Commands to users. If key is null, notification is to all users */
    private Map<ChatInfo, CommandReceiver> lobbyCommands;

    /**
     * Constructor. Starts the selector
     *
     * @throws IOException if something went wrong
     */
    public Lobby() throws IOException {
        users = new ConcurrentHashMap<SelectionKey, UserConnection>(); // concurrency
        newUsers = new CopyOnWriteArrayList<UserConnection>(); // concurrency
        lobbyCommands = new ConcurrentHashMap<ChatInfo, CommandReceiver>(); // concurrency
        selector = Selector.open();
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
     * Add user to lobby and send a request_to_node to selector to register him
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
    protected boolean checkUserOccurrence(SocketChannel socketChannel) {
        Set<Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, UserConnection> entry : entries) {
            if (entry.getValue().getSocketChannel().socket().getInetAddress().getHostAddress().equals(
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
        ArrayList<String> names = new ArrayList<String>();
        Set<Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, UserConnection> entry : entries) {
            names.add(entry.getValue().getUsername());
        }
        return ServerUtil.checkNameCollisions(username, names);
    }

    /***
     * If channel can read info
     * @return if can
     */
    protected boolean canRead() {
        Set <Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, UserConnection> entry: entries) {
            if (entry.getKey().interestOps() == SelectionKey.OP_WRITE) {
                return false;
            }
        }
        return true;
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
                commandLog("Received " + commandType + " from " + connection.toString() + "!");
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
     * Processes writable key
     * @param key writable key
     * @param connection associated connection
     */
    protected void processWritableKey(SelectionKey key, UserConnection connection) {
        try {
            if (connection.writeCommandToClient()) {
                commandLog(connection.getWriteCommandType() +
                        " was written to " + connection.toString() + "!");
                // if the command was written completely
                connection.setWriteCommandType(null); // clearing type
                connection.checkWriteCommands(); // checking for remaining commands
            }
        }
        catch (IOException e) {
            log("Error while writing command to " + connection.toString() +  ":"
                    + e.getMessage());
            closeConnection(key);
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
            //TODO: check if all channels are ready to write new command
            // check lobbyCommands
            Set<Map.Entry<ChatInfo, CommandReceiver>> notificationEntries = lobbyCommands.entrySet();
            for (Map.Entry<ChatInfo, CommandReceiver> notificationEntry : notificationEntries) {
                ChatInfo info = notificationEntry.getKey();
                try {
                    notifyUsers(info, notificationEntry.getValue());
                }
                catch (IOException e) {
                    errorLog("Error while processing lobby command: " + e.getMessage() + "!");
                }
                lobbyCommands.remove(info);
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
                        if (canRead()) {
                            processReadableKey(key, connection);
                        }
                    } else if (key.isWritable()) {
                        processWritableKey(key, connection);
                    }
                }
                keys.clear(); // clear keys
            }
            catch (IOException e) {
                errorLog("Fatal error in lobby selector: " + e.getMessage() + "!");
                shutdown();
            }
            catch (ClosedSelectorException e1) {
                errorLog("Fatal error in lobby selector: " + e1.getMessage() + "!");
                shutdown();
            }
        }
    }

    /***
     * Sends all users the command (if it is a notification, the status will be considered)
     * @param info command
     * @param commandReceiver command receivers
     * @throws IOException if something went wrong
     */
    private void notifyUsers(ChatInfo info, CommandReceiver commandReceiver) throws IOException {
        if (commandReceiver.isToAll()) { // is is to all
            Set <Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
            for (Map.Entry<SelectionKey, UserConnection> entry: entries) {
                UserConnection userConnection = entry.getValue();
                if (userConnection.getStatus() >= commandReceiver.getMinimumUserStatus()) { // checking status
                    entry.getValue().addCommandToWrite(info);
                }
            }
        }
        ArrayList<SelectionKey> receivers = commandReceiver.getReceivers();
        if (receivers != null) { // checking if there are specified receivers
            for (SelectionKey receiver : receivers) {
                users.get(receiver).addCommandToWrite(info);
            }
        }
    }

    /***
     * Adds command to write
     * @param info command
     * @param receiver command receiver
     */
    protected void addLobbyCommand(ChatInfo info, CommandReceiver receiver) {
        //TODO: see wakeup repetitions
        lobbyCommands.put(info, receiver);
        selector.wakeup();
    }
}