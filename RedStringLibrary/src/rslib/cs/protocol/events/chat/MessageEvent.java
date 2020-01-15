package rslib.cs.protocol.events.chat;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a new chat message event
 */
public class MessageEvent extends ChatEvent {

    /** Username */
    private String username;

    /** Message to send */
    private String message;

    /***
     * Constructor
     * @param message message to sent
     * @param username username
     */
    public MessageEvent(String username, String message) {
        if (username == null) {
            throw new IllegalArgumentException("MessageEvent: username is null!");
        }
        if (message == null) {
            throw new IllegalArgumentException("MessageEvent: message is null!");
        }
        this.username = username;
        this.message = message;
    }

    /***
     * Constructor for externalization
     */
    public MessageEvent() {
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public ChatEventType getIndex() {
        return ChatEventType.MESSAGE_E;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(username);
        out.writeUTF(message);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        username = in.readUTF();
        message = in.readUTF();
    }
}