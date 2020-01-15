package rslib.cs.protocol.events.setup;

import rslib.cs.protocol.events.ClientEvent;

/***
 * Represents a setup event
 */
public interface SetupEvent extends ClientEvent {

    /** For better parsing */
    public static final long serialVersionUID = 272866412223278L;

    /** Board event types */
    public enum SetupEventType {
        BOARD_R,
        SET_BOARD_E,
        CLOSE_BOARD_E,
        DELETE_BOARD_E
    }

    public abstract SetupEventType getIndex();
}
