package client_server.protocol.command_to_client.from_node.from_server;

import client_server.LobbyInfo;
import client_server.protocol.ChatInfo;

import java.util.ArrayList;

/***
 * Sends list with lobby info to user
 */
public class SendLobbyListCommand implements ChatInfo {

    /** Lobby list */
    private ArrayList<LobbyInfo> list;

    /***
     * Constructor
     * @param list list with lobbies info
     */
    public SendLobbyListCommand(ArrayList<LobbyInfo> list) {
        if (list == null) {
            throw new NullPointerException("SendLobbyListCommand: list is null");
        }
        this.list = list;
    }

    public ArrayList<LobbyInfo> getList() {
        return list;
    }
}
