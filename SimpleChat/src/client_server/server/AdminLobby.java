package client_server.server;

import client_server.UserConnection;
import client_server.protocol.command_to_server_gui.LogCommand;
import client_server.protocol.request_to_node.to_lobby.InternalRequest;
import client_server.server.util.ServerConfiguration;
import client_server.server.util.Status;
import util.DataManagement;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * Represents an API that sends logs and receive internal commands
 */
public class AdminLobby extends Lobby {

    /** Link to user server */
    private UserServer userServer;

    /** Link to admin server */
    private AdminServer adminServer;

    /***
     * Constructor
     * @param adminServer link to API adapter
     * @param userServer link to server
     * @throws IOException if something went wrong
     */
    public AdminLobby(AdminServer adminServer, UserServer userServer) throws IOException {
        if (adminServer == null) {
            throw new NullPointerException("ServerAPI: adminServer is null!");
        }
        if (userServer == null) {
            throw new NullPointerException("ServerAPI: userServer is null!");
        }
        selector = Selector.open();
        this.adminServer = adminServer;
        this.userServer = userServer;
        newUsers = new CopyOnWriteArrayList<UserConnection>(); // concurrency
    }

    @Override
    public void start() {
        new Thread(this).start();
        foldLog("Server API launched successfully!");
    }

    @Override
    public void log(String message) {
        foldLog(message);
    }

    @Override
    public void errorLog(String message) {
        foldLog(message);
    }

    @Override
    public void commandLog(String message) {
        foldLog(message);
    }

    /***
     * Sends logs to admins
     * @param message log message
     */
    public void sendLog(String message) {
        addLobbyCommand(new LogCommand(message), new CommandReceiver(Status.USER_STATUS_ADMINISTRATOR));
        foldLog(message);
    }

    /***
     * Folds log message
     * @param message log message
     */
    public void foldLog(String message) {
        System.out.println(message);
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
                    errorLog("Error while closing " + users.get(key).toString() + ": "
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
        errorLog("Server API was closed!");
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
        String message = userConnection.toString() + " disconnected!";
        sendLog(message);
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
                sendLog(message1);
            }
            String message = userConnection.toString() + " connected!";
            sendLog(message);
            users.put(key, userConnection);
        }
        newUsers.clear();
    }

    @Override
    protected String processReadCommand(ByteBuffer buffer, SelectionKey key, UserConnection
            connection) throws IllegalStateException,
            ClassCastException, ClassNotFoundException, IOException {
        String command = ((InternalRequest) DataManagement.inflate(buffer)).getCommand();
        sendLog("Received internal command \"" + command + "\" from " +
                connection.toString() + "!");
        processCommand(command);
        return "InternalRequest";
    }

    /***
     * Checks password
     * @param password requested password
     * @return if checking is successful
     */
    public synchronized boolean checkPassword(byte[] password) {
        String s = "";
        for (byte b : password) {
            s += b;
        }
        return s.equals(ServerConfiguration.getAdminPassword());
    }


    /***
     * Processes internal command
     * @param command command
     */
    public void processCommand(String command) {
        String[] result = command.split(" ");
        if (result[0].equals("-start") && result.length == 1) {
            if (userServer != null) {
                sendLog("Server is already working!");
                return;
            }
            try {
                userServer = new UserServer();
                userServer.setAdminLobby(this);
                adminServer.setUserServer(userServer);
                userServer.start();
                sendLog("User server launched successfully!");
            }
            catch (IOException e) {
                sendLog("Error while launching user server: " + e.getMessage());
            }
            return;
        }
        if (result[0].equals("-shutdown") && result.length == 1) {
            if (userServer == null) {
                sendLog("No server is running!");
                return;
            }
            userServer.shutdown();
            userServer = null;
            return;
        }
        if (result[0].equals("-shutdownlobby") && result.length == 2) {
            userServer.shutdownLobbyCommand(result[1]);
            return;
        }
        if (result[0].equals("-lobbies") && result.length == 1) {
            Map<String, ArrayList<UserConnection>> lobbies = userServer.getLobbies();
            Set<Map.Entry<String, ArrayList<UserConnection>>> entries = lobbies.entrySet();
            for (Map.Entry<String, ArrayList<UserConnection>> entry: entries) {
                sendLog("Lobby: " + entry.getKey());
                sendLog("Users: ");
                ArrayList<UserConnection> connections = entry.getValue();
                for (UserConnection connection : connections) {
                    sendLog(connection.toString() + ", host " +
                            connection.getSocketChannel().socket().getInetAddress().getHostAddress());
                }
            }
            return;
        }
        if (result[0].equals("-changelobbyname") && result.length == 3) {
            userServer.changeLobbyNameCommand(result[1], result[2]);
            return;
        }
        if (result[0].equals("-changelobbypassword") && result.length == 3) {
            //TODO: password checks
            String password = result[2];
            if (password.equals("")) {
                sendLog("Illegal password!");
                return;
            }
            userServer.changeLobbyPasswordCommand(result[1], DataManagement.toHashMD5(password));
            return;
        }
        if (result[0].equals("-changeusername") && result.length == 4) {
            userServer.changeUsernameCommand(result[1], result[2], result[3]);
            return;
        }
        if (result[0].equals("-changeuserstatus") && result.length == 4) {
            byte status = 0;
            String str = result[3];
            if (str.equals("readonly")) {
                status = Status.USER_STATUS_READONLY;
            }
            if (str.equals("common")) {
                status = Status.USER_STATUS_COMMON_USER;
            }
            if (str.equals("moderator")) {
                status = Status.USER_STATUS_MODERATOR;
            }
            if (str.equals("route")) {
                status = Status.USER_STATUS_ROUTE;
            }
            if (status == 0) {
                sendLog("Illegal status!");
                return;
            }
            userServer.changeUserStatusCommand(result[1], result[2], status);
            return;
        }
        if (result[0].equals("-kick") && result.length == 3) {
            userServer.kickUserCommand(result[1], result[2]);
            return;
        }
        if (result[0].equals("-ban") && result.length == 3) {
            userServer.banUserCommand(result[1], result[2]);
            return;
        }
        if (result[0].equals("-unban") && result.length == 3) {
            userServer.unbanUserCommand(result[1], result[2]);
            return;
        }
        if (result[0].equals("-enablelogs") && result.length == 1) {
            userServer.setAllLogsEnabled(true);
            sendLog("All logs were enabled!");
            return;
        }
        if (result[0].equals("-disablelogs") && result.length == 1) {
            userServer.setAllLogsEnabled(false);
            sendLog("All logs were disabled!");
            return;
        }
        if (result[0].equals("-enablecommandlogs") && result.length == 1) {
            userServer.setCommandLogsEnabled(true);
            sendLog("Command logs were enabled!");
            return;
        }
        if (result[0].equals("-disablecommandlogs") && result.length == 1) {
            userServer.setCommandLogsEnabled(false);
            sendLog("Command logs were disabled!");
            return;
        }
        if (result[0].equals("-enableequalhosts") && result.length == 1) {
            userServer.setEqualHostsAllowed(true);
            sendLog("Equal hosts in one lobby were enabled!");
            return;
        }
        if (result[0].equals("-disableequalhosts") && result.length == 1) {
            userServer.setEqualHostsAllowed(false);
            sendLog("Equal hosts in one lobby were disabled!");
            return;
        }
        sendLog("Illegal command!");
    }
}
