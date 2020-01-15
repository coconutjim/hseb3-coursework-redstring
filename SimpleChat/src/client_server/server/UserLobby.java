package client_server.server;

import client_server.LobbyInfo;
import client_server.User;
import client_server.UserConnection;
import client_server.protocol.ProtocolConstants;
import client_server.protocol.command_to_client.from_node.NotificationCommand;
import client_server.protocol.command_to_client.from_node.from_lobby.*;
import client_server.protocol.request_to_node.to_lobby.UserLobbyRequest;
import client_server.server.util.Status;
import util.DataManagement;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Represents a lobby with users
 */
public class UserLobby extends Lobby {

    /** Server info */
    private LobbyInfo lobbyInfo;

    //TODO: see structure
    /** Ban list (hosts and user names) */
    private Map<String, String> banList;

    /** Link to server */
    private UserServer userServer;

    /***
     * Constructor
     * @param userServer link to server
     * @param lobbyInfo info about this lobby
     * @throws IOException if something went wrong
     */
    public UserLobby(UserServer userServer, LobbyInfo lobbyInfo) throws IOException {
        if (userServer == null) {
            throw new NullPointerException("Lobby: userServer is null!");
        }
        if (lobbyInfo == null) {
            throw new NullPointerException("Lobby: lobbyInfo is null!");
        }
        selector = Selector.open();
        this.userServer = userServer;
        this.lobbyInfo = lobbyInfo;
        banList = new ConcurrentHashMap<String, String>(); // concurrency
        start();
    }

    @Override
    public void start() {
        new Thread(this).start();
    }

    @Override
    public void log(String message) {
        userServer.log("Lobby " + lobbyInfo.getLobbyName() + ": " + message);
    }

    @Override
    public void commandLog(String message) {
        userServer.commandLog("Lobby " + lobbyInfo.getLobbyName() + ": " + message);
    }

    @Override
    public void errorLog(String message) {
        userServer.errorLog("Lobby " + lobbyInfo.getLobbyName() + ": " + message);
    }

    @Override
    public synchronized void shutdown() {
        //TODO: reduce closing repetition
        // Closing all connections
        Set<SelectionKey> keySet = users.keySet();
        for (SelectionKey key : keySet) {
            SocketChannel socketChannel = (SocketChannel)key.channel();
            if (socketChannel.isConnected()) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    errorLog("Error while closing " + users.get(key).toString()  + ": "
                            + e.getMessage() + " !");
                }
            }
        }

        // Closing lobby itself
        try {
            selector.close();
        }
        catch (IOException e) {
            errorLog("Error while closing lobby selector: " + e.getMessage() + "!");
        }
        userServer.removeLobby(lobbyInfo.getLobbyName());
        log("Lobby " + lobbyInfo.getLobbyName() + " was closed!");
    }

    @Override
    protected void closeConnection(SelectionKey key) {
        //TODO: reduce closing repetition
        UserConnection userConnection = users.get(key);
        users.remove(key);
        SocketChannel socketChannel = (SocketChannel)key.channel();
        if (socketChannel.isConnected()) {
            try {
                socketChannel.close();
            }
            catch (IOException e) {
                errorLog("Error while closing connection to " + userConnection.toString() + ": " + e.getMessage());
            }
        }
        key.cancel();
        if (users.isEmpty()) { // if no users, shutdown
            shutdown();
            return;
        }
        String message = userConnection.toString() + " disconnected!";
        addLobbyCommand(new NotificationCommand(message),
                new CommandReceiver(Status.USER_STATUS_READONLY));
        log(message);
        if (userConnection.getStatus() == Status.USER_STATUS_ROUTE) { // if disconnected user is route, delegate status
            findNewRoute(null); // anybody
        }
        sendUserListToAll();
    }

    @Override
    protected String processReadCommand(ByteBuffer buffer, SelectionKey key, UserConnection
            connection) throws IllegalStateException,
            ClassCastException, ClassNotFoundException, IOException {
        String commandtype;
        if (connection.getReadCommandType() == ProtocolConstants.LOBBY_COMMAND_INDEX) {
            // if it is a command to lobby, handle it
            UserLobbyRequest request = (UserLobbyRequest) DataManagement.inflate(buffer);
            commandtype = request.getClass().getSimpleName();
            request.handleRequest(this, key, connection);
        } else {
            commandtype = "client command";
            // get ready to transfer it to others
            Set<Map.Entry<SelectionKey, UserConnection>> entries1 = users.entrySet();
            for (Map.Entry<SelectionKey, UserConnection> entry : entries1) {
                SelectionKey userKey = entry.getKey();
                // through all users
                if (userKey.equals(key)) {
                    // for not writing to the command sender
                    continue;
                }
                // setting this command to others
                UserConnection receiver = entry.getValue();
                receiver.addCommandToWrite(buffer);
            }
        }
        return commandtype;
    }

    @Override
    protected synchronized void registerNewUsers() throws IOException {
        //TODO: reduce closing repetition
        if (newUsers.isEmpty()) {
            return;
        }
        for (UserConnection userConnection : newUsers) {
            SelectionKey key = userConnection.getSocketChannel().register(selector, SelectionKey.OP_READ);
            userConnection.setKey(key);
            // checking name collisions
            String oldName = userConnection.getUsername();
            String newName = checkUsername(oldName);
            if (! oldName.equals(newName)) {
                String message1 = "Due to collisions avoiding " + userConnection.toString() + " name " +
                        "was changed to " + newName;
                userConnection.setUsername(newName);
                addLobbyCommand(new ChangeUsernameCommand(newName), new CommandReceiver(key));
                addLobbyCommand(new NotificationCommand(message1),
                        new CommandReceiver(Status.USER_STATUS_READONLY));
                log(message1);
            }
            String message = userConnection.toString() + " connected!";
            addLobbyCommand(new NotificationCommand(message),
                    new CommandReceiver(Status.USER_STATUS_READONLY));
            log(message);
            users.put(key, userConnection);
        }
        sendUserListToAll();
        newUsers.clear();
    }

    /***
     * Checks lobby info
     * @param lobbyInfo requested lobby info
     * @return if checking is successful
     */
    public synchronized boolean checkLobbyInfo(LobbyInfo lobbyInfo) {
        return this.lobbyInfo.equals(lobbyInfo);
    }

    /***
     * Checks if the host is banned
     * @param host host
     * @return true id the host is banned, false otherwise
     */
    public boolean checkIfBanned(String host) {
        Set<String> hosts = banList.keySet();
        for (String bannedHost : hosts) {
            if (host.equals(bannedHost)) { // checking if this host is banned
                return true;
            }
        }
        return false;
    }

    /***
     * Returns info about server (without password)
     * @return server info without password
     */
    public LobbyInfo getLobbyInfo() {
        return lobbyInfo.getInfoForUser();
    }

    /***
     * Forms user list for users
     * @param hosts if list must contain host for each user (it depends on the sender's status)
     * @return user list
     */
    private Map<User, String> getUserListToSend(boolean hosts) {
        Map<User, String> userList = new HashMap<User, String>();
        Set <Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, UserConnection> entry: entries) {
            UserConnection connection = entry.getValue();
            String host = hosts ? connection.getSocketChannel().socket().getInetAddress().getHostAddress() : null;
            userList.put(connection.getUser(), host);
        }
        return userList;
    }

    /***
     * Sends user list
     * @param sender command sender
     * @param connection associated connection
     */
    public void sendUserList(SelectionKey sender, UserConnection connection) {
        Map<User, String> userList = getUserListToSend(connection.getStatus() >= Status.USER_STATUS_MODERATOR);
        addLobbyCommand(new SetUserListCommand(userList), new CommandReceiver(sender));
    }

    /***
     * Sends user list to all users
     */
    public void sendUserListToAll() {
        Set<Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, UserConnection> entry : entries) {
            sendUserList(entry.getKey(), entry.getValue());
        }
    }

    /***
     * Sends ban list
     * @param sender command sender
     */
    public void sendBanList(SelectionKey sender) {
        addLobbyCommand(new SetBanListCommand(banList), new CommandReceiver(sender));
    }

    /***
     * Sends ban list to all
     */
    public void sendBanListToAll() {
        addLobbyCommand(new SetBanListCommand(banList), new CommandReceiver(Status.USER_STATUS_MODERATOR));
    }

    /***
     * Getting user connections list
     * @return user connections list
     */
    public ArrayList<UserConnection> getConnectionList() {
        ArrayList<UserConnection> connections = new ArrayList<UserConnection>();
        Set <Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, UserConnection> entry: entries) {
            connections.add(entry.getValue());
        }
        return connections;
    }

    /***
     * Changing lobby name by user
     * @param lobbyName new lobby name
     * @param connection associated connection
     */
    public void changeLobbyName(String lobbyName, UserConnection connection) {
        if (connection.getStatus() < Status.USER_STATUS_MODERATOR) { // check sender status
            log("Changing lobby name from illegal user: " + connection.toString() + "!");
            return;
        }
        String message = connection.toString() + " changed lobby name to ";
        changeLobbyName(lobbyName, message);
    }

    /***
     * Changing lobby name internally
     * @param lobbyName new lobby name
     */
    public void changeLobbyName(String lobbyName) {
        String message = "Lobby name was internally changed to ";
        changeLobbyName(lobbyName, message);
    }

    /***
     * Changing lobby name (checks collisions)
     * @param lobbyName new lobby name
     * @param message message to users
     */
    private void changeLobbyName(String lobbyName, String message) {
        if (lobbyName.equals(lobbyInfo.getLobbyName())) {
            return;
        }
        String newName = userServer.checkLobbyName(lobbyName);
        userServer.changeLobbyName(lobbyInfo.getLobbyName(), newName);
        lobbyInfo.setLobbyName(newName);
        message += newName + "!";
        addLobbyCommand(new ChangeLobbyNameCommand(newName), new CommandReceiver(Status.USER_STATUS_READONLY));
        addLobbyCommand(new NotificationCommand(message),
                new CommandReceiver(Status.USER_STATUS_READONLY)); // notification
        log(message);
    }

    /***
     * Changing lobby password by user
     * @param password new password
     * @param connection associated connection
     */
    public void changeLobbyPassword(byte[] password, UserConnection connection) {
        //TODO: checks
        if (connection.getStatus() < Status.USER_STATUS_MODERATOR) { // check sender status
            log("Changing lobby password from illegal user: " + connection.toString() + "!");
            return;
        }
        String message = connection.getUsername() + " changed lobby password!";
        changeLobbyPassword(password, message);
    }

    /***
     * Changing lobby password internally
     * @param password new password
     */
    public void changeLobbyPassword(byte[] password) {
        String message = "Lobby password was changed internally!";
        changeLobbyPassword(password, message);
    }

    /***
     * Changing lobby password
     * @param password new password
     * @param message message to users
     */
    private void changeLobbyPassword(byte[] password, String message) {
        if (Arrays.equals(password, lobbyInfo.getPassword())) {
            return;
        }
        lobbyInfo.setPassword(password); // changing it here
        //TODO: send new password?
        addLobbyCommand(new NotificationCommand(message),
                new CommandReceiver(Status.USER_STATUS_MODERATOR)); // notification
        log(message);
    }

    /***
     * Changing username (checks collisions)
     * @param name new name
     * @param sender command sender
     * @param connection associated connection
     */
    public void changeUsername(String name, SelectionKey sender, UserConnection connection) {
        String oldName = connection.getUsername();
        String message = connection.toString() + " changed name to ";
        changeUsername(sender, connection, oldName, name, message);

    }

    /***
     * Changing username internally
     * @param oldName old username
     * @param newName new username
     */
    public void changeUsername(String oldName, String newName) {
        SelectionKey targetKey = getUser(oldName);
        UserConnection target = users.get(targetKey);
        if (target == null) {
            String message = "Not found user " + oldName + "!";
            log(message);
            return;
        }
        String message = target.toString() + " name was internally changed to ";
        changeUsername(targetKey, target, oldName, newName, message);
    }

    /***
     * Changing username
     * @param key associated key
     * @param connection target user
     * @param oldName old username
     * @param name new username
     * @param message message to users
     */
    private void changeUsername(SelectionKey key, UserConnection connection,String oldName,
                                String name, String message) {
        //TODO: more checks
        if (name == null || name.equals("")) {
            log("Lobby.changeUsername: Illegal username!");
            return;
        }
        if (name.equals(oldName)) { // if new name is equals to the old one
            return;
        }
        String newName = checkUsername(name); // checking collisions
        message += newName + "!";
        connection.setUsername(newName); // changing name here
        addLobbyCommand(new ChangeUsernameCommand(newName),
                new CommandReceiver(key)); // changing it on the client
        addLobbyCommand(new NotificationCommand(message),
                new CommandReceiver(Status.USER_STATUS_READONLY)); // notification
        log(message);
        sendUserListToAll();
    }

    /***
     * Changing user status by user
     * @param username target user
     * @param status new status
     * @param sender command sender
     * @param connection associated connection
     */
    public void changeUserStatus(String username, byte status, SelectionKey sender, UserConnection connection) {
        SelectionKey targetKey = getUser(username);
        UserConnection target = users.get(targetKey);
        if (target == null) {
            String message = "Not found user " + username + "!";
            addLobbyCommand(new NotificationCommand(message),
                    new CommandReceiver(sender)); // notification
            log(message);
            return;
        }
        if (status < Status.USER_STATUS_READONLY || status > Status.USER_STATUS_ROUTE) {
            log("Illegal status while changing user " + target.toString() + " status from" +
                    connection.toString() + "!");
            return;
        }
        if (connection.getStatus() < Status.USER_STATUS_MODERATOR ||
                target.getStatus() == Status.USER_STATUS_ROUTE) { // check sender status and target status
            log("Changing " + target.toString() + " status from illegal user: " +
                    connection.toString() + "!");
            if (target.getStatus() == Status.USER_STATUS_ROUTE) {
                addLobbyCommand(new NotificationCommand("You can not change user status of lobby route!"),
                        new CommandReceiver(sender)); // notification to sender
            }
            return;
        }
        String message = connection.toString() + " changed " + target.toString() + " status to " +
                Status.STATUS_STRINGS.get(status) + "!";
        changeUserStatus(targetKey, target, status, message);
    }

    /***
     * Changing user status internally
     * @param username target user
     * @param status new status
     */
    public void changeUserStatus(String username, byte status) {
        SelectionKey targetKey = getUser(username);
        UserConnection target = users.get(targetKey);
        if (target == null) {
            String message1 = "Not found user " + username + "!";
            log(message1);
            return;
        }
        if (status == Status.USER_STATUS_ROUTE) { // if route delegating occurs
            delegateRoute(username);
            return;
        }
        if (status < Status.USER_STATUS_READONLY || status > Status.USER_STATUS_ROUTE) {
            log("Illegal status while changing user status internally!");
            return;
        }
        boolean findNewRoute = target.getStatus() == Status.USER_STATUS_ROUTE; // if finding new route needed
        if (findNewRoute && users.size() == 1) { // if nothing can be changed
            log("The only user in the lobby can not be lowered in status!");
            return;
        }
        String message = target.toString()  + " status was internally changed to " +
                Status.STATUS_STRINGS.get(status) + "!";
        changeUserStatus(targetKey, target, status, message);
        if (findNewRoute) {
            findNewRoute(targetKey); // anybody except this user
        }
    }

    /***
     * Changing user status
     * @param targetKey associated key
     * @param target target user
     * @param status new status
     * @param message message to users
     */
    private void changeUserStatus(SelectionKey targetKey, UserConnection target, byte status, String message) {
        if (status > Status.USER_STATUS_ROUTE || status < Status.USER_STATUS_READONLY) {
            log("Lobby.changeUserStatus: Illegal status!");
            return;
        }
        if (target.getStatus() == status) {
            return;
        }
        target.setStatus(status); // changing status here
        addLobbyCommand(new ChangeUserStatusCommand(status), new CommandReceiver(targetKey));
        // changing it on the client
        if (status >= Status.USER_STATUS_MODERATOR) {
            sendBanList(targetKey);
        }
        addLobbyCommand(new NotificationCommand(message),
                new CommandReceiver(Status.USER_STATUS_READONLY)); // notification
        log(message);
        sendUserListToAll();
    }

    /***
     * Delegating route status by user
     * @param username target user
     * @param sender command sender
     * @param connection associated connection
     */
    public void delegateRoute(String username, SelectionKey sender, UserConnection connection) {
        SelectionKey targetKey = getUser(username);
        UserConnection target = users.get(targetKey);
        if (target == null) {
            String message = "Not found user " + username + "!";
            addLobbyCommand(new NotificationCommand(message),
                    new CommandReceiver(sender)); // notification
            log(message);
            return;
        }
        if (connection.getStatus() < Status.USER_STATUS_ROUTE) { // check sender status
            log("Making " + target.toString() + " route from illegal user: " +
                    connection.toString() + "!");
            return;
        }
        String message = connection.getUsername() + " made " + target.toString() + " lobby route!";
        delegateRoute(targetKey, target, sender, connection, message);
    }

    /***
     * Delegating lobby route internally
     * @param username new lobby route
     */
    public void delegateRoute(String username) {
        SelectionKey targetKey = getUser(username);
        UserConnection target = users.get(targetKey);
        if (target == null) {
            String message = "Not found user " + username + "!";
            log(message);
            return;
        }
        // finding route
        Set <Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, UserConnection> entry: entries) {
            if (entry.getValue().getStatus() == Status.USER_STATUS_ROUTE) {
                String message = entry.getValue().toString() + " was internally made new lobby route!";
                delegateRoute(targetKey, target, entry.getKey(), entry.getValue(), message);
            }
        }
    }

    /***
     * Delegating lobby route
     * @param targetKey new route key
     * @param target new route
     * @param routeKey old route key
     * @param route old route
     * @param message message to users
     */
    private void delegateRoute(SelectionKey targetKey, UserConnection target, SelectionKey routeKey,
                               UserConnection route, String message) {
        if (target.equals(route)) {
            return;
        }
        route.setStatus(Status.USER_STATUS_MODERATOR); // down
        addLobbyCommand(new ChangeUserStatusCommand(Status.USER_STATUS_MODERATOR),
                new CommandReceiver(routeKey)); // down on the client
        target.setStatus(Status.USER_STATUS_ROUTE); // up
        addLobbyCommand(new ChangeUserStatusCommand(Status.USER_STATUS_ROUTE),
                new CommandReceiver(targetKey)); // up on the client
        addLobbyCommand(new NotificationCommand(message),
                new CommandReceiver(Status.USER_STATUS_READONLY)); // notification
        sendUserListToAll();
        log(message);
    }

    /***
     * Kicking user by user
     * @param username target user
     * @param sender command sender
     * @param connection associated connection
     */
    public void kickUser(String username, SelectionKey sender, UserConnection connection) {
        SelectionKey targetKey = getUser(username);
        UserConnection target = users.get(targetKey);
        if (target == null) {
            String message = "Not found user " + username + "!";
            addLobbyCommand(new NotificationCommand(message),
                    new CommandReceiver(sender)); // notification
            log(message);
            return;
        }
        if (connection.getStatus() < Status.USER_STATUS_MODERATOR ||
                target.getStatus() == Status.USER_STATUS_ROUTE) { // check sender status and target status
            log("Kicking " + target.toString() + " from illegal user: " +
                    connection.toString() + "!");
            if (target.getStatus() == Status.USER_STATUS_ROUTE) {
                addLobbyCommand(new NotificationCommand("You can not kick lobby route!"),
                        new CommandReceiver(sender)); // notification to sender
            }
            return;
        }
        String message = connection.toString() + " kicked " + target.toString() + "!";
        kickUser(targetKey, message);

    }

    /***
     * Kicking user internally
     * @param username target user
     */
    public void kickUser(String username) {
        SelectionKey targetKey = getUser(username);
        UserConnection target = users.get(targetKey);
        if (target == null) {
            String message = "Not found user " + username + "!";
            log(message);
            return;
        }
        String message = target.toString() + " was kicked internally!";
        kickUser(targetKey, message);
    }

    /***
     * Kicking user
     * @param targetKey target user key
     * @param message message to users
     */
    private void kickUser(SelectionKey targetKey, String message) {
        addLobbyCommand(new KickCommand(), new CommandReceiver(targetKey)); // kick him
        addLobbyCommand(new NotificationCommand(message),
                new CommandReceiver(Status.USER_STATUS_READONLY)); // notification
        log(message);
    }

    /***
     * Banning user
     * @param username target user
     * @param sender command sender
     * @param connection associated connection
     */
    public void banUser(String username, SelectionKey sender, UserConnection connection) {
        SelectionKey targetKey = getUser(username);
        UserConnection target = users.get(targetKey);
        if (target == null) {
            String message = "Not found user " + username + "!";
            addLobbyCommand(new NotificationCommand(message),
                    new CommandReceiver(sender)); // notification
            log(message);
            return;
        }
        if (connection.getStatus() < Status.USER_STATUS_MODERATOR ||
                target.getStatus() == Status.USER_STATUS_ROUTE) { // check sender status and target status
            log("Banning " + target.toString() + " from illegal user: " +
                    connection.toString() + "!");
            if (target.getStatus() == Status.USER_STATUS_ROUTE) {
                addLobbyCommand(new NotificationCommand("You can not ban lobby route!"),
                        new CommandReceiver(sender)); // notification to sender
            }
            return;
        }
        String message = connection.toString() + " banned " + target.toString() + "!";
        banUser(targetKey, target, message);

    }

    /***
     * Banning user internally
     * @param username target user
     */
    public void banUser(String username) {
        SelectionKey targetKey = getUser(username);
        UserConnection target = users.get(targetKey);
        if (target == null) {
            String message = "Not found user " + username + "!";
            log(message);
            return;
        }
        String message = target.toString() + " was banned internally!";
        banUser(targetKey, target, message);
    }

    /***
     * Banning user
     * @param targetKey target user key
     * @param target target user
     * @param message message to users
     */
    private void banUser(SelectionKey targetKey, UserConnection target, String message) {
        //TODO: see banList structure
        banList.put(target.getSocketChannel().socket().getInetAddress().getHostAddress(),
                target.getUsername()); // ban him here
        addLobbyCommand(new BanCommand(), new CommandReceiver(targetKey)); // ban him on the client
        addLobbyCommand(new NotificationCommand(message),
                new CommandReceiver(Status.USER_STATUS_READONLY)); // notification
        log(message);
        sendBanListToAll();
    }

    /***
     * Unbanning user by user
     * @param username target user
     * @param sender command sender
     * @param connection associated connection
     */
    public void unbanUser(String username, SelectionKey sender, UserConnection connection) {
        if (connection.getStatus() < Status.USER_STATUS_MODERATOR) { // check sender status
            log("Unbanning " + username + " from illegal user: " +
                    connection.toString() + "!");
            return;
        }
        String message = connection.toString() + " unbanned " + username + "!";
        if (! unbanUser(username, message)) {
            String message1 = "Not found user " + username + "in the ban list!";
            addLobbyCommand(new NotificationCommand(message1),
                    new CommandReceiver(sender)); // notification
            log(message1);
        }
    }

    /***
     * Unbanning user internally
     * @param username target user
     */
    public void unbanUser(String username) {
        String message = username + " was internally unbanned!";
        if (! unbanUser(username, message)) {
            String message1 = "Not found user " + username + " in the ban list!";
            log(message1);
        }
    }

    /***
     * Unbanning user
     * @param username target user
     * @param message message to users
     * @return true if operation is successful, false if user was not found
     */
    private boolean unbanUser(String username, String message) {
        Set <Map.Entry<String, String>> entries = banList.entrySet();
        for (Map.Entry<String, String> entry: entries) {
            //TODO: see banList structure
            if (entry.getValue().equals(username)) {
                banList.remove(entry.getKey());
                addLobbyCommand(new NotificationCommand(message),
                        new CommandReceiver(Status.USER_STATUS_READONLY)); // notification
                log(message);
                sendBanListToAll();
                return true;
            }
        }
        return false;
    }

    /***
     * Finds new lobby route in case of route disconnection
     * @param notRoute user that can not be new route. May be null
     */
    private void findNewRoute(SelectionKey notRoute) {
        Set <Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        // seeking moderators
        for (Map.Entry<SelectionKey, UserConnection> entry: entries) {
            SelectionKey key = entry.getKey();
            UserConnection target = entry.getValue();
            if (target.getStatus() == Status.USER_STATUS_MODERATOR && (notRoute == null ||
                    ! notRoute.equals(key))) {
                makeRoute(entry.getKey(), target);
                return;
            }
        }
        // if no, seeking common users
        for (Map.Entry<SelectionKey, UserConnection> entry: entries) {
            SelectionKey key = entry.getKey();
            UserConnection target = entry.getValue();
            if (target.getStatus() == Status.USER_STATUS_COMMON_USER && (notRoute == null ||
                    ! notRoute.equals(key))) {
                makeRoute(key, target);
                return;
            }
        }
        // if no, seeking read only users
        for (Map.Entry<SelectionKey, UserConnection> entry: entries) {
            SelectionKey key = entry.getKey();
            UserConnection target = entry.getValue();
            if (notRoute == null || ! notRoute.equals(key)) {
                makeRoute(key, target);
            }
            return;
        }
    }

    /***
     * Making new lobby route
     * @param key associated key
     * @param connection target user
     */
    private void makeRoute(SelectionKey key, UserConnection connection) {
        String message = connection.toString() + " is new lobby route!";
        connection.setStatus(Status.USER_STATUS_ROUTE);
        addLobbyCommand(new ChangeUserStatusCommand(Status.USER_STATUS_ROUTE),
                new CommandReceiver(key));
        sendBanList(key);
        addLobbyCommand(new NotificationCommand(message),
                new CommandReceiver(Status.USER_STATUS_READONLY));
        sendUserListToAll();
        log(message);
    }

    /***
     * Returns key for current user connection
     * @param username username
     * @return key if this user exists, null otherwise
     */
    protected SelectionKey getUser(String username) {
        Set <Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, UserConnection> entry: entries) {
            if (entry.getValue().getUsername().equals(username)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
