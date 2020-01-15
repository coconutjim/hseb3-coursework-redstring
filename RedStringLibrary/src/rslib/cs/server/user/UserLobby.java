package rslib.cs.server.user;

import rslib.commands.common.ChangeBlockCommand;
import rslib.cs.common.*;
import rslib.cs.protocol.ProtocolConstants;
import rslib.cs.protocol.RedStringInfo;
import rslib.cs.protocol.events.bmessage.BoardMessageEvent;
import rslib.cs.protocol.events.board.BoardEvent;
import rslib.cs.protocol.events.board.board.*;
import rslib.cs.protocol.events.board.common.*;
import rslib.cs.protocol.events.board.container.*;
import rslib.cs.protocol.events.board.container.file.ChangeFileEvent;
import rslib.cs.protocol.events.board.container.image.ChangeImageEvent;
import rslib.cs.protocol.events.board.container.text.ChangeTextEvent;
import rslib.cs.protocol.events.message.ShowMessageEvent;
import rslib.cs.protocol.events.setup.BoardRequest;
import rslib.cs.protocol.events.setup.DeleteBoardEvent;
import rslib.cs.protocol.events.setup.SetBoardEvent;
import rslib.cs.protocol.events.chat.NotificationEvent;
import rslib.cs.protocol.events.main_client.*;
import rslib.cs.protocol.events.setup.SetupEvent;
import rslib.cs.protocol.requests.to_lobby.user.*;
import rslib.gui.BasicComponent;
import rslib.gui.board.ExternalizableBoard;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.container.BoardContainer;
import rslib.gui.container.ExternalizableContainer;
import rslib.gui.container.file.FileContainer;
import rslib.gui.container.image.ImageContainer;
import rslib.gui.container.text.TextContainer;
import rslib.gui.style.FontModel;
import rslib.util.DataManagement;
import rslib.cs.server.util.CommandReceiver;
import rslib.cs.server.Lobby;
import rslib.cs.server.util.LobbySession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * Represents a lobby with users
 */
public class UserLobby extends Lobby {

    /** Board instance */
    private ExternalizableBoard serverBoard;

    /** Server info */
    private LobbyInfo lobbyInfo;

    //TODO: see structure
    /** Ban list (hosts and user names) */
    private Map<String, String> banList;

    /** Lobby root */
    private UserConnection root;

    /** Board listeners */
    private List<UserConnection> boardUsers;

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
        serverBoard = null;
        selector = Selector.open();
        this.userServer = userServer;
        this.lobbyInfo = lobbyInfo;
        banList = new ConcurrentHashMap<>(); // concurrency
        boardUsers = new CopyOnWriteArrayList<>(); // concurrency
        start();
    }

    @Override
    public void start() {
        new Thread(this).start();
        new Thread(executor).start();
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
        //TODO: see
        if (userConnection == null) {
            return;
        }
        userList.remove(userConnection);
        boardUsers.remove(userConnection);
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
        log(message);
        if (users.isEmpty()) {
            if (serverBoard == null) {
                shutdown();
            }
            return;
        }
        addChatLobbyCommand(new NotificationEvent(message),
                new CommandReceiver(userList, Status.READONLY));
        if (userConnection.equals(root)) { // if disconnected user is root, delegate status
            findNewRoot(null); // anybody
        }
        if (serverBoard != null) { // if board exists
            String name = userConnection.getUsername();
            String rootName = root.getUsername();
            if (serverBoard.getComponentOwner().equals(name)) { // if was owned
                serverBoard.setComponentOwner(rootName);
                addBoardLobbyCommand(new ChangeOwnerEvent(serverBoard.hashCode(),
                        InteractiveBoard.BOARD_ID, rootName),
                        new CommandReceiver(boardUsers, Status.READONLY)); // change board owner
            }
            if (serverBoard.isBlocked() &&
                    serverBoard.getBlockOwner().equals(name)) { // if was blocked by this user
                serverBoard.setBlocked(false, null);
                addBoardLobbyCommand(new ChangeBlockEvent(serverBoard.hashCode(),
                        InteractiveBoard.BOARD_ID, false, null), new CommandReceiver(boardUsers, Status.READONLY));
            }
            for (BoardContainer bc : serverBoard.getContainers()) {
                if (bc.getComponentOwner().equals(name)) { // if was owned
                    bc.setComponentOwner(rootName);
                    addBoardLobbyCommand(new ChangeOwnerEvent(serverBoard.hashCode(),
                                    bc.getComponentId(), rootName),
                            new CommandReceiver(boardUsers, Status.READONLY)); // change container owner
                }
                if (bc.getBlockOwner() != null &&
                        bc.getBlockOwner().equals(name)) { // if was blocked by this user
                    bc.setBlocked(false, null);
                    addBoardLobbyCommand(new ChangeBlockEvent(serverBoard.hashCode(),
                            bc.getComponentId(), false, null), new CommandReceiver(boardUsers, Status.READONLY));
                }
            }
        }
        sendUserListToAll();
    }

    @Override
    protected void processReadCommand(CommandInfo command, UserConnection
            connection) throws IllegalStateException,
            ClassCastException, ClassNotFoundException, IOException {
        ByteBuffer buffer = command.getCommand();
        byte commandType = command.getCommandType();
        String commandClass;
        switch (commandType) {
            case ProtocolConstants.CLIENT_INDEX: {
                // if it is a command to lobby, handle it
                UserLobbyRequest request = (UserLobbyRequest) DataManagement.inflate(buffer);
                commandClass = request.getClass().getSimpleName();
                commandLog("Received " + commandClass + " from " + connection.toString() +
                        "(" + buffer.capacity() + " bytes)!");
                handleClientRequest(request, connection);
                return;
            }
            case ProtocolConstants.CHAT_INDEX: {
                commandClass = "chat event";
                commandLog("Received " + commandClass + " from " + connection.toString() +
                        "(" + buffer.capacity() + " bytes)!");
                CommandReceiver receiver = new CommandReceiver(userList, Status.READONLY);
                receiver.removeReceiver(connection);
                addLobbyCommand(new CommandInfo(buffer, ProtocolConstants.CHAT_INDEX, commandClass), receiver);
                return;
            }
            case ProtocolConstants.SETUP_INDEX: {
                SetupEvent setupEvent = (SetupEvent) DataManagement.inflate(buffer);
                commandClass = setupEvent.getClass().getSimpleName();
                commandLog("Received " + commandClass + " from " + connection.toString() +
                        "(" + buffer.capacity() + " bytes)!");
                handleSetupEvent(setupEvent, connection);
                return;
            }
            case ProtocolConstants.BOARD_INDEX: {
                BoardEvent boardEvent = (BoardEvent) DataManagement.inflate(buffer);
                commandClass = boardEvent.getClass().getSimpleName();
                commandLog("Received " + commandClass + " from " + connection.toString() +
                        "(" + buffer.capacity() + " bytes)!");
                try {
                    if (handleBoardEvent(boardEvent, connection)) {
                        CommandReceiver receiver = new CommandReceiver(boardUsers, Status.READONLY);
                        addLobbyCommand(new CommandInfo(buffer, ProtocolConstants.BOARD_INDEX, commandClass), receiver);
                    }
                }
                catch (Exception e) {
                    errorLog("Error while processing board event: " + e.getMessage());
                }
                return;
            }
        }
        throw new IllegalStateException();
    }

    /***
     * Checks board synchronization
     * @param boardEvent event
     * @param connection event sender
     * @return if sender board is synchronized
     */
    private boolean synchronization(BoardEvent boardEvent, UserConnection connection) {
        if (boardEvent.getHash() != serverBoard.hashCode()) {
            addSetupLobbyCommand(new SetBoardEvent(serverBoard), new CommandReceiver(connection));
            String message = connection.toString() + " board is not actual, sent server board!";
            log(message);
            addBoardMessageLobbyCommand(new BoardMessageEvent(message), new CommandReceiver(connection));
            return false;
        }
        return true;
    }

    /***
     * Handles board event
     * @param boardEvent board event
     * @param connection associated connection
     * @return true if event can be implemented, false otherwise
     */
    private boolean handleBoardEvent(BoardEvent boardEvent, UserConnection connection) {
        if (! serverBoard.isAsynchronous()) {
            if (! synchronization(boardEvent, connection)) {
                return false;
            }
        }
        boolean success = false;
        String message = null;
        switch (boardEvent.getIndex()) {
            case SEND_HASH_E: {
                if (synchronization(boardEvent, connection)) {
                    message = connection.toString() + " board is equal to server board!";
                    success = false; // it is true actually, but no need to transfer this event
                }
                break;
            }
            case CHANGE_SYNC_MODE_E: {
                ChangeSyncModeEvent csme = (ChangeSyncModeEvent) boardEvent;
                if (checkComponent(connection, serverBoard, Status.LOBBY_ROOT)) {
                    serverBoard.setAsynchronous(csme.isAsynchronous());
                    message = connection.toString() + " changed sync mode!";
                    success = true;
                }
                else {
                    message = connection.toString() + " has no rights to change sync mode!";
                    success = false;
                }
                break;
            }
            case POINT_E: {
                message = connection.toString() + " pointed!";
                success = true;
                break;
            }
            case CLEAR_BOARD_E: {
                if (checkComponent(connection, serverBoard, Status.MODERATOR)) {
                    serverBoard.clearBoard();
                    message = connection.toString() + " cleared the board!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to clear the board!";
                    success = false;
                }
                break;
            }
            case SET_BOARD_CONTENT_E: {
                CopyOnWriteArrayList<ExternalizableContainer> content =
                        ((SetBoardContentEvent) boardEvent).getSerializableContainers();
                if (checkComponent(connection, serverBoard)) {
                    serverBoard.setBoardContent(content);
                    message = connection.toString() + " set new board content!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to set board content!";
                    success = false;
                }
                break;
            }
            case ADD_CONTAINER_E: {
                ExternalizableContainer container = ((AddContainerEvent) boardEvent).getSerializableContainer();
                if (checkComponent(connection, serverBoard)) {
                    serverBoard.addContainer(container);
                    message = connection.toString() + " added container " + container.getComponentId() + "!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to add container!";
                    success = false;
                }
                break;
            }
            case DELETE_CONTAINER_E: {
                int id = ((DeleteContainerEvent) boardEvent).getId();
                BoardContainer container = serverBoard.findContainer(id);
                if (container == null) {
                    message = "Unable to find component by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, container)) {
                    serverBoard.deleteContainer(id);
                    message = connection.toString() + " deleted container " + container.getComponentId() + "!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to delete container: " +
                            container.getComponentId() + "!";
                    success = false;
                }
                break;
            }
            case CHANGE_GENERAL_COLOR_E: {
                ChangeGeneralColorEvent cgce = (ChangeGeneralColorEvent) boardEvent;
                if (checkComponent(connection, serverBoard, Status.LOBBY_ROOT)) {
                    if (cgce.getForeground() != null) {
                        serverBoard.setGeneralContainerForeground(cgce.getForeground());
                        for (BoardContainer container : serverBoard.getContainers()) {
                            if (! container.isBlocked()) {
                                container.setComponentForeground(cgce.getForeground());
                            }
                        }
                        message = connection.toString() + " changed general container foreground color!";
                    }
                    if (cgce.getBackground() != null) {
                        serverBoard.setGeneralContainerBackground(cgce.getBackground());
                        for (BoardContainer container : serverBoard.getContainers()) {
                            if (! container.isBlocked()) {
                                container.setComponentBackground(cgce.getBackground());
                            }
                        }
                        if (cgce.getForeground() != null) {
                            message += "\n" + connection.toString() + " changed general container background color!";
                        }
                        else {
                            message = connection.toString() + " changed general container background color!";
                        }

                    }
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to change general container color!";
                    success = false;
                }
                break;
            }
            case CHANGE_GENERAL_FONT_E: {
                FontModel font = ((ChangeGeneralFontEvent) boardEvent).getFont();
                if (checkComponent(connection, serverBoard, Status.LOBBY_ROOT)) {
                    serverBoard.setGeneralContainerFont(font);
                    for (BoardContainer container : serverBoard.getContainers()) {
                        if (! container.isBlocked()) {
                            container.setComponentFont(font);
                        }
                    }
                    message = connection.toString() + " changed general container font!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to change general container font!";
                    success = false;
                }
                break;
            }
            case CHANGE_GENERAL_OPAQUE_E: {
                boolean opaque = ((ChangeGeneralOpaqueEvent) boardEvent).isOpaque();
                if (checkComponent(connection, serverBoard, Status.LOBBY_ROOT)) {
                    serverBoard.setGeneralContainerOpaque(opaque);
                    for (BoardContainer container : serverBoard.getContainers()) {
                        if (! container.isBlocked()) {
                            container.setComponentOpaque(opaque);
                        }
                    }
                    message = connection.toString() + " changed general container opaque!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to change general container opaque!";
                    success = false;
                }
                break;
            }
            case CHANGE_BLOCK_E: {
                ChangeBlockEvent cbe = ((ChangeBlockEvent) boardEvent);
                BasicComponent component = cbe.getId() == InteractiveBoard.BOARD_ID ? serverBoard :
                        serverBoard.findContainer(cbe.getId());
                if (component == null) {
                    message = "Unable to find component by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, component)) {
                    boolean block = cbe.isBlock();
                    component.setBlocked(block, cbe.getUsername());
                    if (block) {
                        message = connection.toString() + " blocked " + component.getComponentId() + "!";
                    }
                    else {
                        message = connection.toString() + " unblocked " + component.getComponentId() + "!";
                    }
                    success = true;
                }
                else {
                    message =connection.toString() + " has no rights to change block of " +
                            component.getComponentId() + "!";
                    success = false;
                }
                break;
            }
            case RESIZE_E: {
                ResizeEvent resizeEvent = (ResizeEvent) boardEvent;
                BasicComponent component = resizeEvent.getId() == InteractiveBoard.BOARD_ID ? serverBoard :
                        serverBoard.findContainer(resizeEvent.getId());
                if (component == null) {
                    message = "Unable to find component by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, component)) {
                    if (checkResizing(component, resizeEvent)) {
                        component.resizeComponent(resizeEvent.getLeft(), resizeEvent.getTop(),
                                resizeEvent.getWidth(), resizeEvent.getHeight());
                        message = connection.toString() + " resized " + component.getComponentId() + "!";
                        if (resizeEvent.isUnblock()) {
                            component.setBlocked(false, null);
                            message += "\n" +
                                    connection.toString() + " unblocked " + component.getComponentId() + "!";
                        }
                        success = true;
                    }
                    else {
                        message = connection.toString() + " tried to perform illegal resizing of " +
                                component.getComponentId() + "!";
                        success = false;
                    }
                }
                else {
                    message = connection.toString() + " has no rights to resize " +
                            component.getComponentId() + "!";
                    success = false;
                }
                break;
            }
            case MOVE_E: {
                MoveEvent moveEvent = (MoveEvent) boardEvent;
                BasicComponent component = moveEvent.getId() == InteractiveBoard.BOARD_ID ? serverBoard :
                        serverBoard.findContainer(moveEvent.getId());
                if (component == null) {
                    message = "Unable to find component by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, component)) {
                    component.moveComponent(moveEvent.getLeft(), moveEvent.getTop());
                    message = connection.toString() + " moved " + component.getComponentId() + "!";
                    if (moveEvent.isUnblock()) {
                        component.setBlocked(false, null);
                        message += "\n" +
                                connection.toString() + " unblocked " + component.getComponentId() + "!";
                    }
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to move component " +
                            component.getComponentId() + "!";
                    success = false;
                }
                break;
            }
            case CHANGE_NAME_E: {
                ChangeNameEvent cne = (ChangeNameEvent) boardEvent;
                BasicComponent component = cne.getId() == InteractiveBoard.BOARD_ID ? serverBoard :
                        serverBoard.findContainer(cne.getId());
                if (component == null) {
                    message = "Unable to find component by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, component)) {
                    component.setComponentName(cne.getName());
                    message = connection.toString() + " changed " + component.getComponentId() + " name to " +
                            cne.getName() + "!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to change component " +
                            component.getComponentId() + " name!";
                    success = false;
                }
                break;
            }
            case CHANGE_COLOR_E: {
                ChangeColorEvent cce = (ChangeColorEvent) boardEvent;
                BasicComponent component = cce.getId() == InteractiveBoard.BOARD_ID ? serverBoard :
                        serverBoard.findContainer(cce.getId());
                if (component == null) {
                    message ="Unable to find component by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, component)) {
                    if (cce.getForeground() != null) {
                        component.setComponentForeground(cce.getForeground());
                        message = connection.toString() + " changed " + component.getComponentId() + " foreground color!";
                    }
                    if (cce.getBackground() != null) {
                        component.setComponentBackground(cce.getBackground());
                        if (cce.getForeground() != null) {
                            message += "\n" + connection.toString() + " changed " + component.getComponentId() + " background color!";
                        }
                        else {
                            message = connection.toString() + " changed " + component.getComponentId() + " background color!";
                        }
                    }
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to change component " +
                            component.getComponentId() + " color!";
                    success = false;
                }
                break;
            }
            case CHANGE_OPAQUE_E: {
                ChangeOpaqueEvent coe = (ChangeOpaqueEvent) boardEvent;
                BasicComponent component = coe.getId() == InteractiveBoard.BOARD_ID ? serverBoard :
                        serverBoard.findContainer(coe.getId());
                if (component == null) {
                    message = "Unable to find component by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, component)) {
                    component.setComponentOpaque(coe.isOpaque());
                    message = connection.toString() + " changed " + component.getComponentId() + " opaque!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to change component " +
                            component.getComponentId() + " opaque!";
                    success = false;
                }
                break;
            }
            case CHANGE_FONT_E: {
                ChangeFontEvent cfe = (ChangeFontEvent) boardEvent;
                BasicComponent component = cfe.getId() == InteractiveBoard.BOARD_ID ? serverBoard :
                        serverBoard.findContainer(cfe.getId());
                if (component == null) {
                    message = "Unable to find component by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, component)) {
                    component.setComponentFont(cfe.getFont());
                    message = connection.toString() + " changed " + component.getComponentId() + " font!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to change component " +
                            component.getComponentId() + " font!";
                    success = false;
                }
                break;
            }
            case CHANGE_OWNER_E: {
                ChangeOwnerEvent ccoe = (ChangeOwnerEvent) boardEvent;
                BasicComponent component = ccoe.getId() == InteractiveBoard.BOARD_ID ? serverBoard :
                        serverBoard.findContainer(ccoe.getId());
                if (component == null) {
                    message = "Unable to find component by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, component, Status.MODERATOR )
                        && component.getComponentId() != InteractiveBoard.BOARD_ID) {
                    if (component.isBlocked()) {
                        message = connection.toString() + " can not change component " +
                                component.getComponentId() + " owner while it is blocked!";
                        success = false;
                        break;
                    }
                    component.setComponentOwner(ccoe.getNewOwner());
                    message = connection.toString() + " changed " + component.getComponentId() + " owner!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to change component " +
                            component.getComponentId() + " owner!";
                    success = false;
                }
                break;
            }
            case CHANGE_STATUS_E: {
                ChangeStatusEvent ccse = (ChangeStatusEvent) boardEvent;
                BasicComponent component = ccse.getId() == InteractiveBoard.BOARD_ID ? serverBoard :
                        serverBoard.findContainer(ccse.getId());
                if (component == null) {
                    message = "Unable to find component by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, component, Status.MODERATOR )
                        || (component.getComponentId() == InteractiveBoard.BOARD_ID &&
                        connection.getStatus() == Status.LOBBY_ROOT)) {
                    component.setComponentStatus(ccse.getNewStatus());
                    message = connection.toString() + " changed " + component.getComponentId() + " status!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to change component " +
                            component.getComponentId() + " status!";
                    success = false;
                }
                break;
            }
            case CHANGE_CONTAINER_LAYER_E: {
                ChangeContainerLayerEvent ccle = (ChangeContainerLayerEvent) boardEvent;
                BoardContainer container = serverBoard.findContainer(ccle.getId());
                if (container == null) {
                    message = "Unable to find component by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, container)) {
                    serverBoard.setLayerPosition(container, ccle.getLayer());
                    message = connection.toString() + " changed container " + container.getComponentId() + " layer!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to change container " +
                            container.getComponentId() + "layer!";
                    success = false;
                }
                break;
            }
            case CLEAR_CONTAINER_E: {
                int id = ((ClearContainerEvent) boardEvent).getId();
                BoardContainer container = serverBoard.findContainer(id);
                if (container == null) {
                    message = "Unable to find component by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, container)) {
                    container.clearContainer();
                    message = connection.toString() + " cleared container: " + container.getComponentId() + "!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to clear container " +
                            container.getComponentId() + "!";
                    success = false;
                }
                break;
            }
            case SET_CONTAINER_CONTENT_E: {
                SetContainerContentEvent scce = (SetContainerContentEvent) boardEvent;
                int id = scce.getId();
                BoardContainer container = serverBoard.findContainer(id);
                if (container == null) {
                    message = "Unable to find component by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, container)) {
                    container.setContent(scce.getSerializableContainer());
                    message = connection.toString() + " set content to container: " + container.getComponentId() + "!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights set content to container " +
                            container.getComponentId() + "!";
                    success =  false;
                }
                break;
            }
            case CHANGE_TEXT_E: {
                ChangeTextEvent ste = (ChangeTextEvent) boardEvent;
                TextContainer textContainer;
                try {
                    textContainer = (TextContainer) serverBoard.findContainer(ste.getId());
                    if (textContainer == null) {
                        throw new ClassCastException();
                    }
                }
                catch (ClassCastException e) {
                    message = "Unable to find text container by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, textContainer)) {
                    textContainer.setText(ste.getText());
                    message = connection.toString() + " set text to " + textContainer.getComponentId() + "!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to set text to " +
                            textContainer.getComponentId() + "!";
                    success = false;
                }
                break;
            }
            case CHANGE_IMAGE_E: {
                ChangeImageEvent cie = (ChangeImageEvent) boardEvent;
                ImageContainer imageContainer;
                try {
                    imageContainer = (ImageContainer) serverBoard.findContainer(cie.getId());
                    if (imageContainer == null) {
                        throw new ClassCastException();
                    }
                }
                catch (ClassCastException e) {
                    message = "Unable to find text container by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, imageContainer)) {
                    imageContainer.setImage(cie.getImage());
                    message = connection.toString() + " set image to " + imageContainer.getComponentId() + "!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to set image to " +
                            imageContainer.getComponentId() + "!";
                    success = false;
                }
                break;
            }
            case CHANGE_FILE_E: {
                ChangeFileEvent cfe = (ChangeFileEvent) boardEvent;
                FileContainer fileContainer;
                try {
                    fileContainer = (FileContainer) serverBoard.findContainer(cfe.getId());
                    if (fileContainer == null) {
                        throw new ClassCastException();
                    }
                }
                catch (ClassCastException e) {
                    message = "Unable to find text container by id!";
                    success = false;
                    break;
                }
                if (checkComponent(connection, fileContainer)) {
                    fileContainer.setFile(cfe.getFile());
                    message = connection.toString() + " set file to " + fileContainer.getComponentId() + "!";
                    success = true;
                } else {
                    message = connection.toString() + " has no rights to set file to " +
                            fileContainer.getComponentId() + "!";
                    success = false;
                }
                break;
            }
        }
        if (message != null) {
            log(message);
            if (success) {
                addBoardMessageLobbyCommand(new BoardMessageEvent(message),
                        new CommandReceiver(boardUsers, Status.READONLY));
            }
            else {
                addBoardMessageLobbyCommand(new BoardMessageEvent(message),
                        new CommandReceiver(connection));
            }
        }
        return success;
    }

    /***
     * Checks if the component can be processed
     * Compares user status and component status and component owner
     * Checks whether it is blocked and user has rights to modify it
     * @param connection user
     * @param component component
     * @return if user has rights to modify the component
     */
    private boolean checkComponent(UserConnection connection, BasicComponent component) {
        return (component.getComponentStatus().ordinal() <= connection.getStatus().ordinal() ||
                connection.getUsername().equals(component.getComponentOwner())) &&
                (! (component.isBlocked() && ! connection.getUsername().equals(component.getBlockOwner())));
    }

    /***
     * Checks if the component can be processed
     * Compares specified status and user status
     * Compares user status and component status and component owner
     * Checks whether it is blocked and user has rights to modify it
     * @param connection user
     * @param component component
     * @param status specified status
     * @return if user has rights to modify the component
     */
    private boolean checkComponent(UserConnection connection, BasicComponent component, Status status) {
        return (status.ordinal() <= connection.getStatus().ordinal() &&
                (component.getComponentStatus().ordinal() <= connection.getStatus().ordinal() ||
                connection.getUsername().equals(component.getComponentOwner())) &&
                (! (component.isBlocked() && ! connection.getUsername().equals(component.getBlockOwner()))));
    }

    //TODO check moving
    /***
     * Checks if component can be resized
     * @param component component
     * @param event resize event
     * @return if component can be resized
     */
    private boolean checkResizing(BasicComponent component, ResizeEvent event) {
        if (event.getWidth() < component.getComponentMinimumWidth() || event.getHeight() <
                component.getComponentMinimumHeight() || event.getWidth() > component.getComponentMaximumWidth() ||
                event.getHeight() > component.getComponentMaximumHeight()) {
            return false;
        }
        if (component.getComponentId() == InteractiveBoard.BOARD_ID) {
            InteractiveBoard board = (InteractiveBoard) component;
            int minWidth = board.getComponentMinimumWidth();
            int minHeight = board.getComponentMinimumHeight();
            for (BoardContainer container : serverBoard.getContainers()) {
                int borderX = container.getComponentLeft() + container.getComponentWidth() +
                        InteractiveBoard.BORDER_DELTA;
                if (borderX > minWidth) {
                    minWidth = borderX;
                }
                int borderY = container.getComponentTop() + container.getComponentHeight() +
                        InteractiveBoard.BORDER_DELTA;
                if (borderY > minHeight) {
                    minHeight = borderY;
                }
            }
            return event.getWidth() >= minWidth && event.getHeight() >= minHeight;
        }
        return true;
    }

    /***
     * Processes client request
     * @param setupEvent setup event
     * @param connection associated connection
     */
    private void handleSetupEvent(SetupEvent setupEvent, UserConnection connection) {
        switch (setupEvent.getIndex()) {
            case BOARD_R: {
                BoardRequest boardRequest = (BoardRequest) setupEvent;
                log("New board request from " + connection.toString() + "!");
                ExternalizableBoard serializableBoard = boardRequest.getSerializableBoard();
                if (serverBoard != null) {
                    serializableBoard = serverBoard.toExternalizable();
                    log("Sent current server board to " + connection.toString() + "!");
                    boardUsers.add(connection); // add to board users
                    log(connection.toString() + " is new board user!");
                    addSetupLobbyCommand(new SetBoardEvent(serializableBoard), new CommandReceiver(connection));
                }
                else {
                    if (connection.equals(root)) {
                        serverBoard = new ExternalizableBoard(serializableBoard);
                        log(connection.toString() + "Created new server board!");
                        boardUsers.add(connection); // add to board users
                        addSetupLobbyCommand(new SetBoardEvent(serializableBoard), new CommandReceiver(connection));
                    }
                    else {
                        log(connection.toString() + "has no rights to create board!");
                        addMessageLobbyCommand(new ShowMessageEvent("Lobby has not board now. Only lobby root can" +
                                        " create a new one!",
                                ShowMessageEvent.MessageType.ERROR),
                                new CommandReceiver(connection));
                    }
                }
                break;
            }
            case CLOSE_BOARD_E: {
                boardUsers.remove(connection); // remove from board users
                log(connection.toString() + " is not board user anymore!");
                break;
            }
            case SET_BOARD_E: {
                if (checkComponent(connection, serverBoard, Status.LOBBY_ROOT)) {
                    serverBoard = ((SetBoardEvent) setupEvent).getSerializableBoard();
                    if (serverBoard.isBlocked()) {
                        serverBoard.setBlocked(false, null);
                    }
                    serverBoard.setComponentOwner(root.getUsername());
                    for (BoardContainer container : serverBoard.getContainers()) {
                        if (container.isBlocked()) {
                            container.setBlocked(false, null);
                        }
                        boolean contains = false;
                        for (UserConnection us : boardUsers) {
                            if (container.getComponentOwner().equals(us.getUsername())) {
                                contains  = true;
                            }
                        }
                        if (! contains) {
                            container.setComponentOwner(root.getUsername());
                        }
                    }
                    addSetupLobbyCommand(new SetBoardEvent(serverBoard),
                            new CommandReceiver(boardUsers, Status.READONLY));
                    log(connection.toString() + " set new board!");
                }
                else {
                    log(connection.toString() + " has no rights to set board!");
                }
                break;
            }
            case DELETE_BOARD_E: {
                if (checkComponent(connection, serverBoard, Status.LOBBY_ROOT)) {
                    addSetupLobbyCommand(new DeleteBoardEvent(),
                            new CommandReceiver(boardUsers, Status.READONLY));
                    serverBoard = null;
                    log(connection.toString() + " deleted server board!");
                }
                else {
                    log(connection.toString() + " has no rights to delete server board!");
                }
                break;
            }
        }
    }

    /***
     * Processes client request
     * @param request request
     * @param connection associated connection
     */
    private void handleClientRequest(UserLobbyRequest request, UserConnection connection) {
        switch (request.getIndex()) {
            case GET_USER_LIST_R: {
                sendUserList(connection);
                break;
            }
            case GET_BAN_LIST_R: {
                sendBanList(connection);
                break;
            }
            case CHANGE_LOBBY_NAME_R: {
                changeLobbyName(((ChangeLobbyNameRequest) request).getLobbyName(), connection);
                break;
            }
            case CHANGE_LOBBY_PASSWORD_R: {
                changeLobbyPassword(((ChangeLobbyPasswordRequest) request).getPassword(), connection);
                break;
            }
            case CHANGE_USERNAME_R: {
                ChangeUsernameRequest changeUsernameRequest = (ChangeUsernameRequest) request;
                changeUsername(changeUsernameRequest.getUsername(), connection);
                break;
            }
            case CHANGE_USER_STATUS_R: {
                ChangeUserStatusRequest changeUserStatusRequest = (ChangeUserStatusRequest) request;
                changeUserStatus(changeUserStatusRequest.getUsername(),
                        changeUserStatusRequest.getStatus(), connection);
                break;
            }
            case DELEGATE_ROOT_R: {
                delegateRoot(((DelegateRootRequest) request).getUsername(), connection);
                break;
            }
            case KICK_R: {
                kickUser(((KickRequest) request).getUsername(), connection);
                break;
            }
            case BAN_R: {
                banUser(((BanRequest) request).getUsername(), connection);
                break;
            }
            case UNBAN_R: {
                unbanUser(((UnbanRequest) request).getUsername(), connection);
                break;
            }
        }
    }

    @Override
    protected synchronized void registerNewUsers() throws IOException {
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
                addClientLobbyCommand(new ChangeUsernameEvent(newName), new CommandReceiver(userConnection));
                addChatLobbyCommand(new NotificationEvent(message1),
                        new CommandReceiver(userList, Status.READONLY));
                log(message1);
            }
            String message = userConnection.toString() + " connected!";
            addChatLobbyCommand(new NotificationEvent(message),
                    new CommandReceiver(userList, Status.READONLY));
            if (users.isEmpty()) {
                makeRoot(userConnection);
                if (serverBoard != null) {
                    String name = userConnection.getUsername();
                    serverBoard.setComponentOwner(name);
                    for (BoardContainer bc : serverBoard.getContainers()) {
                        bc.setComponentOwner(name);
                    }
                }
            }
            log(message);
            users.put(key, userConnection);
            userList.add(userConnection);
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
        Map<User, String> userList = new HashMap<>();
        Set <Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, UserConnection> entry: entries) {
            UserConnection connection = entry.getValue();
            String host = hosts ? connection.getHost() : null;
            userList.put(connection.getUser(), host);
        }
        return userList;
    }

    /***
     * Sends user list
     * @param connection associated connection
     */
    private void sendUserList(UserConnection connection) {
        Map<User, String> userList = getUserListToSend(connection.getStatus().ordinal() >= Status.MODERATOR.ordinal());
        addClientLobbyCommand(new SetUserListEvent(userList), new CommandReceiver(connection));
    }

    /***
     * Sends user list to all users
     */
    private void sendUserListToAll() {
        Set<Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, UserConnection> entry : entries) {
            sendUserList(entry.getValue());
        }
    }

    /***
     * Sends ban list
     * @param connection command sender
     */
    private void sendBanList(UserConnection connection) {
        addClientLobbyCommand(new SetBanListEvent(banList), new CommandReceiver(connection));
    }

    /***
     * Sends ban list to all
     */
    private void sendBanListToAll() {
        addClientLobbyCommand(new SetBanListEvent(banList), new CommandReceiver(userList, Status.MODERATOR));
    }

    /***
     * Getting lobby session
     * @return lobby session
     */
    public LobbySession getSession() {
        return new LobbySession(lobbyInfo.getLobbyName(), userList, boardUsers, serverBoard);
    }

    /***
     * Changing lobby name by user
     * @param lobbyName new lobby name
     * @param connection request sender
     */
    private void changeLobbyName(String lobbyName, UserConnection connection) {
        if (connection.getStatus().ordinal() < Status.MODERATOR.ordinal()) { // check sender status
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
        if (lobbyName.equals(lobbyInfo.getLobbyName()) || ! DataChecking.isLobbyNameValid(lobbyName)) {
            return;
        }
        String oldName = lobbyInfo.getLobbyName();
        String newName = userServer.checkLobbyName(lobbyName);
        userServer.changeLobbyName(lobbyInfo.getLobbyName(), newName);
        lobbyInfo.setLobbyName(newName);
        message += newName + "!";
        addClientLobbyCommand(new ChangeLobbyNameEvent(oldName, newName),
                new CommandReceiver(userList, Status.READONLY));
        addChatLobbyCommand(new NotificationEvent(message),
                new CommandReceiver(userList, Status.READONLY)); // notification
        log(message);
    }

    /***
     * Changing lobby password by user
     * @param password new lobby password
     * @param connection request sender
     */
    private void changeLobbyPassword(byte[] password, UserConnection connection) {
        if (connection.getStatus().ordinal() < Status.MODERATOR.ordinal()) { // check sender status
            log("Changing lobby password from illegal user: " + connection.toString() + "!");
            return;
        }
        String message = connection.toString() + " changed lobby password!";
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
        if (lobbyInfo.isSecured() && password == null) {
            lobbyInfo.setSecured(false);
        }
        if (! lobbyInfo.isSecured() && password != null) {
            lobbyInfo.setSecured(true);
        }
        lobbyInfo.setPassword(password); // changing it here
        //TODO: send new password?
        addChatLobbyCommand(new NotificationEvent(message),
                new CommandReceiver(userList, Status.MODERATOR)); // notification
        log(message);
    }

    /***
     * Changing username by user
     * @param name new username
     * @param connection associated connection
     */
    private void changeUsername(String name, UserConnection connection) {
        String oldName = connection.getUsername();
        String message = connection.toString() + " changed name to ";
        changeUsername(connection, oldName, name, message);

    }

    /***
     * Changing username internally
     * @param oldName old username
     * @param newName new username
     */
    public void changeUsername(String oldName, String newName) {
        Map.Entry<SelectionKey, UserConnection> entry = getUser(oldName);
        if (entry == null) {
            String message = "Not found user " + oldName + "!";
            log(message);
            return;
        }
        UserConnection target = entry.getValue();
        String message = target.toString() + " name was internally changed to ";
        changeUsername(target, oldName, newName, message);
    }

    /***
     * Changing username
     * @param connection target user
     * @param oldName old username
     * @param name new username
     * @param message message to users
     */
    private void changeUsername(UserConnection connection,String oldName,
                                String name, String message) {
        if (name.equals(oldName) || ! DataChecking.isUsernameValid(name)) { // if new name is equals to the old one
            return;
        }
        String newName = checkUsername(name); // checking collisions
        message += newName + "!";
        connection.setUsername(newName); // changing name here
        addClientLobbyCommand(new ChangeUsernameEvent(newName),
                new CommandReceiver(connection)); // changing it on the client
        addChatLobbyCommand(new NotificationEvent(message),
                new CommandReceiver(userList, Status.READONLY)); // notification
        if (serverBoard != null) {
            if (serverBoard.getComponentOwner().equals(oldName)) {
                serverBoard.setComponentOwner(newName);
                addBoardLobbyCommand(new ChangeOwnerEvent(serverBoard.hashCode(),
                                InteractiveBoard.BOARD_ID, newName),
                        new CommandReceiver(boardUsers, Status.READONLY));
            }
            if (serverBoard.isBlocked() && serverBoard.getBlockOwner().equals(oldName)) {
                serverBoard.setBlocked(true, newName);
                addBoardLobbyCommand(new ChangeBlockEvent(serverBoard.hashCode(),
                        serverBoard.getComponentId(), true, newName),
                        new CommandReceiver(boardUsers, Status.READONLY));
            }
            for (BoardContainer bc : serverBoard.getContainers()) {
                if (bc.getComponentOwner().equals(oldName)) {
                    bc.setComponentOwner(newName);
                    addBoardLobbyCommand(new ChangeOwnerEvent(serverBoard.hashCode(),
                                    bc.getComponentId(), newName),
                            new CommandReceiver(boardUsers, Status.READONLY));
                }
                if (bc.isBlocked() && bc.getBlockOwner().equals(oldName)) {
                    bc.setBlocked(true, newName);
                    addBoardLobbyCommand(new ChangeBlockEvent(serverBoard.hashCode(),
                                    bc.getComponentId(), true, newName),
                            new CommandReceiver(boardUsers, Status.READONLY));
                }
            }
        }
        log(message);
        sendUserListToAll();
    }

    /***
     * Changing user status by user
     * @param username target user
     * @param status new status
     * @param connection associated connection
     */
    private void changeUserStatus(String username, Status status, UserConnection connection) {
        Map.Entry<SelectionKey, UserConnection> entry = getUser(username);
        if (entry == null) {
            String message = "Not found user " + username + "!";
            addChatLobbyCommand(new NotificationEvent(message),
                    new CommandReceiver(connection)); // notification
            log(message);
            return;
        }
        UserConnection target = entry.getValue();
        if (status.ordinal() < Status.READONLY.ordinal() || status.ordinal() > Status.LOBBY_ROOT.ordinal()) {
            log("Illegal status while changing user " + target.toString() + " status from" +
                    connection.toString() + "!");
            return;
        }
        if (connection.getStatus().ordinal() < Status.MODERATOR.ordinal() ||
                target.getStatus() == Status.LOBBY_ROOT || connection.equals(target)) {
                // check sender status and target status
            log("Changing " + target.toString() + " status from illegal user: " +
                    connection.toString() + "!");
            if (target.getStatus() == Status.LOBBY_ROOT) {
                addMessageLobbyCommand(new ShowMessageEvent("You can not change user status of lobby root!",
                                ShowMessageEvent.MessageType.ERROR),
                        new CommandReceiver(connection)); // message to sender
            }
            if (connection.equals(target)) {
                addMessageLobbyCommand(new ShowMessageEvent("You can not change user status of yourself!",
                                ShowMessageEvent.MessageType.ERROR),
                        new CommandReceiver(connection)); // message to sender
            }
            return;
        }
        String message = connection.toString() + " changed " + target.toString() + " status to " +
                status.toString() + "!";
        changeUserStatus(target, status, message);
    }

    /***
     * Changing user status internally
     * @param username target user
     * @param status new status
     */
    public void changeUserStatus(String username, Status status) {
        Map.Entry<SelectionKey, UserConnection> entry = getUser(username);
        if (entry == null) {
            String message1 = "Not found user " + username + "!";
            log(message1);
            return;
        }
        SelectionKey targetKey = entry.getKey();
        UserConnection target = entry.getValue();
        if (status == Status.LOBBY_ROOT) { // if root delegating occurs
            delegateRoot(username);
            return;
        }
        if (status.ordinal() < Status.READONLY.ordinal() || status.ordinal() > Status.LOBBY_ROOT.ordinal()) {
            log("Illegal status while changing user status internally!");
            return;
        }
        boolean findNewRoot = target.equals(root); // if finding new root needed
        if (findNewRoot && users.size() == 1) { // if nothing can be changed
            log("The only user in the lobby can not be lowered in status!");
            return;
        }
        String message = target.toString()  + " status was internally changed to " +
                status.toString() + "!";
        changeUserStatus(target, status, message);
        if (findNewRoot) {
            findNewRoot(targetKey); // anybody except this user
        }
    }

    /***
     * Changing user status
     * @param target target user
     * @param status new status
     * @param message message to users
     */
    private void changeUserStatus(UserConnection target, Status status, String message) {
        if (target.getStatus() == status || ! DataChecking.isUserStatusValid(status)) {
            return;
        }
        target.setStatus(status); // changing status here
        addClientLobbyCommand(new ChangeUserStatusEvent(status), new CommandReceiver(target));
        // changing it on the client
        if (status.ordinal() >= Status.MODERATOR.ordinal()) {
            sendBanList(target);
        }
        addChatLobbyCommand(new NotificationEvent(message),
                new CommandReceiver(userList, Status.READONLY)); // notification
        log(message);
        sendUserListToAll();
    }

    /***
     * Delegating root status by user
     * @param username target user
     * @param connection associated connection
     */
    private void delegateRoot(String username, UserConnection connection) {
        Map.Entry<SelectionKey, UserConnection> entry = getUser(username);
        if (entry == null) {
            String message = "Not found user " + username + "!";
            addChatLobbyCommand(new NotificationEvent(message),
                    new CommandReceiver(connection)); // notification
            log(message);
            return;
        }
        UserConnection target = entry.getValue();
        if (connection.getStatus().ordinal() < Status.LOBBY_ROOT.ordinal()) { // check sender status
            log("Making " + target.toString() + " root from illegal user: " +
                    connection.toString() + "!");
            return;
        }
        String message = connection.getUsername() + " made " + target.toString() + " lobby root!";
        delegateRoot(target, connection, message);
    }

    /***
     * Delegating lobby root internally
     * @param username new lobby root
     */
    public void delegateRoot(String username) {
        Map.Entry<SelectionKey, UserConnection> entry = getUser(username);
        if (entry == null) {
            String message1 = "Not found user " + username + "!";
            log(message1);
            return;
        }
        UserConnection target = entry.getValue();
        // finding root
        Set <Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, UserConnection> entry1: entries) {
            if (entry1.getValue().getStatus() == Status.LOBBY_ROOT) {
                String message = target.toString() + " was internally made new lobby root!";
                delegateRoot(target, entry1.getValue(), message);
            }
        }
    }

    /***
     * Delegating lobby root
     * @param target new root
     * @param root old root
     * @param message message to users
     */
    private void delegateRoot(UserConnection target,
                              UserConnection root, String message) {
        if (target.equals(root)) {
            return;
        }
        root.setStatus(Status.MODERATOR); // down
        addClientLobbyCommand(new ChangeUserStatusEvent(Status.MODERATOR),
                new CommandReceiver(root)); // down on the client
        target.setStatus(Status.LOBBY_ROOT); // up
        addClientLobbyCommand(new ChangeUserStatusEvent(Status.LOBBY_ROOT),
                new CommandReceiver(target)); // up on the client
        addChatLobbyCommand(new NotificationEvent(message),
                new CommandReceiver(userList, Status.READONLY)); // notification
        sendUserListToAll();
        log(message);
    }

    /***
     * Kicking user by user
     * @param username target user
     * @param connection associated connection
     */
    private void kickUser(String username, UserConnection connection) {
        Map.Entry<SelectionKey, UserConnection> entry = getUser(username);
        if (entry == null) {
            String message = "Not found user " + username + "!";
            log(message);
            return;
        }
        UserConnection target = entry.getValue();
        if (connection.getStatus().ordinal() < Status.MODERATOR.ordinal() ||
                target.getStatus() == Status.LOBBY_ROOT || connection.equals(target)) {
                // checks
            log("Kicking " + target.toString() + " from illegal user: " +
                    connection.toString() + "!");
            if (target.getStatus() == Status.LOBBY_ROOT) {
                addMessageLobbyCommand(new ShowMessageEvent("You can not kick lobby root!",
                                ShowMessageEvent.MessageType.ERROR),
                        new CommandReceiver(connection)); // message to sender
            }
            if (connection.equals(target)) {
                addMessageLobbyCommand(new ShowMessageEvent("You can not kick yourself!",
                                ShowMessageEvent.MessageType.ERROR),
                        new CommandReceiver(connection)); // message to sender
            }
            return;
        }
        String message = connection.toString() + " kicked " + target.toString() + "!";
        kickUser(target, message);

    }

    /***
     * Kicking user internally
     * @param username target user
     */
    public void kickUser(String username) {
        Map.Entry<SelectionKey, UserConnection> entry = getUser(username);
        if (entry == null) {
            String message = "Not found user " + username + "!";
            log(message);
            return;
        }
        UserConnection target = entry.getValue();
        String message = target.toString() + " was kicked internally!";
        kickUser(target, message);
    }

    /***
     * Kicking user
     * @param target target user
     * @param message message to users
     */
    private void kickUser(UserConnection target, String message) {
        addClientLobbyCommand(new KickEvent(), new CommandReceiver(target)); // kick him
        addChatLobbyCommand(new NotificationEvent(message),
                new CommandReceiver(userList, Status.READONLY)); // notification
        log(message);
    }

    /***
     * Banning user by user
     * @param username target user
     * @param connection associated connection
     */
    private void banUser(String username, UserConnection connection) {
        Map.Entry<SelectionKey, UserConnection> entry = getUser(username);
        if (entry == null) {
            String message = "Not found user " + username + "!";
            log(message);
            return;
        }
        UserConnection target = entry.getValue();
        if (connection.getStatus().ordinal() < Status.MODERATOR.ordinal() ||
                target.getStatus() == Status.LOBBY_ROOT ||
                connection.equals(target)) { // checks
            log("Banning " + target.toString() + " from illegal user: " +
                    connection.toString() + "!");
            if (target.getStatus() == Status.LOBBY_ROOT) {
                addMessageLobbyCommand(new ShowMessageEvent("You can not ban lobby root!",
                                ShowMessageEvent.MessageType.ERROR),
                        new CommandReceiver(connection)); // message to sender
            }
            if (connection.equals(target)) {
                addMessageLobbyCommand(new ShowMessageEvent("You can not ban yourself!",
                                ShowMessageEvent.MessageType.ERROR),
                        new CommandReceiver(connection)); // message to sender
            }
            return;
        }
        String message = connection.toString() + " banned " + target.toString() + "!";
        banUser(target, message);

    }

    /***
     * Banning user internally
     * @param username target user
     */
    public void banUser(String username) {
        Map.Entry<SelectionKey, UserConnection> entry = getUser(username);
        if (entry == null) {
            String message = "Not found user " + username + "!";
            log(message);
            return;
        }
        UserConnection target = entry.getValue();
        String message = target.toString() + " was banned internally!";
        banUser(target, message);
    }

    /***
     * Banning user
     * @param target target user
     * @param message message to users
     */
    private void banUser(UserConnection target, String message) {
        //TODO: see banList structure
        banList.put(target.getHost(), target.getUsername()); // ban him here
        addClientLobbyCommand(new BanEvent(), new CommandReceiver(target)); // ban him on the client
        addChatLobbyCommand(new NotificationEvent(message),
                new CommandReceiver(userList, Status.READONLY)); // notification
        log(message);
        sendBanListToAll();
    }

    /***
     * Unbanning user by user
     * @param username target user
     * @param connection associated connection
     */
    private void unbanUser(String username, UserConnection connection) {
        if (connection.getStatus().ordinal() < Status.MODERATOR.ordinal()) { // check sender status
            log("Unbanning " + username + " from illegal user: " +
                    connection.toString() + "!");
            return;
        }
        String message = connection.toString() + " unbanned " + username + "!";
        if (! unbanUser(username, message)) {
            String message1 = "Not found user " + username + "in the ban list!";
            addMessageLobbyCommand(new ShowMessageEvent(message1, ShowMessageEvent.MessageType.ERROR),
                    new CommandReceiver(connection)); // message
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
                addChatLobbyCommand(new NotificationEvent(message),
                        new CommandReceiver(userList, Status.READONLY)); // notification
                log(message);
                sendBanListToAll();
                return true;
            }
        }
        return false;
    }

    /***
     * Finds new lobby root in case of root disconnection
     * @param notRoot user that can not be new root. May be null
     * @return new root
     */
    private UserConnection findNewRoot(SelectionKey notRoot) {
        Set <Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        // seeking for moderators
        for (Map.Entry<SelectionKey, UserConnection> entry: entries) {
            SelectionKey key = entry.getKey();
            UserConnection target = entry.getValue();
            if (target.getStatus() == Status.MODERATOR && (notRoot == null ||
                    ! notRoot.equals(key))) {
                makeRoot(target);
                return target;
            }
        }
        // if no, seeking for common users
        for (Map.Entry<SelectionKey, UserConnection> entry: entries) {
            SelectionKey key = entry.getKey();
            UserConnection target = entry.getValue();
            if (target.getStatus() == Status.COMMON && (notRoot == null ||
                    ! notRoot.equals(key))) {
                makeRoot(target);
                return target;
            }
        }
        // if no, seeking for read only users
        for (Map.Entry<SelectionKey, UserConnection> entry: entries) {
            SelectionKey key = entry.getKey();
            UserConnection target = entry.getValue();
            if (target.getStatus() == Status.READONLY && (notRoot == null ||
                    ! notRoot.equals(key))) {
                makeRoot(target);
                return target;
            }
        }
        return null;
    }

    /***
     * Making new lobby root
     * @param connection target user
     */
    private void makeRoot(UserConnection connection) {
        String message = connection.toString() + " is new lobby root!";
        root = connection;
        connection.setStatus(Status.LOBBY_ROOT);
        addClientLobbyCommand(new ChangeUserStatusEvent(Status.LOBBY_ROOT),
                new CommandReceiver(connection));
        sendBanList(connection);
        addChatLobbyCommand(new NotificationEvent(message),
                new CommandReceiver(userList, Status.READONLY));
        sendUserListToAll();
        log(message);
    }

    /***
     * Returns key for current user connection
     * @param username username
     * @return key and connection for this user if he exists in the lobby, null otherwise
     */
    protected Map.Entry<SelectionKey, UserConnection> getUser(String username) {
        Set <Map.Entry<SelectionKey, UserConnection>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, UserConnection> entry: entries) {
            if (entry.getValue().getUsername().equals(username)) {
                return entry;
            }
        }
        return null;
    }

    /***
     * Adds chat command to write
     * @param info command
     * @param receiver command receiver
     */
    private void addChatLobbyCommand(RedStringInfo info, CommandReceiver receiver) {
        addLobbyCommand(info, ProtocolConstants.CHAT_INDEX, receiver);
    }

    /***
     * Adds message command to write
     * @param info command
     * @param receiver command receiver
     */
    private void addMessageLobbyCommand(RedStringInfo info, CommandReceiver receiver) {
        addLobbyCommand(info, ProtocolConstants.MESSAGE_INDEX, receiver);
    }

    /***
     * Adds client command to write
     * @param info command
     * @param receiver command receiver
     */
    private void addClientLobbyCommand(RedStringInfo info, CommandReceiver receiver) {
        addLobbyCommand(info, ProtocolConstants.CLIENT_INDEX, receiver);
    }

    /***
     * Adds board command to write
     * @param info command
     * @param receiver command receiver
     */
    private void addBoardLobbyCommand(RedStringInfo info, CommandReceiver receiver) {
        addLobbyCommand(info, ProtocolConstants.BOARD_INDEX, receiver);
    }

    /***
     * Adds board message command to write
     * @param info command
     * @param receiver command receiver
     */
    private void addBoardMessageLobbyCommand(RedStringInfo info, CommandReceiver receiver) {
        addLobbyCommand(info, ProtocolConstants.BOARD_MESSAGE_INDEX, receiver);
    }

    /***
     * Adds setup command to write
     * @param info command
     * @param receiver command receiver
     */
    private void addSetupLobbyCommand(RedStringInfo info, CommandReceiver receiver) {
        addLobbyCommand(info, ProtocolConstants.SETUP_INDEX, receiver);
    }
}
