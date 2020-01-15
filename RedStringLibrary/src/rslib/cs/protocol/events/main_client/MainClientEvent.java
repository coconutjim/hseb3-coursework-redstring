package rslib.cs.protocol.events.main_client;

import rslib.cs.protocol.events.ClientEvent;

/**
 * Represents a main client event
 */
public abstract class MainClientEvent implements ClientEvent {

    /** For better parsing */
    public static final long serialVersionUID = 826472766221511L;

    /** Main client event types */
    public enum MainClientEventType {
        BAN_E,
        CHANGE_LOBBY_NAME_E,
        CHANGE_USERNAME_E,
        CHANGE_USER_STATUS_E,
        KICK_E,
        BAN_LIST_E,
        USER_LIST_E
    }

    public abstract MainClientEventType getIndex();
}
