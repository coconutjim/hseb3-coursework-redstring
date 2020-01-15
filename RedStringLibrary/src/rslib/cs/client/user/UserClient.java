package rslib.cs.client.user;


import rslib.cs.client.Client;
import rslib.cs.common.*;
import rslib.cs.protocol.ProtocolConstants;
import rslib.cs.protocol.events.AnswerCommand;
import rslib.cs.protocol.events.ClientEvent;
import rslib.cs.protocol.events.SendLobbyListCommand;
import rslib.cs.protocol.events.bmessage.BoardMessageEvent;
import rslib.cs.protocol.events.board.BoardEvent;
import rslib.cs.protocol.events.chat.ChatEvent;
import rslib.cs.protocol.events.main_client.*;
import rslib.cs.protocol.events.message.ShowMessageEvent;
import rslib.cs.protocol.events.setup.DeleteBoardEvent;
import rslib.cs.protocol.events.setup.SetupEvent;
import rslib.cs.protocol.requests.to_lobby.user.*;
import rslib.cs.protocol.requests.to_server.user.CreateLobbyRequest;
import rslib.cs.protocol.requests.to_server.user.LoginRequest;
import rslib.cs.protocol.requests.to_server.user.SendLobbyListRequest;
import rslib.listeners.*;
import rslib.util.DataManagement;

import java.net.ConnectException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * Represents user as a client
 */
public class UserClient extends Client {

    /** Info about connected server */
    private LobbyInfo lobbyInfo;

    /** Connected users */
    private Map<User, String> users;

    /** Banned users */
    private Map<String, String> banned;

    /** Main client listeners */
    private List<MainClientListener> mainClientListeners;

    /** Chat listeners */
    private List<ChatListener> chatListeners;

    /** Board listeners */
    private List<BoardListener> boardListeners;

    /** Setup listeners */
    private List<SetupListener> setupListeners;

    /** Board message listeners */
    private List<BoardMessageListener> boardMessageListeners;

    /**
     * Constructor (protected because of factory pattern)
     * @param host server host
     */
    protected UserClient(String host) {
        super(host, ConnectConfiguration.USER_PORT);
        //TODO: mb not concurrency? and generic listeners
        mainClientListeners = new CopyOnWriteArrayList<>(); // concurrency
        chatListeners = new CopyOnWriteArrayList<>(); // concurrency
        boardListeners = new CopyOnWriteArrayList<>(); // concurrency
        setupListeners = new CopyOnWriteArrayList<>(); // concurrency
        boardMessageListeners = new CopyOnWriteArrayList<>(); // concurrency
        users = new HashMap<>();
        banned = new HashMap<>();
    }

    /***
     * Adds listener to observer
     * @param listener listener
     */
    public void addMainClientListener(MainClientListener listener) {
        if (listener != null) {
            mainClientListeners.add(listener);
        }
    }

    /***
     * Adds listener to observer
     * @param listener listener
     */
    public void addChatListener(ChatListener listener) {
        if (listener != null) {
            chatListeners.add(listener);
        }
    }

    /***
     * Adds listener to observer
     * @param listener listener
     */
    public void addBoardListener(BoardListener listener) {
        if (listener != null) {
            boardListeners.add(listener);
        }
    }

    /***
     * Adds listener to observer
     * @param listener listener
     */
    public void addSetupListener(SetupListener listener) {
        setupListeners.add(listener);
    }

    /***
     * Adds listener to observer
     * @param listener listener
     */
    public void addBoardMessageListener(BoardMessageListener listener) {
        boardMessageListeners.add(listener);
    }

    /***
     * Removes listener from observer
     * @param listener listener
     */
    public void removeMainClientListener(MainClientListener listener) {
        mainClientListeners.remove(listener);
    }

    /***
     * Removes listener from observer
     * @param listener listener
     */
    public void removeChatListener(ChatListener listener) {
        chatListeners.remove(listener);
    }

    /***
     * Removes listener from observer
     * @param listener listener
     */
    public void removeBoardListener(BoardListener listener) {
        boardListeners.remove(listener);
    }

    /***
     * Removes listener from observer
     * @param listener listener
     */
    public void removeSetupListener(SetupListener listener) {
        setupListeners.remove(listener);
    }

    /***
     * Removes listener from observer
     * @param listener listener
     */
    public void removeBoardMessageListener(BoardMessageListener listener) {
        boardMessageListeners.remove(listener);
    }

    /***
     * Factory method. Represent getting lobbies
     * @param host specified host
     * @param logListener log listener
     * @param messageListener message listener
     * @return lobby list if successful, null otherwise
     */
    public static ArrayList<LobbyInfo> getLobbies(String host, MessageListener messageListener, LogListener logListener) {
        UserClient userClient = new UserClient(host);
        userClient.addLogListener(logListener);
        userClient.addMessageListener(messageListener);
        userClient.log("Trying to get lobby list...");
        SocketChannel socketChannel = userClient.connect(userClient.host, userClient.port);
        if (socketChannel == null) {
            return null;
        }
        try {
            userClient.sendInfoToServer(new SendLobbyListRequest(), socketChannel);
        }
        //TODO: provide normal feedback
        catch (IOException e) {
            userClient.log("Error while writing to server: " + e.getMessage());
            userClient.disconnect(socketChannel, "Error while connecting to server!");
            return null;
        }
        try {
            ArrayList<LobbyInfo> lobbyInfos =
                    ((SendLobbyListCommand) userClient.getAnswerFromServer(socketChannel, TIMEOUT)).getList();
            userClient.log("Got lobby list successfully!");
            userClient.disconnect(socketChannel, "");
            return lobbyInfos;
        }
        //TODO: provide normal feedback
        catch (NotYetConnectedException e2) {
            userClient.log("Connection failed!");
        }
        catch (ConnectException e1) {
            userClient.log("No answer from server!");
        }
        catch (IllegalStateException | ClassNotFoundException | ClassCastException e) {
            userClient.log("Unexpected answer from server: " + e.getMessage() + "!");
        }
        catch (IOException e3) {
            userClient.log("Error while reading message from server: " + e3.getMessage());
        }
        userClient.disconnect(socketChannel, "Error while connecting to server!");
        return null;
    }

    /***
     * Factory method. Represent getting lobbies
     * @param logListener log listener
     * @param messageListener message listener
     * @return lobby list if successful, null otherwise
     */
    public static ArrayList<LobbyInfo> getLobbies(MessageListener messageListener, LogListener logListener) {
        return getLobbies(DEFAULT_HOST, messageListener, logListener);
    }

    /***
     * Factory method. Represents logging in
     * @param host specified host
     * @param logListener log listener
     * @param messageListener message listener
     * @param username user name
     * @param lobbyInfo info about lobby
     * @return client if successful, null otherwise
     */
    public static UserClient login(String host, String username, LobbyInfo lobbyInfo,
                                   MessageListener messageListener, LogListener logListener) {
        UserClient userClient = new UserClient(host);
        userClient.addLogListener(logListener);
        userClient.addMessageListener(messageListener);
        userClient.log("Trying to login to lobby: " + lobbyInfo.getLobbyName() + "...");
        SocketChannel socketChannel = userClient.connect(userClient.host, userClient.port);
        if (socketChannel == null) {
            return null;
        }
        try {
            userClient.sendInfoToServer(new LoginRequest(username, lobbyInfo), socketChannel);
        }
        //TODO: provide normal feedback
        catch (IOException e) {
            userClient.log("Error while writing to server: " + e.getMessage());
            userClient.disconnect(socketChannel, "Error while connecting to server!");
            return null;
        }
        try {
            AnswerCommand answer = (AnswerCommand) userClient.getAnswerFromServer(socketChannel, TIMEOUT);
            byte answerValue = answer.getAnswer();
            if (answerValue == ProtocolConstants.ANSWER_OK) {
                userClient.lobbyInfo = lobbyInfo;
                userClient.log("Logged in to lobby: " + lobbyInfo.getLobbyName() + "!");
                userClient.start(new UserConnection(new User(username, Status.COMMON),
                        socketChannel), socketChannel);
                return userClient;
            } else if (answerValue == ProtocolConstants.ANSWER_CANCEL) {
                userClient.disconnect(socketChannel, answer.getMessage());
                return null;
            }
            else {
                userClient.log("Unexpected answer from server!");
                userClient.disconnect(socketChannel, "Error while connecting to server!");
                return null;
            }
        }
        //TODO: provide normal feedback
        catch (NotYetConnectedException e2) {
            userClient.log("Connection failed!");
        }
        catch (IllegalStateException | ClassNotFoundException | ClassCastException e) {
            userClient.log("Unexpected answer from server: " + e.getMessage() + "!");
        }
        catch (ConnectException e1) {
            userClient.log("No answer from server!");
        }
        catch (IOException e3) {
            userClient.log("Error while reading message from server: " + e3.getMessage());
        }
        userClient.disconnect(socketChannel, "Error while connecting to server!");
        return null;
    }

    /***
     * Factory method. Represents logging in
     * @param logListener log listener
     * @param messageListener message listener
     * @param username user name
     * @param lobbyInfo info about lobby
     * @return client if successful, null otherwise
     */
    public static UserClient login(String username, LobbyInfo lobbyInfo,
                                   MessageListener messageListener, LogListener logListener) {
        return login(DEFAULT_HOST, username, lobbyInfo,
                messageListener, logListener);
    }

    /***
     * Factory method. Represents creating lobby
     * @param host specified host
     * @param logListener log listener
     * @param messageListener message listener
     * @param username user name
     * @param lobbyInfo info about lobby
     * @return client if successful, null otherwise
     */
    public static UserClient createLobby(String host, String username, LobbyInfo lobbyInfo,
                                         MessageListener messageListener, LogListener logListener) {
        UserClient userClient = new UserClient(host);
        userClient.addLogListener(logListener);
        userClient.addMessageListener(messageListener);
        userClient.log("Trying to create lobby: " + lobbyInfo.getLobbyName() + "...");
        SocketChannel socketChannel = userClient.connect(userClient.host, userClient.port);
        if (socketChannel == null) {
            return null;
        }
        try {
            userClient.sendInfoToServer(new CreateLobbyRequest(username, lobbyInfo),
                    socketChannel);
        }
        //TODO: provide normal feedback
        catch (IOException e) {
            userClient.log("Error while writing to server: " + e.getMessage());
            userClient.disconnect(socketChannel, "Error while connecting to server!");
            return null;
        }
        try {
            AnswerCommand answer = (AnswerCommand) userClient.getAnswerFromServer(socketChannel, TIMEOUT);
            byte answerValue = answer.getAnswer();
            if (answerValue == ProtocolConstants.ANSWER_OK) {
                String newName = answer.getMessage();
                lobbyInfo.setLobbyName(newName);
                userClient.log("Created lobby: " + newName + "!");
                userClient.lobbyInfo = lobbyInfo;
                userClient.start(new UserConnection(new User(username, Status.LOBBY_ROOT),
                        socketChannel), socketChannel);
                return userClient;
            } else if (answerValue == ProtocolConstants.ANSWER_CANCEL) {
                userClient.disconnect(socketChannel, answer.getMessage());
                return null;
            }
            else {
                userClient.log("Unexpected answer from server!");
                userClient.disconnect(socketChannel, "Error while connecting to server!");
                return null;
            }
        }
        //TODO: provide normal feedback
        catch (NotYetConnectedException e3) {
            userClient.log("Connection failed!");
        }
        catch (IllegalStateException | ClassNotFoundException | ClassCastException e) {
            userClient.log("Unexpected answer from server: " + e.getMessage() + "!");
        }
        catch (ConnectException e2) {
            userClient.log("No answer from server!");
        }
        catch (IOException e4) {
            userClient.log("Error while reading message from server: " + e4.getMessage());
        }
        userClient.disconnect(socketChannel, "Error while connecting to server!");
        return null;
    }

    /***
     * Factory method. Represents creating lobby
     * @param logListener log listener
     * @param messageListener message listener
     * @param username user name
     * @param lobbyInfo info about lobby
     * @return client if successful, null otherwise
     */
    public static UserClient createLobby(String username, LobbyInfo lobbyInfo,
                                         MessageListener messageListener, LogListener logListener) {
        return createLobby(DEFAULT_HOST, username, lobbyInfo, messageListener, logListener);
    }

    @Override
    public synchronized void disconnect(String message) {
        super.disconnect(message);
        lobbyInfo = null;
        mainClientListeners.clear();
        chatListeners.clear();
        boardListeners.clear();
        setupListeners.clear();
        boardMessageListeners.clear();
    }

    @Override
    protected void processReadCommand(CommandInfo command) throws
            IllegalStateException, IOException, ClassCastException, ClassNotFoundException {
        ByteBuffer buffer = command.getCommand();
        byte commandType = command.getCommandType();
        ClientEvent clientEvent = (ClientEvent) DataManagement.inflate(buffer);
        log("Received " + clientEvent.getClass().getSimpleName() + "!");
        switch (commandType) {
            case ProtocolConstants.CLIENT_INDEX: {
                MainClientEvent mce = (MainClientEvent) clientEvent;
                processMainClientEvent(mce);
                for (MainClientListener listener : mainClientListeners) {
                    listener.hear(mce);
                }
                return;
            }
            case ProtocolConstants.CHAT_INDEX: {
                ChatEvent ce = (ChatEvent) clientEvent;
                for (ChatListener listener : chatListeners) {
                    listener.hear(ce);
                }
                return;
            }
            case ProtocolConstants.MESSAGE_INDEX: {
                ShowMessageEvent sme = (ShowMessageEvent) clientEvent;
                for (MessageListener listener : messageListeners) {
                    listener.hear(sme);
                }
                return;
            }
            case ProtocolConstants.BOARD_INDEX: {
                BoardEvent be = (BoardEvent) clientEvent;
                for (BoardListener listener : boardListeners) {
                    listener.hear(be);
                }
                return;
            }
            case ProtocolConstants.SETUP_INDEX: {
                SetupEvent se = (SetupEvent) clientEvent;
                for (SetupListener listener : setupListeners) {
                    listener.hear(se);
                }
                return;
            }
            case ProtocolConstants.BOARD_MESSAGE_INDEX: {
                BoardMessageEvent bme = (BoardMessageEvent) clientEvent;
                for (BoardMessageListener listener : boardMessageListeners) {
                    listener.hearMessage(bme.getMessage());
                }
                return;
            }
        }
        throw new IllegalStateException();
    }


    @Override
    protected void log(String message) {
        super.log((lobbyInfo == null ? "" : lobbyInfo.getLobbyName() + ": ") + message);
    }

    /***
     * Checks user rights while executing command from him
     * @param minimumStatus minimum required status
     * @return if user have required rights
     */
    public boolean checkRights(Status minimumStatus) {
        if (connection.getStatus().ordinal() < minimumStatus.ordinal()) {
            String message = "You do not have rights to do it! At least " +
                    minimumStatus.toString() + " is required!";
            log(message);
            showMessage(message, ShowMessageEvent.MessageType.ERROR);
            return false;
        }
        return true;
    }

    /***
     * Checks user rights while executing command from him
     * @return if user have required rights
     */
    public boolean checkRights() {
        return checkRights(Status.COMMON);
    }

    /***
     * Checks board rights (owner + status)
     * @param minimumStatus minimum required status
     * @return if user have required rights
     */
    public boolean checkBoardRights(Status minimumStatus, String owner) {
        return owner.equals(getUsername()) || minimumStatus.ordinal() <= connection.getStatus().ordinal();
    }

    //TODO: see
    /***
     * Adds client command to send
     * @param command command to send
     */
    public void addChatEvent(ClientEvent command) {
        addCommandToSend(command, ProtocolConstants.CHAT_INDEX);
    }

    /***
     * Adds setup command to send
     * @param command command to send
     */
    public void addSetupEvent(SetupEvent command) {
        addCommandToSend(command, ProtocolConstants.SETUP_INDEX);
    }

    /***
     * Adds board command to send
     * @param command command to send
     */
    public void addBoardEvent(BoardEvent command) {
        addCommandToSend(command, ProtocolConstants.BOARD_INDEX);
    }

    /***
     * Gets user list
     */
    public void getUserListRequest() {
        addCommandToSend(new GetUserListRequest(), ProtocolConstants.CLIENT_INDEX);
    }

    /***
     * Gets ban list
     */
    public void getBanListRequest() {
        if (! checkRights(Status.MODERATOR)) {
            return;
        }
        addCommandToSend(new GetBanListRequest(), ProtocolConstants.CLIENT_INDEX);
    }

    /***
     * Sends request to lobby asking lobby name changing
     * @param lobbyName new name
     */
    public void changeLobbyNameRequest(String lobbyName) {
        if (! checkRights(Status.MODERATOR)) {
            return;
        }
        addCommandToSend(new ChangeLobbyNameRequest(lobbyName), ProtocolConstants.CLIENT_INDEX);
    }

    /***
     * Sends request to lobby asking lobby password changing
     * @param password new password
     */
    public void changeLobbyPasswordRequest(String password) {
        if (! checkRights(Status.MODERATOR)) {
            return;
        }
        addCommandToSend(new ChangeLobbyPasswordRequest(password), ProtocolConstants.CLIENT_INDEX);
    }

    /***
     * Sends request to lobby asking username changing
     * @param username new username
     */
    public void changeUsernameRequest(String username) {
        if (! checkRights(Status.COMMON)) {
            return;
        }
        addCommandToSend(new ChangeUsernameRequest(username), ProtocolConstants.CLIENT_INDEX);
    }

    /***
     * Sends request to lobby asking user status changing
     * @param username target user
     * @param status new status
     */
    public void changeUserStatusRequest(String username, Status status) {
        if (! checkRights(Status.MODERATOR)) {
            return;
        }
        if (getUsername().equals(username)) {
            String message = "You can not change user status of yourself!";
            log(message);
            showMessage(message, ShowMessageEvent.MessageType.ERROR);
            return;
        }
        if (! (status.ordinal() >= Status.READONLY.ordinal() &&
                status.ordinal() <= Status.MODERATOR.ordinal())) {
            String message = "You can only change status to \"readonly\", \"common\" and \"moderator\"!";
            log(message);
            showMessage(message, ShowMessageEvent.MessageType.ERROR);
            return;
        }
        addCommandToSend(new ChangeUserStatusRequest(username, status), ProtocolConstants.CLIENT_INDEX);
    }

    /***
     * Sends request to lobby asking route delegating
     * @param username target user
     */
    public void delegateRootRequest(String username) {
        if (! checkRights(Status.LOBBY_ROOT)) {
            return;
        }
        addCommandToSend(new DelegateRootRequest(username), ProtocolConstants.CLIENT_INDEX);
    }

    /***
     * Sends request_to_node to lobby asking user kicking
     * @param username target user
     */
    public void kickRequest(String username) {
        if (! checkRights(Status.MODERATOR)) {
            return;
        }
        if (getUsername().equals(username)) {
            String message = "You can not kick yourself!";
            log(message);
            showMessage(message, ShowMessageEvent.MessageType.ERROR);
            return;
        }
        addCommandToSend(new KickRequest(username), ProtocolConstants.CLIENT_INDEX);
    }

    /***
     * Sends request_to_node to lobby asking user baning
     * @param username target user
     */
    public void banRequest(String username) {
        if (! checkRights(Status.MODERATOR)) {
            return;
        }
        if (getUsername().equals(username)) {
            String message = "You can not ban yourself!";
            log(message);
            showMessage(message, ShowMessageEvent.MessageType.ERROR);
            return;
        }
        addCommandToSend(new BanRequest(username), ProtocolConstants.CLIENT_INDEX);
    }

    /***
     * Sends request_to_node to lobby asking user unbaning
     * @param username target user
     */
    public void unbanRequest(String username) {
        if (! checkRights(Status.MODERATOR)) {
            return;
        }
        addCommandToSend(new UnbanRequest(username), ProtocolConstants.CLIENT_INDEX);
    }

    /***
     * Deletes board from server
     */
    public void deleteBoard() {
        if (! checkRights(Status.LOBBY_ROOT)) {
            return;
        }
        addSetupEvent(new DeleteBoardEvent());
    }

    public void processMainClientEvent(MainClientEvent event) {
        switch (event.getIndex()) {
            case CHANGE_LOBBY_NAME_E: {
                lobbyInfo.setLobbyName(((ChangeLobbyNameEvent) event).getLobbyName());
                break;
            }
            case CHANGE_USERNAME_E: {
                connection.setUsername(((ChangeUsernameEvent) event).getNewName());
                break;
            }
            case CHANGE_USER_STATUS_E: {
                Status status = ((ChangeUserStatusEvent) event).getStatus();
                if (status.ordinal() < Status.MODERATOR.ordinal()) {
                    banned.clear();
                }
                connection.setStatus(status);
                break;
            }
            case KICK_E: {
                disconnect("You were kicked from lobby " + lobbyInfo.getLobbyName() + "!");
                break;
            }
            case BAN_E: {
                disconnect("You were banned in lobby " + lobbyInfo.getLobbyName() + "!");
                break;
            }
            case USER_LIST_E: {
                users = ((SetUserListEvent) event).getUsers();
                break;
            }
            case BAN_LIST_E: {
                banned = ((SetBanListEvent) event).getUsers();
                break;
            }
        }
    }

    public String getUsername() {
        return connection.getUsername();
    }

    public Status getUserStatus() {
        return connection.getStatus();
    }

    public String getLobbyName() {
        return lobbyInfo.getLobbyName();
    }

    public Map<User, String> getUsers() {
        return users;
    }

    public Map<String, String> getBanned() {
        return banned;
    }
}