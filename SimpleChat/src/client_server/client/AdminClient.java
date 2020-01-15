package client_server.client;

import client_server.User;
import client_server.client.util.AdminClientConfiguration;
import client_server.protocol.ChatInfo;
import client_server.protocol.ProtocolConstants;
import client_server.protocol.command_to_client.from_node.from_server.AnswerCommand;
import client_server.protocol.command_to_server_gui.LogCommand;
import client_server.protocol.request_to_node.to_server.LoginAdminRequest;
import client_server.protocol.request_to_node.to_lobby.InternalRequest;
import client_server.server.util.Status;
import gui.ServerGUI;
import util.DataManagement;

import javax.swing.*;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.NotYetConnectedException;

/***
 * Represents admin as a client
 */
public class AdminClient extends Client {

    /** Link to the associated gui */
    private ServerGUI gui;

    /**
     * Constructor (private because of factory pattern)
     * @param gui a link to gui
     */
    private AdminClient(ServerGUI gui) {
        if (gui == null) {
            throw new NullPointerException("AdminClient: gui is null!");
        }
        this.gui = gui;
        commandLogsEnabled = false;
    }

    /***
     * Factory method. Represents logging in
     * @param gui link to gui
     * @param username user name
     * @param password password
     * @return client if successful, null otherwise
     */
    public static AdminClient login(ServerGUI gui, String username, String password) {
        AdminClient adminClient = new AdminClient(gui);
        adminClient.log("Trying to login admin server...");
        if (! adminClient.connect(AdminClientConfiguration.getHost(), AdminClientConfiguration.getPort())) {
            return null;
        }
        try {
            adminClient.sendInfoToServer(new LoginAdminRequest(username, DataManagement.toHashMD5(password)),
                    ProtocolConstants.SERVER_COMMAND_INDEX);
        }
        //TODO: provide normal feedback
        catch (IOException e) {
            adminClient.log("Error while writing to server: " + e.getMessage());
            adminClient.disconnect(null);
            return null;
        }
        try {
            AnswerCommand answer = (AnswerCommand) adminClient.getAnswerFromServer(TIMEOUT);
            byte answerValue = answer.getAnswer();
            if (answerValue == ProtocolConstants.ANSWER_OK) {
                adminClient.user = new User(username, Status.USER_STATUS_ADMINISTRATOR);
                adminClient.log("Logged in to admin server!");
                adminClient.runClient(); // start sending and receiving info
                return adminClient;
            } else if (answerValue == ProtocolConstants.ANSWER_CANCEL) {
                adminClient.log(answer.getMessage());
                adminClient.showMessageToUser(answer.getMessage());
                adminClient.disconnect(null);
                return null;
            }
            else {
                adminClient.log("Unexpected answer from server!");
                adminClient.disconnect(null);
                return null;
            }
        }
        //TODO: provide normal feedback
        catch (ClassNotFoundException e) {
            adminClient.log("Unexpected answer from server!");
        }
        catch (ClassCastException e1) {
            adminClient.log("Unexpected answer from server!");
        }
        catch (ConnectException e2) {
            adminClient.log("No answer from server!");
        }
        catch (NotYetConnectedException e3) {
            adminClient.log("Connection failed!");
        }
        catch (IOException e4) {
            adminClient.log("Error while reading message from server: " + e4.getMessage());
        }
        adminClient.disconnect(null);
        return null;
    }

    @Override
    public synchronized void disconnect(String message) {
        super.disconnect(message);
        if (user != null) {
            gui.disconnectGUIActions(message);
        }
    }

    @Override
    protected boolean canExecute() {
        //TODO: more
        return gui != null;
    }

    @Override
    protected void executeCommand(ChatInfo command) throws ClassCastException, ClassNotFoundException {
        if (canExecute()) {
            ((LogCommand) command).execute(this);
        }
    }

    @Override
    protected void showMessageToUser(String message) {
        if (gui == null) {
            JOptionPane.showMessageDialog(null, message, "Network error", JOptionPane.ERROR_MESSAGE);
        }
        else {
            gui.showMessageToUser(message);
        }
    }

    @Override
    public void log(String message) {
        gui.log(message);
    }

    /***
     * Adds client command to send
     * @param command command to send
     * @return if command has been executed (depends on user status)
     */
    public boolean addCommand(InternalRequest command) {
        addCommandToSend(command, ProtocolConstants.SERVER_COMMAND_INDEX);
        return true;
    }
}
