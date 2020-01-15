package rslib.cs.server.util;

import rslib.cs.common.UserConnection;
import rslib.gui.board.ExternalizableBoard;

import java.util.List;

/***
 * Holds information about lobby users and board
 */
public class LobbySession {

    /** Lobby name */
    private String lobbyName;

    /** Lobby board */
    private ExternalizableBoard serializableBoard;

    /** All users */
    private List<UserConnection> users;

    /** Board users */
    private List<UserConnection> boardUsers;

    /***
     * Constructor
     * @param serializableBoard board
     * @param users all users
     * @param boardUsers board users
     */
    public LobbySession(String lobbyName, List<UserConnection> users,
                        List<UserConnection> boardUsers, ExternalizableBoard serializableBoard) {
        if (lobbyName == null) {
            throw new IllegalArgumentException("LobbySession: lobbyName is null!");
        }
        if (users == null) {
            throw new IllegalArgumentException("LobbySession: users is null!");
        }
        if (boardUsers == null) {
            throw new IllegalArgumentException("LobbySession: boardUsers is null!");
        }
        this.lobbyName = lobbyName;
        this.serializableBoard = serializableBoard;
        this.users = users;
        this.boardUsers = boardUsers;
    }

    @Override
    public String toString() {
        String s = "\nLobby: " + lobbyName + "\n\nAll users:\n";
        if (users.isEmpty()) {
            s += "No users\n";
        }
        else {
            for (UserConnection userConnection : users) {
                s += userConnection.toString() + "\n";
            }
        }
        s += "\n";
        s += "Board: " + (serializableBoard == null? "No board" : serializableBoard.toString()) + "\n\n";
        s += "Board users:\n";
        for (UserConnection userConnection : boardUsers) {
            s += userConnection.toString() + "\n";
        }
        return s;
    }
}
