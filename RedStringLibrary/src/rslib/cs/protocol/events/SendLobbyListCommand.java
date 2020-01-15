package rslib.cs.protocol.events;

import rslib.cs.common.LobbyInfo;
import rslib.cs.protocol.RedStringInfo;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

/***
 * Sends list with lobby info to user
 */
public class SendLobbyListCommand implements RedStringInfo {

    /** For better parsing */
    public static final long serialVersionUID = 372882287663222L;

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

    /***
     * Constructor foe externalization
     */
    public SendLobbyListCommand() {
    }

    public ArrayList<LobbyInfo> getList() {
        return list;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(list);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        list = (ArrayList<LobbyInfo>) in.readObject();
    }
}
