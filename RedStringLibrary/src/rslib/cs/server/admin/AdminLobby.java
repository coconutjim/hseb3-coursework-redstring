package rslib.cs.server.admin;

import rslib.cs.common.DataChecking;
import rslib.cs.common.CommandInfo;
import rslib.cs.common.Status;
import rslib.cs.common.UserConnection;
import rslib.cs.common.ConnectConfiguration;
import rslib.cs.protocol.ProtocolConstants;
import rslib.cs.protocol.RedStringInfo;
import rslib.cs.protocol.events.admin.LogFileEvent;
import rslib.cs.protocol.events.admin.LogSizeEvent;
import rslib.cs.protocol.events.admin.ServerLogEvent;
import rslib.cs.protocol.requests.to_lobby.admin.InternalRequest;
import rslib.util.DataManagement;
import rslib.cs.server.util.CommandReceiver;
import rslib.cs.server.Lobby;
import rslib.cs.server.user.UserServer;
import rslib.cs.server.util.LobbySession;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    /** List of commands */
    public static final String COMMANDS = "List of available commands:\n" +
            "-enablelogs enables all logs\n" +
            "-disablelogs disables all logs\n" +
            "-start starts user server\n" +
            "-shutdown shutdowns user server\n" +
            "-lobbies list server lobbies with info about users and boards\n" +
            "-shutdownlobby <lobby name> shutdowns lobby\n" +
            "-changelobbyname <lobby name> <new name> changes lobby name\n" +
            "-changelobbypassword <lobby name> <new password> changes lobby password\n" +
            "-deletelobbypassword <lobby name> deletes lobby password\n" +
            "-changeusername <lobby name> <username> <new name> changes username\n" +
            "-changeuserstatus <lobby name> <new status> changes user status (status must be one of: " +
            "readonly, common, moderator, root)\n" +
            "-kick <lobby name> <username> kicks user\n" +
            "-ban <lobby name> <username> bans user\n" +
            "-unban <lobby name> <username> unbans user";

    /***
     * Constructor
     * @param adminServer link to API adapter
     * @param userServer link to server
     * @throws IOException if something went wrong
     */
    public AdminLobby(AdminServer adminServer, UserServer userServer) throws IOException {
        if (adminServer == null) {
            throw new IllegalArgumentException("ServerAPI: adminServer is null!");
        }
        if (userServer == null) {
            throw new IllegalArgumentException("ServerAPI: userServer is null!");
        }
        selector = Selector.open();
        this.adminServer = adminServer;
        this.userServer = userServer;
        newUsers = new CopyOnWriteArrayList<>(); // concurrency
    }

    @Override
    public void start() {
        new Thread(this).start();
        new Thread(executor).start();
        foldLog("Admin lobby launched successfully!");
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
     * @param needFold if it is needed to fold log
     */
    public void sendLog(String message, boolean needFold) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        addAdminLobbyCommand(new ServerLogEvent(dateFormat.format(date) + ": " + message),
                new CommandReceiver(userList, Status.ADMINISTRATOR));
        if (needFold) {
            foldLog(message);
        }
    }

    /***
     * Folds log message
     * @param message log message
     */
    public void foldLog(String message) {
        adminServer.foldLog("Admin lobby : " + message);
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
        userList.remove(userConnection);
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
        sendLog(message, true);
    }

    @Override
    protected synchronized void registerNewUsers() throws IOException {
        //TODO: reduce closing repetition
        if (newUsers.isEmpty()) {
            return;
        }
        for (UserConnection userConnection : newUsers) {
            userConnection.setId(generateId());
            SelectionKey key = userConnection.registerOnSelector(selector);
            // checking name collisions
            String oldName = userConnection.getUsername();
            String newName = checkUsername(oldName);
            if (! oldName.equals(newName)) {
                String message1 = "Due to collisions avoiding " + userConnection.toString() + " name " +
                        "was changed to " + newName;
                userConnection.setUsername(newName);
                sendLog(message1, true);
            }
            String message = userConnection.toString() + " connected!";
            sendLog(message, true);
            users.put(key, userConnection);
            userList.add(userConnection);
        }
        newUsers.clear();
    }

    @Override
    protected void processReadCommand(CommandInfo command, UserConnection
            connection) throws IllegalStateException, ClassCastException, ClassNotFoundException, IOException {
        if (command.getCommandType() != ProtocolConstants.ADMIN_INDEX) {
            throw new IllegalStateException();
        }
        ByteBuffer buffer = command.getCommand();
        String request = ((InternalRequest) DataManagement.inflate(buffer)).getCommand();
        sendLog("Received internal command \"" + request + "\" from " +
                connection.toString() + "!", true);
        processCommand(request);
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
        return s.equals(ConnectConfiguration.ADMIN_PASSWORD);
    }


    /***
     * Processes internal command
     * @param command command
     */
    private void processCommand(String command) {
        String[] result = command.split(" ");
        if (result[0].equals("-start") && result.length == 1) {
            if (userServer != null) {
                sendLog("Server is already working!", true);
                return;
            }
            try {
                userServer = new UserServer(adminServer);
                userServer.setAdminLobby(this);
                adminServer.setUserServer(userServer);
                userServer.start();
            }
            catch (IOException e) {
                sendLog("Error while launching user server: " + e.getMessage(), true);
            }
            return;
        }
        if (result[0].equals("-shutdown") && result.length == 1) {
            if (userServer == null) {
                sendLog("No server is running!", true);
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
            ArrayList<LobbySession> lobbies = userServer.getLobbies();
            if (lobbies.isEmpty()) {
                sendLog("No active lobbies!", true);
                return;
            }
            String info = "";
            for (LobbySession lobbySession : lobbies) {
                info += lobbySession.toString() + "\n";
            }
            sendLog(info, true);
            return;
        }
        if (result[0].equals("-changelobbyname") && result.length == 3) {
            userServer.changeLobbyNameCommand(result[1], result[2]);
            return;
        }
        if (result[0].equals("-changelobbypassword") && result.length == 3) {
            String password = result[2];
            if (! DataChecking.isLobbyPasswordValid(password)) {
                sendLog("Illegal password!", true);
                return;
            }
            userServer.changeLobbyPasswordCommand(result[1], DataManagement.digest(password));
            return;
        }
        if (result[0].equals("-deletelobbypassword") && result.length == 2) {
            userServer.changeLobbyPasswordCommand(result[1], DataManagement.digest(null));
            return;
        }
        if (result[0].equals("-changeusername") && result.length == 4) {
            userServer.changeUsernameCommand(result[1], result[2], result[3]);
            return;
        }
        if (result[0].equals("-changeuserstatus") && result.length == 4) {
            Status status = null;
            String str = result[3];
            if (str.equals("readonly")) {
                status = Status.READONLY;
            }
            if (str.equals("common")) {
                status = Status.COMMON;
            }
            if (str.equals("moderator")) {
                status = Status.MODERATOR;
            }
            if (str.equals("root")) {
                status = Status.LOBBY_ROOT;
            }
            if (status == null) {
                sendLog("Illegal status!", true);
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
            sendLog("All logs were enabled!", true);
            return;
        }
        if (result[0].equals("-disablelogs") && result.length == 1) {
            userServer.setAllLogsEnabled(false);
            sendLog("All logs were disabled!", true);
            return;
        }
        if (result[0].equals("-enablecommandlogs") && result.length == 1) {
            userServer.setCommandLogsEnabled(true);
            sendLog("Command logs were enabled!", true);
            return;
        }
        if (result[0].equals("-disablecommandlogs") && result.length == 1) {
            userServer.setCommandLogsEnabled(false);
            sendLog("Command logs were disabled!", true);
            return;
        }
        if (result[0].equals("-enableequalhosts") && result.length == 1) {
            userServer.setEqualHostsAllowed(true);
            sendLog("Equal hosts in one lobby were enabled!", true);
            return;
        }
        if (result[0].equals("-disableequalhosts") && result.length == 1) {
            userServer.setEqualHostsAllowed(false);
            sendLog("Equal hosts in one lobby were disabled!", true);
            return;
        }
        if (result[0].equals("-clearlogfile") && result.length == 1) {
            AdminServer.deleteLogFile();
            sendLog("Log file was cleared!", true);
            return;
        }
        if (result[0].equals("-getlogfile") && result.length == 1) {
            try {
                sendLogFile("logs.txt");
                sendLog("Log file was sent!", true);
            }
            catch (IOException e) {
                sendLog("Problems extracting log file: " + e.getMessage(), true);
            }
            return;
        }
        if (result[0].equals("-getlogsize") && result.length == 1) {
            File file = new File("logs.txt");
            addAdminLobbyCommand(new LogSizeEvent(file.length()),
                    new CommandReceiver(userList, Status.ADMINISTRATOR));
            return;
        }
        sendLog("Illegal command!", true);
    }

    /***
     * Sends log file by parts
     * @param filename file name
     * @throws IOException if something went wrong
     */
    private void sendLogFile(String filename) throws IOException {
        if (filename == null) {
            throw new IllegalArgumentException("LogFileEvent: filename is null!");
        }
        ArrayList<String> content = new ArrayList<>();
        BufferedReader br = null;
        try {
            String line;
            int lines = 0;
            br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                content.add(line);
                if (++ lines == 100) {
                    addAdminLobbyCommand(new LogFileEvent(content), new CommandReceiver(userList,
                            Status.ADMINISTRATOR));
                    content.clear();
                    break;
                }
            }
        }
        finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e1) {
                // ????????
            }
        }
    }

    /***
     * Adds admin command to write
     * @param info command
     * @param receiver command receiver
     */
    private void addAdminLobbyCommand(RedStringInfo info, CommandReceiver receiver) {
        addLobbyCommand(info, ProtocolConstants.ADMIN_INDEX, receiver);
    }
}
