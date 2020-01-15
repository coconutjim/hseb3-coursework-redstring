package rslib.cs.protocol.events.chat;

import rslib.cs.protocol.events.ClientEvent;

/***
 * Represents a chat event
 */
public abstract class ChatEvent implements ClientEvent {

    /** For better parsing */
    public static final long serialVersionUID = 8264727673761511L;

    /** Chat event types */
    public enum ChatEventType {
        NOTIFICATION_E,
        MESSAGE_E
    }

    public abstract ChatEventType getIndex();
}
