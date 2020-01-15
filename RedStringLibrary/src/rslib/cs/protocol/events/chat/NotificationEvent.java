package rslib.cs.protocol.events.chat;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a notification event
 */
public class NotificationEvent extends ChatEvent {

    /** For better parsing */
    public static final long serialVersionUID = 4292758985882L;

    /** Notification message */
    private String message;

    /***
     * Constructor
     * @param message message itself
     */
    public NotificationEvent(String message) {
        if (message == null) {
            throw new IllegalArgumentException("NotificationEvent: message is null");
        }
        this.message = message;
    }

    /***
     * Constructor for externalization
     */
    public NotificationEvent() {
    }

    public String getMessage() {
        return message;
    }

    @Override
    public ChatEventType getIndex() {
        return ChatEventType.NOTIFICATION_E;
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
