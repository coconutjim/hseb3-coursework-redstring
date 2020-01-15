package rslib.cs.protocol.events.message;

import rslib.cs.protocol.events.ClientEvent;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a show message event
 */
public class ShowMessageEvent implements ClientEvent {

    public enum MessageType {
        INFO,
        ERROR
    }

    /** Message */
    private String message;

    /** Message type */
    private MessageType type;

    /***
     * Constructor
     * @param message message
     * @param type message type
     */
    public ShowMessageEvent(String message, MessageType type) {
        if (message == null) {
            throw new IllegalArgumentException("MessageEvent: message is null!");
        }
        if (type == null) {
            throw new IllegalArgumentException("MessageEvent: type is null!");
        }
        this.message = message;
        this.type = type;
    }

    /***
     * Constructor for externalization
     */
    public ShowMessageEvent() {
    }

    public String getMessage() {
        return message;
    }

    public MessageType getType() {
        return type;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(message);
        out.writeObject(type);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        message = in.readUTF();
        type = (MessageType) in.readObject();
    }
}
