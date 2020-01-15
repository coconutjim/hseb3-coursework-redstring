package rslib.cs.client.admin;

import rslib.cs.client.Client;
import rslib.cs.common.*;
import rslib.cs.protocol.ProtocolConstants;
import rslib.cs.protocol.events.AnswerCommand;
import rslib.cs.protocol.events.admin.AdminEvent;
import rslib.cs.protocol.requests.to_lobby.admin.InternalRequest;
import rslib.cs.protocol.requests.to_server.admin.LoginAdminRequest;
import rslib.listeners.AdminListener;
import rslib.listeners.LogListener;
import rslib.listeners.MessageListener;
import rslib.util.DataManagement;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * Represents admin as a client
 */
public class AdminClient extends Client {

    /** Chat listeners */
    private List<AdminListener> adminListeners;

    /**
     * Constructor (protected because of factory pattern)
     * @param host server host
     */
    protected AdminClient(String host) {
        super(host, ConnectConfiguration.ADMIN_PORT);
        adminListeners = new CopyOnWriteArrayList<>(); // concurrency
    }

    /***
     * Adds listener to observer
     * @param listener listener
     */
    public void addAdminListener(AdminListener listener) {
        adminListeners.add(listener);
    }

    /***
     * Removes listener from observer
     * @param listener listener
     */
    public void removeAdminListener(AdminListener listener) {
        adminListeners.remove(listener);
    }

    /***
     * Factory method. Represents logging in
     * @param host specified host
     * @param logListener log listener
     * @param messageListener messageListener
     * @param username user name
     * @param password password
     * @return client if successful, null otherwise
     */
    public static AdminClient login(String host, String username, String password,
                                    MessageListener messageListener, LogListener logListener) {
        AdminClient adminClient = new AdminClient(host);
        adminClient.addLogListener(logListener);
        adminClient.addMessageListener(messageListener);
        adminClient.log("Trying to login admin server...");
        SocketChannel socketChannel = adminClient.connect(adminClient.host, adminClient.port);
        if (socketChannel == null) {
            return null;
        }
        try {
            adminClient.sendInfoToServer(new LoginAdminRequest(username,
                    DataManagement.digest(password)),
                    socketChannel);
        }
        catch (IOException e) {
            adminClient.log("Error while writing to server: " + e.getMessage());
            adminClient.disconnect(socketChannel, "Error while connecting to server!");
            return null;
        }
        try {
            AnswerCommand answer = (AnswerCommand) adminClient.getAnswerFromServer(socketChannel, TIMEOUT);
            byte answerValue = answer.getAnswer();
            if (answerValue == ProtocolConstants.ANSWER_OK) {
                adminClient.log("Logged in to admin server!");
                adminClient.start(new UserConnection(new User(username, Status.ADMINISTRATOR),
                        socketChannel), socketChannel); // start sending and receiving info
                return adminClient;
            } else if (answerValue == ProtocolConstants.ANSWER_CANCEL) {
                adminClient.disconnect(socketChannel, answer.getMessage());
                return null;
            }
            else {
                adminClient.log("Unexpected answer from server!");
                adminClient.disconnect(socketChannel, "Error while connecting to server!");
                return null;
            }
        }
        //TODO: provide normal feedback
        catch (NotYetConnectedException e2) {
            adminClient.log("Connection failed!");
        }
        catch (IllegalStateException | ClassNotFoundException | ClassCastException e) {
            adminClient.log("Unexpected answer from server: " + e.getMessage() + "!");
        }
        catch (ConnectException e1) {
            adminClient.log("No answer from server!");
        }
        catch (IOException e3) {
            adminClient.log("Error while reading message from server: " + e3.getMessage());
        }
        adminClient.disconnect(socketChannel, "Error while connecting to server!");
        return null;
    }

    /***
     * Factory method. Represents logging in
     * @param logListener log listener
     * @param messageListener messageListener
     * @param username user name
     * @param password password
     * @return client if successful, null otherwise
     */
    public static AdminClient login(String username, String password,
                                    MessageListener messageListener, LogListener logListener) {
        return login(DEFAULT_HOST, username, password, messageListener, logListener);
    }

    @Override
    public synchronized void disconnect(String message) {
        super.disconnect(message);
        adminListeners.clear();
    }

    @Override
    protected void processReadCommand(CommandInfo command) throws
            IllegalStateException, IOException, ClassCastException, ClassNotFoundException {
        ByteBuffer buffer = command.getCommand();
        byte commandType = command.getCommandType();
        if (commandType != ProtocolConstants.ADMIN_INDEX) {
            throw new IllegalStateException();
        }
        AdminEvent ae = (AdminEvent) DataManagement.inflate(buffer);
        log("Received " + ae.getClass().getSimpleName() + "!");
        for (AdminListener listener : adminListeners) {
            listener.hear(ae);
        }
    }

    /***
     * Adds client command to send
     * @param command command to send
     * @return if command has been executed (depends on user status)
     */
    public boolean addAdminCommand(InternalRequest command) {
        addCommandToSend(command, ProtocolConstants.ADMIN_INDEX);
        return true;
    }
}
