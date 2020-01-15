package rslib.cs.protocol.requests.to_lobby.admin;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents internal command to admin lobby
 */
public class InternalRequest implements AdminLobbyRequest {

    /** For better parsing */
    public static final long serialVersionUID = 174687368477834L;

    /** Command text */
    private String command;

    /***
     * Constructor
     * @param command command
     */
    public InternalRequest(String command) {
        if (command == null) {
            throw new IllegalArgumentException("LogCommand: command is null!");
        }
        this.command = command;
    }

    /***
     * Constructor for externalization
     */
    public InternalRequest() {
    }

    @Override
    public UserLobbyRequestType getIndex() {
        return UserLobbyRequestType.INTERNAL_R;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(command);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        command = in.readUTF();
    }
}
