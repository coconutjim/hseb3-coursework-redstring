package client_server.server;
import client_server.LobbyInfo;
import client_server.User;
import client_server.UserConnection;
import client_server.protocol.ProtocolConstants;
import client_server.protocol.command_to_client.from_node.from_server.AnswerCommand;
import client_server.protocol.command_to_client.from_node.from_server.SendLobbyListCommand;
import client_server.protocol.request_to_node.to_server.UserServerRequest;
import client_server.server.util.ServerConfiguration;
import client_server.server.util.ServerUtil;
import client_server.server.util.Status;
import util.DataManagement;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.nio.channels.*;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Represents main server for users
 */
public class UserServer extends Server implements Runnable {

    /** Lobbies (String for lobby name) */
    private Map<String, UserLobby> lobbies;

    /** Link to admin lobby */
    private AdminLobby adminLobby;

    /***
     * The constructor. Starts the server
     * @throws IOException if something went wrong
     */
    public UserServer() throws IOException {
        super(ServerConfiguration.getUserPort());
        lobbies = new ConcurrentHashMap<String, UserLobby>(); // concurrency
    }

    @Override
    public void start() {
        new Thread(this).start();
        adminLobby.foldLog("User server launched successfully!");
    }

    @Override
    public void log(String message) {
        if (allLogsEnabled && adminLobby != null) {
            adminLobby.sendLog("UserServer: " + message);
        }
    }

    @Override
    public void commandLog(String message) {
        if (commandLogsEnabled && adminLobby != null) {
            adminLobby.sendLog("UserServer: Command: " + message);
        }
    }

    @Override
    public void errorLog(String message) {
        if (adminLobby != null) {
            adminLobby.sendLog("UserServer: Error: " + message);
        }
    }

    @Override
    public synchronized void shutdown() {
        // Closing all lobbies
        Set <Map.Entry<String, UserLobby>> entries = lobbies.entrySet();
        for (Map.Entry<String, UserLobby> entry: entries) {
            entry.getValue().shutdown(); // shutting down each lobby
        }
        // Closing all unregistered connections
        Set<SelectionKey> keySet = users.keySet();
        for (SelectionKey key : keySet) {
            SocketChannel socketChannel = (SocketChannel)key.channel();
            if (socketChannel.isConnected()) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    errorLog("Error while closing connection (host " +
                            socketChannel.socket().getInetAddress().getHostAddress() + " ): "
                            + e.getMessage() + " !");
                }
            }
        }
        // Closing server itself
        if (serverSocketChannel.isOpen()) {
            try {
                serverSocketChannel.close();
                selector.close();
            }
            catch (IOException e) {
                errorLog("Error while shutting down ServerSocketChannel: " + e.getMessage() + " !");
            }
        }
        log("Server has been shut down!");
    }

    @Override
    public void closeConnection(SelectionKey key) {
        //TODO: reduce closing repetition (see shutdown)
        users.remove(key);
        SocketChannel socketChannel = (SocketChannel)key.channel();
        String userHost = socketChannel.socket().getInetAddress().getHostAddress();
        if (socketChannel.isConnected()) {
            try {
                socketChannel.close();
            }
            catch (IOException e) {
                errorLog("Error while closing connection " + userHost + ": " + e.getMessage());
            }
        }
        key.cancel();
        log("Connection " + userHost + " was closed!");
    }

    @Override
    protected String processReadCommand(ByteBuffer buffer, SelectionKey key, UserConnection
            connection) throws IllegalStateException,
            ClassCastException, ClassNotFoundException, IOException {
        if (connection.getReadCommandType() == ProtocolConstants.SERVER_COMMAND_INDEX) {
            // if it is a command to server, handle it
            UserServerRequest request = (UserServerRequest) DataManagement.inflate(buffer);
            request.handleRequest(this, key, connection);
            return request.getClass().getSimpleName();
        } else {
            // not expected here
            throw new IllegalStateException();
        }
    }

    /***
     * Shutting down lobby internally
     * @param lobbyName lobby name
     */
    public void shutdownLobbyCommand(String lobbyName) {
        UserLobby userLobby = lobbies.get(lobbyName);
        if (userLobby == null) {
            log("No lobby with name " + lobbyName + "!");
            return;
        }
        userLobby.shutdown();
    }

    /***
     * Getting all lobby names with their connection lists
     * @return all lobby names with their connection lists
     */
    public Map<String, ArrayList<UserConnection>> getLobbies() {
        Map<String, ArrayList<UserConnection>> lobbies = new HashMap<String, ArrayList<UserConnection>>();
        Set <Map.Entry<String, UserLobby>> entries = this.lobbies.entrySet();
        for (Map.Entry<String, UserLobby> entry: entries) {
            lobbies.put(entry.getKey(), entry.getValue().getConnectionList());
        }
        return lobbies;
    }

    /***
     * Changing lobby name internally
     * @param oldName old name
     * @param newName new name
     */
    public void changeLobbyNameCommand(String oldName, String newName) {
        UserLobby userLobby = lobbies.get(oldName);
        if (userLobby == null) {
            log("No lobby with name " + oldName + "!");
            return;
        }
        userLobby.changeLobbyName(newName);
    }

    /***
     * Changing lobby password internally
     * @param lobbyName lobby name
     * @param  password new password
     */
    public void changeLobbyPasswordCommand(String lobbyName, byte[] password) {
        UserLobby userLobby = lobbies.get(lobbyName);
        if (userLobby == null) {
            log("No lobby with name " + lobbyName + "!");
            return;
        }
        userLobby.changeLobbyPassword(password);
    }

    /***
     * Changing user name internally
     * @param lobbyName lobby name
     * @param oldName old username
     * @param newName new username
     */
    public void changeUsernameCommand(String lobbyName, String oldName, String newName) {
        UserLobby userLobby = lobbies.get(lobbyName);
        if (userLobby == null) {
            log("No lobby with name " + lobbyName + "!");
            return;
        }
        userLobby.changeUsername(oldName, newName);
    }

    /***
     * Changing user status internally
     * @param lobbyName lobby name
     * @param username user name
     * @param status new status
     */
    public void changeUserStatusCommand(String lobbyName, String username, byte status) {
        UserLobby userLobby = lobbies.get(lobbyName);
        if (userLobby == null) {
            log("No lobby with name " + lobbyName + "!");
            return;
        }
        userLobby.changeUserStatus(username, status);
    }

    /***
     * Kicking user internally
     * @param lobbyName lobby name
     * @param username user name
     */
    public void kickUserCommand(String lobbyName, String username) {
        UserLobby userLobby = lobbies.get(lobbyName);
        if (userLobby == null) {
            log("No lobby with name " + lobbyName + "!");
            return;
        }
        userLobby.kickUser(username);
    }

    /***
     * Banning user internally
     * @param lobbyName lobby name
     * @param username user name
     */
    public void banUserCommand(String lobbyName, String username) {
        UserLobby userLobby = lobbies.get(lobbyName);
        if (userLobby == null) {
            log("No lobby with name " + lobbyName + "!");
            return;
        }
        userLobby.banUser(username);
    }

    /***
     * Unbanning user internally
     * @param lobbyName lobby name
     * @param username user name
     */
    public void unbanUserCommand(String lobbyName, String username) {
        UserLobby userLobby = lobbies.get(lobbyName);
        if (userLobby == null) {
            log("No lobby with name " + lobbyName + "!");
            return;
        }
        userLobby.unbanUser(username);
    }

    /***
     * Removes lobby from list
     * @param lobbyName lobby name
     */
    public void removeLobby(String lobbyName) {
        lobbies.remove(lobbyName);
    }

    /***
     * Checks if it possible to create a requested lobby, creates if true
     * @param key command sender
     * @param connection associated connection
     * @param username requested username
     * @param lobbyInfo info about server
     */
    public void createLobby(SelectionKey key, UserConnection connection, String username, LobbyInfo lobbyInfo) {
        SocketChannel socketChannel = connection.getSocketChannel();
        String userHost = socketChannel.socket().getInetAddress().getHostAddress();
        String lobbyName = lobbyInfo.getLobbyName();
        log("Creating lobby " + lobbyName + " by user " + username + " (host " +
                userHost + ")!");
        String newName = checkLobbyName(lobbyName);
        lobbyInfo.setLobbyName(newName);
        try {
            UserLobby userLobby = null;
            try {
                userLobby = new UserLobby(this, lobbyInfo); // creating new lobby
                userLobby.addUser(new UserConnection(new User(username, Status.USER_STATUS_ROUTE),
                        socketChannel));
                ServerUtil.writeMessageToClient(socketChannel, new AnswerCommand(ProtocolConstants.ANSWER_OK,
                        newName)); // writing message to user
                lobbies.put(newName, userLobby);
                users.remove(key);
                key.cancel(); // unregister user from main selector
                log("Successfully created lobby " + newName + " by user " + username + " (host " +
                        userHost + ")!");
            } catch (IOException e) {
                //TODO: more feedback
                ServerUtil.writeMessageToClient(socketChannel, new AnswerCommand(ProtocolConstants.ANSWER_CANCEL,
                        "Error while creating the lobby!"));
                // writing message to user
                if (userLobby != null) {
                    userLobby.shutdown();
                }
                log("Creating lobby failed: error while creating lobby: " + e.getMessage() + "!");
            }
        }
        catch (IOException e) { // if error occurred while writing to user
            closeConnection(key); // close this connection
            errorLog("Creating lobby failed: Error while writing to user: " + e.getMessage() + "!");
        }
    }

    /***
     * Sends lobby list to user
     * @param key command sender
     * @param connection associated connection
     */
    public void sendLobbyList(SelectionKey key, UserConnection connection) {
        SocketChannel socketChannel = connection.getSocketChannel();
        String userHost = socketChannel.socket().getInetAddress().getHostAddress();
        log("Sending lobby info to host " + userHost + "!");
        ArrayList<LobbyInfo> servers = new ArrayList<LobbyInfo>();
        Set <Map.Entry<String, UserLobby>> entries = lobbies.entrySet();
        for (Map.Entry<String, UserLobby> entry: entries) {
            servers.add(entry.getValue().getLobbyInfo());
        }
        try {
            ServerUtil.writeMessageToClient(socketChannel, new SendLobbyListCommand(servers));
            log("Successfully sent lobby info to host " + userHost + "!");
        }
        catch (IOException e) { // if error occurred while writing to user
            closeConnection(key); // close this connection
            errorLog("Logging in failed: Error while writing to user: " + e.getMessage() + "!");
        }
    }

    /***
     * Tries to login to the user server
     * @param key command sender
     * @param connection associated connection
     * @param username user who sent request
     * @param lobbyInfo info about server
     */
    public void login(SelectionKey key, UserConnection connection, String username, LobbyInfo lobbyInfo) {
        SocketChannel socketChannel = connection.getSocketChannel();
        String lobbyName = lobbyInfo.getLobbyName();
        String userHost = socketChannel.socket().getInetAddress().getHostAddress();
        UserLobby userLobby = lobbies.get(lobbyName);
        log("Logging in to lobby " + lobbyName + " from user " + username + " (host " +
                userHost + ")!");
        try {
            if (userLobby == null) { // no server with such name
                String message = "No lobby with name " + lobbyInfo.getLobbyName() + "!";
                ServerUtil.writeMessageToClient(socketChannel, new AnswerCommand(ProtocolConstants.ANSWER_CANCEL,
                        message)); // writing message to user
                log("Logging in failed: " + message);
                return;
            }
            if (userLobby.checkIfBanned(userHost)) { // checking if user is banned
                String message = username + " is banned on this lobby!";
                ServerUtil.writeMessageToClient(socketChannel, new AnswerCommand(ProtocolConstants.ANSWER_CANCEL,
                        message)); // writing message to user
                log("Logging in failed: " + message);
                return;
            }
            if (! equalHostsAllowed && userLobby.checkUserOccurrence(socketChannel)) { // checking user existence
                String message = socketChannel.socket().getInetAddress().getHostAddress() +
                        " already exists in the lobby!";
                ServerUtil.writeMessageToClient(socketChannel, new AnswerCommand(ProtocolConstants.ANSWER_CANCEL,
                        message)); // writing message to user
                log("Logging in failed: " + message);
                return;
            }
            if (! userLobby.checkLobbyInfo(lobbyInfo)) { // if data is incorrect
                String message = "Wrong password!";
                ServerUtil.writeMessageToClient(socketChannel, new AnswerCommand(ProtocolConstants.ANSWER_CANCEL,
                        message)); // writing message to user
                log("Logging in failed: " + message);
                return;
            }
            users.remove(key);
            key.cancel(); // unregister user from main selector
            UserConnection newUser = new UserConnection(new User(username, Status.USER_STATUS_COMMON_USER),
                    socketChannel);
            userLobby.addUser(newUser); // registering user
            log(newUser.toString() + " has successfully connected to lobby " +
                    lobbyName + "!");
            ServerUtil.writeMessageToClient(socketChannel, new AnswerCommand(ProtocolConstants.ANSWER_OK,
                    "")); // writing message to user
        }
        catch (IOException e) { // if error occurred while writing to user
            closeConnection(key); // close this connection
            errorLog("Logging in failed: Error while writing to user: " + e.getMessage() + "!");
        }
    }

    /***
     * Checks lobby names and changes it if collisions occur
     * @param name requested name
     * @return old name if no collisions, new name otherwise
     */
    public String checkLobbyName(String name) {
        //TODO: see map - list converting
        ArrayList<String> lobbyNames = new ArrayList<String>();
        Set <Map.Entry<String, UserLobby>> entries = lobbies.entrySet();
        for (Map.Entry<String, UserLobby> entry: entries) {
            lobbyNames.add(entry.getKey());
        }
        return ServerUtil.checkNameCollisions(name, lobbyNames);
    }

    /***
     * Changing lobby name
     * @param oldName old name
     * @param newName new name
     */
    public void changeLobbyName(String oldName, String newName) {
        UserLobby userLobby = lobbies.get(oldName);
        lobbies.remove(oldName);
        lobbies.put(newName, userLobby);
    }

    public void setAdminLobby(AdminLobby adminLobby) {
        this.adminLobby = adminLobby;
    }
}