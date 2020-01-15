package rslib.cs.server.admin;

import rslib.cs.common.*;
import rslib.cs.protocol.ProtocolConstants;
import rslib.cs.protocol.events.AnswerCommand;
import rslib.cs.protocol.requests.to_server.admin.AdminServerRequest;
import rslib.cs.protocol.requests.to_server.admin.LoginAdminRequest;
import rslib.util.DataManagement;
import rslib.util.FileWorking;
import rslib.cs.server.Server;
import rslib.cs.server.user.UserServer;
import rslib.cs.server.util.ServerUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Set;

/***
 * Represents a server that connects admins to server api
 */
public class AdminServer extends Server {

    /** Link to user server */
    protected UserServer userServer;

    /** Server API */
    protected AdminLobby adminLobby;

    /** If server is working */
    protected boolean working;

    /***
     * Constructor
     * @throws IOException if something went wrong
     */
    public AdminServer() throws IOException {
        super(ConnectConfiguration.ADMIN_PORT);
        userServer = new UserServer(this);
        adminLobby = new AdminLobby(this, userServer);
        userServer.setAdminLobby(adminLobby);
        working = false;
    }

    @Override
    public void start() {
        new Thread(this).start();
        new Thread(executor).start();
        foldLog("Admin server launched successfully!");
        userServer.start();
        adminLobby.start();
        working = true;
    }

    @Override
    public void log(String message) {
        foldLog(message);
    }

    @Override
    public void commandLog(String message) {
        foldLog(message);
    }

    @Override
    public void errorLog(String message) {
        foldLog(message);
    }

    /***
     * Logs message if it can not be transferred to administrators
     * @param message message
     */
    public void foldLog(String message) {
        //FileWorking.logToFile("logs.txt", "Admin server: " + message);
    }

    /***
     * Deletes log file
     */
    public static void deleteLogFile() {
        File file = new File("logs.txt");
        if (! file.delete()) {
            System.out.println("Problems deleting log file!");
        }
    }

    @Override
    public synchronized void shutdown() {
        adminLobby.shutdown();
        userServer.shutdown();
        // Closing all unregistered connections
        Set<SelectionKey> keySet = users.keySet();
        for (SelectionKey key : keySet) {
            SocketChannel socketChannel = (SocketChannel)key.channel();
            if (socketChannel.isConnected()) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    foldLog("Error while closing connection (host " +
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
                foldLog("Error while shutting down ServerSocketChannel: " + e.getMessage() + " !");
            }
        }
        foldLog("Admin server has been shut down!");
        working = false;
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
                foldLog("Error while closing connection " + userHost + ": " + e.getMessage());
            }
        }
        key.cancel();
        foldLog("Connection " + userHost + " was closed!");
    }

    @Override
    protected void processReadCommand(CommandInfo command, UserConnection
            connection) throws IllegalStateException,
            ClassCastException, ClassNotFoundException, IOException {
        ByteBuffer buffer = command.getCommand();
        byte commandType = command.getCommandType();
        if (commandType == ProtocolConstants.CONNECT_INDEX) {
            // if it is a command to server, handle it
            AdminServerRequest request = (AdminServerRequest) DataManagement.inflate(buffer);
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
    private void handleRequest(AdminServerRequest request, SelectionKey key, UserConnection connection) {
        switch (request.getIndex()) {
            case LOGIN_R: {
                LoginAdminRequest loginAdminRequest = (LoginAdminRequest)request;
                login(key, connection, loginAdminRequest.getUsername(), loginAdminRequest.getPassword());
                break;
            }
        }
    }

    /***
     * Represents logging in to admin lobby
     * @param key request sender
     * @param connection associated connection
     * @param username user name
     * @param password password
     */
    private void login(SelectionKey key, UserConnection connection, String username, byte[] password) {
        SocketChannel socketChannel = (SocketChannel)key.channel();
        String userHost = connection.getHost();
        foldLog("Logging in to server api from user " + username + " (host " +
                userHost + ")!");
        try {
            if (! adminLobby.checkPassword(password)) { // if data is incorrect
                String message = "Wrong password!";
                ServerUtil.writeMessageToClient(socketChannel, new AnswerCommand(ProtocolConstants.ANSWER_CANCEL,
                        message)); // writing message to user
                foldLog("Logging in failed: " + message);
                return;
            }
            UserConnection newUser = new UserConnection(new User(username, Status.ADMINISTRATOR),
                    socketChannel);
            adminLobby.addUser(newUser); // registering user
            ServerUtil.writeMessageToClient(socketChannel, new AnswerCommand(ProtocolConstants.ANSWER_OK,
                    "")); // writing message to user
            users.remove(key);
            key.cancel(); // unregister user from main selector
            foldLog(newUser.toString() + " has successfully connected!");
        }
        catch (IOException e) { // if error occurred while writing to user
            foldLog("Logging in failed: Error while writing to user: " + e.getMessage() + "!");
            closeConnection(key); // close this connection
        }
    }

    public void setUserServer(UserServer userServer) {
        this.userServer = userServer;
    }

    public boolean isWorking() {
        return working;
    }
}
