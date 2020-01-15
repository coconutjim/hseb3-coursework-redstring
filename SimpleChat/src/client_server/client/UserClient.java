package client_server.client;

import client_server.LobbyInfo;
import client_server.User;
import client_server.client.util.UserClientConfiguration;
import client_server.protocol.ChatInfo;
import client_server.protocol.ProtocolConstants;
import client_server.protocol.command_to_client.ClientCommand;
import client_server.protocol.command_to_client.from_node.from_server.AnswerCommand;
import client_server.protocol.command_to_client.from_node.from_server.SendLobbyListCommand;
import client_server.protocol.request_to_node.to_lobby.*;
import client_server.protocol.request_to_node.to_server.CreateLobbyRequest;
import client_server.protocol.request_to_node.to_server.LoginRequest;
import client_server.protocol.request_to_node.to_server.SendLobbyListRequest;
import client_server.server.util.Status;
import gui.ChatPanel;

import javax.swing.*;
import java.net.ConnectException;
import java.io.*;
import java.nio.channels.NotYetConnectedException;
import java.util.*;

/***
 * Represents user as a client
 */
public class UserClient extends Client {

    /** Link to the associated chat */
    private ChatPanel chat;

    /** Info about connected server */
    private LobbyInfo lobbyInfo;

    /**
     * Constructor (private because of factory pattern)
     */
    private UserClient() {
    }

    /***
     * Factory method. Represent getting lobbies
     * @return lobby list if successful, null otherwise
     */
    public static ArrayList<LobbyInfo> getLobbies() {
        UserClient userClient = new UserClient();
        userClient.log("Trying to get lobby list...");
        if (! userClient.connect(UserClientConfiguration.getHost(), UserClientConfiguration.getPort())) {
            return null;
        }
        try {
            userClient.sendInfoToServer(new SendLobbyListRequest(), ProtocolConstants.SERVER_COMMAND_INDEX);
        }
        //TODO: provide normal feedback
        catch (IOException e) {
            userClient.log("Error while writing to server: " + e.getMessage());
            userClient.disconnect(null);
            return null;
        }
        try {
            ArrayList<LobbyInfo> lobbyInfos =
                    ((SendLobbyListCommand) userClient.getAnswerFromServer(TIMEOUT)).getList();
            userClient.log("Got lobby list successfully!");
            userClient.disconnect(null);
            return lobbyInfos;
        }
        //TODO: provide normal feedback
        catch (ClassNotFoundException e) {
            userClient.log("Unexpected answer from server!");
        }
        catch (ClassCastException e1) {
            userClient.log("Unexpected answer from server!");
        }
        catch (ConnectException e2) {
            userClient.log("No answer from server!");
        }
        catch (NotYetConnectedException e3) {
            userClient.log("Connection failed!");
        }
        catch (IOException e4) {
            userClient.log("Error while reading message from server: " + e4.getMessage());
        }
        userClient.disconnect(null);
        return null;
    }

    /***
     * Factory method. Represents logging in
     * @param username user name
     * @param lobbyInfo info about lobby
     * @return client if successful, null otherwise
     */
    public static UserClient login(String username, LobbyInfo lobbyInfo) {
        UserClient userClient = new UserClient();
        userClient.log("Trying to login to lobby: " + lobbyInfo.getLobbyName() + "...");
        if (! userClient.connect(UserClientConfiguration.getHost(), UserClientConfiguration.getPort())) {
            return null;
        }
        try {
            userClient.sendInfoToServer(new LoginRequest(username, lobbyInfo), ProtocolConstants.SERVER_COMMAND_INDEX);
        }
        //TODO: provide normal feedback
        catch (IOException e) {
            userClient.log("Error while writing to server: " + e.getMessage());
            userClient.disconnect(null);
            return null;
        }
        try {
            AnswerCommand answer = (AnswerCommand) userClient.getAnswerFromServer(TIMEOUT);
            byte answerValue = answer.getAnswer();
            if (answerValue == ProtocolConstants.ANSWER_OK) {
                //TODO: set name to window
                userClient.lobbyInfo = lobbyInfo;
                userClient.user = new User(username, Status.USER_STATUS_COMMON_USER);
                userClient.log("Logged in to lobby: " + lobbyInfo.getLobbyName() + "!");
                userClient.setLobbyInfo(lobbyInfo);
                userClient.runClient();
                return userClient;
            } else if (answerValue == ProtocolConstants.ANSWER_CANCEL) {
                userClient.log(answer.getMessage());
                userClient.showMessageToUser(answer.getMessage());
                userClient.disconnect(null);
                return null;
            }
            else {
                userClient.log("Unexpected answer from server!");
                userClient.disconnect(null);
                return null;
            }
        }
        //TODO: provide normal feedback
        catch (ClassNotFoundException e) {
            userClient.log("Unexpected answer from server!");
        }
        catch (ClassCastException e1) {
            userClient.log("Unexpected answer from server!");
        }
        catch (ConnectException e2) {
            userClient.log("No answer from server!");
        }
        catch (NotYetConnectedException e3) {
            userClient.log("Connection failed!");
        }
        catch (IOException e4) {
            userClient.log("Error while reading message from server: " + e4.getMessage());
        }
        userClient.disconnect(null);
        return null;
    }

    /***
     * Factory method. Represents creating lobby
     * @param username user name
     * @param lobbyInfo info about lobby
     * @return client if successful, null otherwise
     */
    public static UserClient createLobby(String username, LobbyInfo lobbyInfo) {
        UserClient userClient = new UserClient();
        userClient.log("Trying to create lobby: " + lobbyInfo.getLobbyName() + "...");
        if (! userClient.connect(UserClientConfiguration.getHost(), UserClientConfiguration.getPort())) {
            userClient.disconnect(null);
            return null;
        }
        try {
            userClient.sendInfoToServer(new CreateLobbyRequest(username, lobbyInfo), ProtocolConstants.SERVER_COMMAND_INDEX);
        }
        //TODO: provide normal feedback
        catch (IOException e) {
            userClient.log("Error while writing to server: " + e.getMessage());
            userClient.disconnect(null);
            return null;
        }
        try {
            AnswerCommand answer = (AnswerCommand) userClient.getAnswerFromServer(TIMEOUT);
            byte answerValue = answer.getAnswer();
            if (answerValue == ProtocolConstants.ANSWER_OK) {
                String newName = answer.getMessage();
                lobbyInfo.setLobbyName(newName);
                userClient.log("Created lobby: " + newName + "!");
                userClient.lobbyInfo = lobbyInfo;
                userClient.user = new User(username, Status.USER_STATUS_ROUTE);
                userClient.setLobbyInfo(lobbyInfo);
                userClient.runClient();
                return userClient;
            } else if (answerValue == ProtocolConstants.ANSWER_CANCEL) {
                userClient.log(answer.getMessage());
                userClient.showMessageToUser(answer.getMessage());
                userClient.disconnect(null);
                return null;
            }
            else {
                userClient.log("Unexpected answer from server!");
                userClient.disconnect(null);
                return null;
            }
        }
        //TODO: provide normal feedback
        catch (ClassNotFoundException e) {
            userClient.log("Unexpected answer from server!");
        }
        catch (ClassCastException e1) {
            userClient.log("Unexpected answer from server!");
        }
        catch (ConnectException e2) {
            userClient.log("No answer from server!");
        }
        catch (NotYetConnectedException e3) {
            userClient.log("Connection failed!");
        }
        catch (IOException e4) {
            userClient.log("Error while reading message from server: " + e4.getMessage());
        }
        userClient.disconnect(null);
        return null;
    }

    @Override
    public synchronized void disconnect(String message) {
        super.disconnect(message);
        if (lobbyInfo != null && chat != null) {
            chat.disconnect(message);
        }
        lobbyInfo = null;
    }

    @Override
    protected boolean canExecute() {
        //TODO: more
        return chat != null;
    }

    @Override
    protected void executeCommand(ChatInfo command) throws ClassCastException, ClassNotFoundException {
        if (canExecute()) {
            ((ClientCommand) command).execute(this);
        }
    }

    @Override
    protected void showMessageToUser(String message) {
        if (chat == null) {
            JOptionPane.showMessageDialog(null, message, "Network error", JOptionPane.ERROR_MESSAGE);
        }
        else {
            chat.showMessageToUser(message);
        }
    }

    @Override
    protected void log(String message) {
        //TODO: norm logs
        System.out.println((lobbyInfo == null ? "" : lobbyInfo.getLobbyName() + ": ") + message);
    }

    /***
     * Checks user rights while executing command from him
     * @param minimumStatus minimum needed status
     * @return if user have needed rights
     */
    private boolean checkRights(byte minimumStatus) {
        if (user.getStatus() < minimumStatus) {
            String message = "You do not have rights to do it! At least " +
                    Status.STATUS_STRINGS.get(minimumStatus) + " is required!";
            log(message);
            showMessageToUser(message);
            return false;
        }
        return true;
    }

    /***
     * Adds client command to send
     * @param command command to send
     * @param minimumStatus minimum user status to execute command
     * @return if command has been executed (depends on user status)
     */
    public boolean addCommand(ClientCommand command, byte minimumStatus) {
        if (! checkRights(minimumStatus)) {
            return false;
        }
        addCommandToSend(command, ProtocolConstants.CLIENT_COMMAND_INDEX);
        return true;
    }

    /***
     * Gets user list
     */
    public void getUserListRequest() {
        addCommandToSend(new GetUserListRequest(), ProtocolConstants.LOBBY_COMMAND_INDEX);
    }

    /***
     * Gets user list
     */
    public void getBanListRequest() {
        if (! checkRights(Status.USER_STATUS_MODERATOR)) {
            return;
        }
        addCommandToSend(new GetBanListRequest(), ProtocolConstants.LOBBY_COMMAND_INDEX);
    }

    /***
     * Sends request_to_node to lobby asking lobby name changing
     * @param lobbyName new name
     */
    public void changeLobbyNameRequest(String lobbyName) {
        if (! checkRights(Status.USER_STATUS_MODERATOR)) {
            return;
        }
        addCommandToSend(new ChangeLobbyNameRequest(lobbyName), ProtocolConstants.LOBBY_COMMAND_INDEX);
        addCommandToSend(new ChangeLobbyNameRequest(lobbyName), ProtocolConstants.LOBBY_COMMAND_INDEX);
        log("Sent request_to_node to lobby to change lobby name to " + lobbyName + "!");
    }

    /***
     * Sends request_to_node to lobby asking lobby password changing
     * @param password new password
     */
    public void changeLobbyPasswordRequest(String password) {
        if (! checkRights(Status.USER_STATUS_MODERATOR)) {
            return;
        }
        addCommandToSend(new ChangeLobbyPasswordRequest(password), ProtocolConstants.LOBBY_COMMAND_INDEX);
    }

    /***
     * Sends request_to_node to lobby asking username changing
     * @param username new username
     */
    public void changeUsernameRequest(String username) {
        if (! checkRights(Status.USER_STATUS_COMMON_USER)) {
            return;
        }
        addCommandToSend(new ChangeUsernameRequest(username), ProtocolConstants.LOBBY_COMMAND_INDEX);
    }

    /***
     * Sends request_to_node to lobby asking user status changing
     * @param username target user
     * @param status new status
     */
    public void changeUserStatusRequest(String username, byte status) {
        if (! checkRights(Status.USER_STATUS_MODERATOR)) {
            return;
        }
        if (! (status >= Status.USER_STATUS_READONLY && status <= Status.USER_STATUS_MODERATOR)) {
            String message = "You can only change status to \"readonly\", \"common\" and \"moderator\"!";
            log(message);
            showMessageToUser(message);
        }
        addCommandToSend(new ChangeUserStatusRequest(username, status), ProtocolConstants.LOBBY_COMMAND_INDEX);
    }

    /***
     * Sends request_to_node to lobby asking route delegating
     * @param username target user
     */
    public void delegateRouteRequest(String username) {
        if (! checkRights(Status.USER_STATUS_ROUTE)) {
            return;
        }
        addCommandToSend(new DelegateRouteRequest(username), ProtocolConstants.LOBBY_COMMAND_INDEX);
    }

    /***
     * Sends request_to_node to lobby asking user kicking
     * @param username target user
     */
    public void kickRequest(String username) {
        if (! checkRights(Status.USER_STATUS_MODERATOR)) {
            return;
        }
        addCommandToSend(new KickRequest(username), ProtocolConstants.LOBBY_COMMAND_INDEX);
    }

    /***
     * Sends request_to_node to lobby asking user baning
     * @param username target user
     */
    public void banRequest(String username) {
        if (! checkRights(Status.USER_STATUS_MODERATOR)) {
            return;
        }
        addCommandToSend(new BanRequest(username), ProtocolConstants.LOBBY_COMMAND_INDEX);
    }

    /***
     * Sends request_to_node to lobby asking user unbaning
     * @param username target user
     */
    public void unbanRequest(String username) {
        if (! checkRights(Status.USER_STATUS_MODERATOR)) {
            return;
        }
        addCommandToSend(new UnbanRequest(username), ProtocolConstants.LOBBY_COMMAND_INDEX);
    }

    /***
     * Set user list to gui
     * @param users users
     */
    public void setUsers(Map<User, String> users) {
        chat.setUsers(users);
    }

    /***
     * Set ban list to gui
     * @param users ban list
     */
    public void setBanList(Map<String, String> users) {
        chat.setBanList(users);
    }

    /***
     * Changing lobby name
     * @param lobbyName new name
     */
    public void changeLobbyName(String lobbyName) {
        String oldName = lobbyInfo.getLobbyName();
        lobbyInfo.setLobbyName(lobbyName);
        chat.changeLobbyName(oldName, lobbyName);
    }

    /***
     * Changing username
     * @param username new username
     */
    public void changeUsername(String username) {
        user.setUsername(username);
        chat.changeUsername(username);
    }

    /***
     * Changing user status
     * @param status new status
     */
    public void changeUserStatus(byte status) {
        user.setStatus(status);
        chat.changeUserStatus(status);
    }

    /***
     * Kicking
     */
    public void kick() {
        disconnect("You were kicked from the lobby " + lobbyInfo.getLobbyName() + "!");
        chat.kicked();
    }

    /***
     * Banning
     */
    public void ban() {
        disconnect("You were banned in the lobby " + lobbyInfo.getLobbyName() + "!");
        chat.banned();
    }

    /***
     * Rebroadcasts command
     * @param username user name
     * @param message message itself
     */
    public void messageReceived(String username, String message) {
        chat.addMessage(username, message);
    }

    /***
     * Rebroadcasts command
     * @param message message itself
     */
    public void notificationReceived(String message) {
        chat.addNotification(message);
    }

    public void setChat(ChatPanel chat) {
        this.chat = chat;
    }

    public void setLobbyInfo(LobbyInfo lobbyInfo) {
        this.lobbyInfo = lobbyInfo;
    }

    public String getUsername() {
        return user.getUsername();
    }

    public byte getUserStatus() {
        return user.getStatus();
    }

    public String getLobbyName() {
        return lobbyInfo.getLobbyName();
    }
}