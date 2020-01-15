package rslib.cs.server.user;

import rslib.cs.common.*;
import rslib.cs.protocol.ProtocolConstants;
import rslib.cs.protocol.events.AnswerCommand;
import rslib.cs.protocol.events.SendLobbyListCommand;
import rslib.cs.protocol.requests.to_server.user.CreateLobbyRequest;
import rslib.cs.protocol.requests.to_server.user.LoginRequest;
import rslib.cs.protocol.requests.to_server.user.UserServerRequest;
import rslib.util.DataManagement;
import rslib.cs.server.Server;
import rslib.cs.server.admin.AdminLobby;
import rslib.cs.server.admin.AdminServer;
import rslib.cs.server.util.LobbySession;
import rslib.cs.server.util.ServerUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.nio.channels.*;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Represents main server for users
 */
public class UserServer extends Server  {

    /** Lobbies (String for lobby name) */
    private Map<String, UserLobby> lobbies;

    /** Link to admin lobby */
    private AdminLobby adminLobby;

    /** Link to admin server */
    private AdminServer adminServer;

    /***
     * The constructor. Starts the server
     * @param adminServer  link to main server (for logs)
     * @throws IOException if something went wrong
     */
    public UserServer(AdminServer adminServer) throws IOException {
        super(ConnectConfiguration.USER_PORT);
        if (adminServer == null) {
            throw new IllegalArgumentException("UserServer: adminServer is null!");
        }
        this.adminServer  = adminServer;
        lobbies = new ConcurrentHashMap<>(); // concurrency
    }

    @Override
    public void start() {
        new Thread(this).start();
        new Thread(executor).start();
        adminServer.foldLog("User server launched successfully!");
    }

    @Override
    public void log(String message) {
        String mess = "UserServer: " + message;
        if (allLogsEnabled && adminLobby != null) {
            adminLobby.sendLog(mess, false);
        }
        adminServer.foldLog(mess);
    }

    @Override
    public void commandLog(String message) {
        String mess = "UserServer: Command: " + message;
        if (commandLogsEnabled && adminLobby != null) {
            adminLobby.sendLog(mess, false);
        }
        adminServer.foldLog(mess);
    }

    @Override
    public void errorLog(String message) {
        String mess = "UserServer: Error: " + message;
        if (adminLobby != null) {
            adminLobby.sendLog(mess, false);
        }
        adminServer.foldLog(mess);
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
    protected void processReadCommand(CommandInfo command, UserConnection
            connection) throws IllegalStateException,
            ClassCastException, ClassNotFoundException, IOException {
        ByteBuffer buffer = command.getCommand();
        byte commandType = command.getCommandType();
        if (commandType == ProtocolConstants.CONNECT_INDEX) {
            // if it is a command to server, handle it
            UserServerRequest request = (UserServerRequest) DataManagement.inflate(buffer);
            commandLog("Received " + request.getClass().getSimpleName() + " from " + connection.getHost() + "!");
            handleRequest(request, connection.getKey(), connection);
        } else {
            // not expected here
            throw new IllegalStateException();
        }
    }

    /***
     * Processes server request
     * @param request request
     * @param key request sender
     * @param connection associated connection
     */
    private void handleRequest(UserServerRequest request, SelectionKey key, UserConnection connection) {
        switch (request.getIndex()) {
            case LOGIN_R: {
                LoginRequest loginRequest = (LoginRequest) request;
                login(key, connection, loginRequest.getUsername(), loginRequest.getLobbyInfo());
                break;
            }
            case CREATE_LOBBY_R: {
                CreateLobbyRequest createLobbyRequest = (CreateLobbyRequest) request;
                createLobby(key, connection, createLobbyRequest.getUsername(), createLobbyRequest.getLobbyInfo());
                break;
            }
            case SEND_LOBBY_LIST_R: {
                sendLobbyList(key, connection);
            }
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
     * Getting all lobby sessions
     * @return all lobby sessions
     */
    public ArrayList<LobbySession> getLobbies() {
        ArrayList<LobbySession> lobbySessions = new ArrayList<>();
        Set <Map.Entry<String, UserLobby>> entries = this.lobbies.entrySet();
        for (Map.Entry<String, UserLobby> entry: entries) {
            lobbySessions.add(entry.getValue().getSession());
        }
        return lobbySessions;
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
    public void changeUserStatusCommand(String lobbyName, String username, Status status) {
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
     * Creating lobby
     * @param key request sender
     * @param connection associated connection
     * @param username user name
     * @param lobbyInfo info about lobby
     */
    private void createLobby(SelectionKey key, UserConnection connection, String username, LobbyInfo lobbyInfo) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        String userHost = connection.getHost();
        String lobbyName = lobbyInfo.getLobbyName();
        log("Creating lobby " + lobbyName + " by user " + username + " (host " +
                userHost + ")!");
        String newName = checkLobbyName(lobbyName);
        lobbyInfo.setLobbyName(newName);
        try {
            UserLobby userLobby = null;
            try {
                if (! DataChecking.isUsernameValid(username)) {
                    ServerUtil.writeMessageToClient(socketChannel, new AnswerCommand(ProtocolConstants.ANSWER_CANCEL,
                            DataChecking.USERNAME_RULES)); // writing message to user
                    return;
                }
                if (! DataChecking.isLobbyNameValid(lobbyInfo.getLobbyName())) {
                    ServerUtil.writeMessageToClient(socketChannel, new AnswerCommand(ProtocolConstants.ANSWER_CANCEL,
                            DataChecking.LOBBY_NAME_RULES)); // writing message to user
                    return;
                }
                userLobby = new UserLobby(this, lobbyInfo); // creating new lobby
                userLobby.addUser(new UserConnection(new User(username, Status.LOBBY_ROOT),
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
            errorLog("Creating lobby failed: Error while writing to user: " + e.getMessage() + "!");
            closeConnection(key); // close this connection
        }
    }

    /***
     * Sending lobby list
     * @param key target
     * @param connection associated connection
     */
    private void sendLobbyList(SelectionKey key, UserConnection connection) {
        SocketChannel socketChannel = (SocketChannel)key.channel();
        String userHost = connection.getHost();
        log("Sending lobby info to host " + userHost + "!");
        ArrayList<LobbyInfo> servers = new ArrayList<>();
        Set <Map.Entry<String, UserLobby>> entries = lobbies.entrySet();
        for (Map.Entry<String, UserLobby> entry: entries) {
            servers.add(entry.getValue().getLobbyInfo());
        }
        try {
            ServerUtil.writeMessageToClient(socketChannel, new SendLobbyListCommand(servers));
            log("Successfully sent lobby info to host " + userHost + "!");
        }
        catch (IOException e) { // if error occurred while writing to user
            errorLog("Sending lobby list failed: Error while writing to user: " + e.getMessage() + "!");
            closeConnection(key); // close this connection
        }
    }

    /***
     * Represents logging in to user lobby
     * @param key request sender
     * @param connection associated connection
     * @param username user name
     * @param lobbyInfo info about lobby
     */
    private void login(SelectionKey key, UserConnection connection, String username, LobbyInfo lobbyInfo) {
        SocketChannel socketChannel = (SocketChannel)key.channel();
        String lobbyName = lobbyInfo.getLobbyName();
        String userHost = connection.getHost();
        UserLobby userLobby = lobbies.get(lobbyName);
        log("Logging in to lobby " + lobbyName + " from user " + username + " (host " +
                userHost + ")!");
        try {
            if (! DataChecking.isUsernameValid(username)) {
                ServerUtil.writeMessageToClient(socketChannel, new AnswerCommand(ProtocolConstants.ANSWER_CANCEL,
                        DataChecking.USERNAME_RULES)); // writing message to user
                return;
            }
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
            UserConnection newUser = new UserConnection(new User(username, Status.COMMON),
                    socketChannel);
            userLobby.addUser(newUser); // registering user
            ServerUtil.writeMessageToClient(socketChannel, new AnswerCommand(ProtocolConstants.ANSWER_OK,
                    "")); // writing message to user
            users.remove(key);
            key.cancel(); // unregister user from main selector
            log(newUser.toString() + " has successfully connected to lobby " +
                    lobbyName + "!");
        }
        catch (IOException e) { // if error occurred while writing to user
            errorLog("Logging in failed: Error while writing to user: " + e.getMessage() + "!");
            closeConnection(key); // close this connection
        }
    }

    /***
     * Checks lobby names and changes it if collisions occur
     * @param name requested name
     * @return old name if no collisions, new name otherwise
     */
    public String checkLobbyName(String name) {
        //TODO: see map - list converting
        ArrayList<String> lobbyNames = new ArrayList<>();
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