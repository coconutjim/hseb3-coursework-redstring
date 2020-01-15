package rslib.cs.protocol.events.main_client;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a changing lobby name event
 */
public class ChangeLobbyNameEvent extends MainClientEvent {

    /** For better parsing */
    public static final long serialVersionUID = 92451983662L;

    /** Old name */
    private String oldName;

    /** New name */
    private String lobbyName;

    /***
     * Constructor
     * @param oldName old name
     * @param lobbyName new name
     */
    public ChangeLobbyNameEvent(String oldName, String lobbyName) {
        if (oldName == null) {
            throw new IllegalArgumentException("ChangeLobbyNameEvent: oldName is null!");
        }
        if (lobbyName == null) {
            throw new IllegalArgumentException("ChangeLobbyNameEvent: lobbyName is null!");
        }
        this.oldName = oldName;
        this.lobbyName = lobbyName;
    }

    /***
     * Constructor for externalization
     */
    public ChangeLobbyNameEvent() {
    }

    public String getOldName() {
        return oldName;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    @Override
    public MainClientEventType getIndex() {
        return MainClientEventType.CHANGE_LOBBY_NAME_E;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(oldName);
        out.writeUTF(lobbyName);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        oldName = in.readUTF();
        lobbyName = in.readUTF();
    }
}
