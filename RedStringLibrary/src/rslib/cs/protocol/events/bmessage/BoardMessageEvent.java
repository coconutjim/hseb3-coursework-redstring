package rslib.cs.protocol.events.bmessage;

import rslib.cs.protocol.events.ClientEvent;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a board message event
 */
public class BoardMessageEvent implements ClientEvent {

    /** Message */
    private String message;

    /***
     * Constructor
     * @param message message
     */
    public BoardMessageEvent(String message) {
        if (message == null) {
            throw new IllegalArgumentException("BoardMessageEvent: message is null!");
        }
        this.message = message;
    }

    /***
     * Constructor for externalization
     */
    public BoardMessageEvent() {
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(message);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        message = in.readUTF();
    }
}
