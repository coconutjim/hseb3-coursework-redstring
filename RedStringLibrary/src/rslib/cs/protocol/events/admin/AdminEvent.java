package rslib.cs.protocol.events.admin;

import rslib.cs.protocol.events.ClientEvent;

/**
 * Represents a main client event
 */
public abstract class AdminEvent implements ClientEvent {

    /** For better parsing */
    public static final long serialVersionUID = 82676565473891L;

    public enum AdminEventType {
        SERVER_LOG_E,
        LOG_FILE_E,
        LOG_SIZE_E
    }

    public abstract AdminEventType getIndex();
}
