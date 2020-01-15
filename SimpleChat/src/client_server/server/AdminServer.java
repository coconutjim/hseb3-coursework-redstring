package client_server.server;

import client_server.User;
import client_server.UserConnection;
import client_server.protocol.ProtocolConstants;
import client_server.protocol.command_to_client.from_node.from_server.AnswerCommand;
import client_server.protocol.request_to_node.to_server.AdminServerRequest;
import client_server.server.util.ServerConfiguration;
import client_server.server.util.ServerUtil;
import client_server.server.util.Status;
import util.DataManagement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Set;

/***
 * Represents a server that connects admins to server api
 */
public class AdminServer extends Server {

    /** Link to user server */
    private UserServer userServer;

    /** Server API */
    private AdminLobby adminLobby;

    /***
     * Constructor
     * @throws IOException if something went wrong
     */
    public AdminServer() throws IOException {
        super(ServerConfiguration.getAdminPort());
        userServer = new UserServer();
        adminLobby = new AdminLobby(this, userServer);
        userServer.setAdminLobby(adminLobby);
        start();
    }

    @Override
    public void start() {
        new Thread(this).start();
        foldLog("Server API adapter launched successfully!");
        userServer.start();
        adminLobby.start();
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
       System.out.println("Server API Adapter: " + message);
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
        foldLog("Server API adapter has been shut down!");
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
    protected String processReadCommand(ByteBuffer buffer, SelectionKey key, UserConnection
            connection) throws IllegalStateException,
            ClassCastException, ClassNotFoundException, IOException {
        if (connection.getReadCommandType() == ProtocolConstants.SERVER_COMMAND_INDEX) {
            // if it is a command to server, handle it
            AdminServerRequest request = (AdminServerRequest) DataManagement.inflate(buffer);
            request.handleRequest(this, key, connection);
            return request.getClass().getSimpleName();
        } else {
            // not expected here
            throw new IllegalStateException();
        }
    }

    /***
     * Tries to login to the admin server
     * @param key command sender
     * @param connection associated connection
     * @param username user who sent request
     * @param password password
     */
    public void login(SelectionKey key, UserConnection connection, String username, byte[] password) {
        SocketChannel socketChannel = connection.getSocketChannel();
        String userHost = socketChannel.socket().getInetAddress().getHostAddress();
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
            users.remove(key);
            key.cancel(); // unregister user from main selector
            UserConnection newUser = new UserConnection(new User(username, Status.USER_STATUS_ADMINISTRATOR),
                    socketChannel);
            adminLobby.addUser(newUser); // registering user
            foldLog(newUser.toString() + " has successfully connected!");
            ServerUtil.writeMessageToClient(socketChannel, new AnswerCommand(ProtocolConstants.ANSWER_OK,
                    "")); // writing message to user
        }
        catch (IOException e) { // if error occurred while writing to user
            closeConnection(key); // close this connection
            foldLog("Logging in failed: Error while writing to user: " + e.getMessage() + "!");
        }
    }

    public void setUserServer(UserServer userServer) {
        this.userServer = userServer;
    }

    /***
     * Reads config file and gets essential info
     * @throws IllegalArgumentException if data is not correct
     */
    private static void readConfig() throws IllegalArgumentException {

        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader("server.properties"));
            boolean readAdminPort = false;
            boolean readUserPort = false;
            boolean readPassword = false;
            while ((line = br.readLine()) != null && ! line.equals("")) {
                String[] result = line.split(" ");
                if (result[0].equals("UserServerPort:") && result.length == 2) {
                    ServerConfiguration.setUserPort(Integer.parseInt(result[1]));
                    readUserPort = true;
                }
                if (result[0].equals("AdminServerPort:") && result.length == 2) {
                    ServerConfiguration.setAdminPort(Integer.parseInt(result[1]));
                    readAdminPort = true;
                }
                if (result[0].equals("AdminPassword:") && result.length == 2) {
                    ServerConfiguration.setAdminPassword(result[1]);
                    readPassword = true;
                }
            }
            if (! (readPassword && readAdminPort && readUserPort)) {
                throw new IllegalArgumentException("Lack of data!");
            }
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Problems reading the file!");
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

    public static void main(String[] args) {
        try {
            readConfig();
        }
        catch (IllegalArgumentException e) {
            System.out.println("Fatal error: Could not " +
                    "read configuration file: " + e.getMessage());
            return;
        }
        try {
            new AdminServer();
        }
        catch (IOException e) {
            System.out.println("Unable to start server: " + e.getMessage());
        }
    }
}
