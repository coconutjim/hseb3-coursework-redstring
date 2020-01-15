package rslib.cs.protocol.events.board;

import rslib.cs.protocol.events.ClientEvent;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents board event
 */
public abstract class BoardEvent implements ClientEvent {

    /** For better parsing */
    public static final long serialVersionUID = 8287652232222333L;

    /** Board hash */
    private int hash;

    /** Board event types */
    public enum BoardEventType {

        SEND_HASH_E,

        POINT_E,

        // Common
        MOVE_E,
        RESIZE_E,
        CHANGE_NAME_E,
        CHANGE_FONT_E,
        CHANGE_COLOR_E,
        CHANGE_OPAQUE_E,
        CHANGE_BLOCK_E,
        CHANGE_OWNER_E,
        CHANGE_STATUS_E,

        // Board
        SET_BOARD_CONTENT_E,
        CLEAR_BOARD_E,
        ADD_CONTAINER_E,
        DELETE_CONTAINER_E,
        CHANGE_GENERAL_FONT_E,
        CHANGE_GENERAL_COLOR_E,
        CHANGE_GENERAL_OPAQUE_E,
        CHANGE_SYNC_MODE_E,

        // Container (common)
        CHANGE_CONTAINER_LAYER_E,
        CLEAR_CONTAINER_E,
        SET_CONTAINER_CONTENT_E,

        // Text container
        CHANGE_TEXT_E,

        // Image container
        CHANGE_IMAGE_E,

        // File container
        CHANGE_FILE_E
    }

    /***
     * Constructor
     * @param hash board hash
     */
    protected BoardEvent(int hash) {
        this.hash = hash;
    }

    /***
     * Constructor for externalization
     */
    public BoardEvent() {
    }

    public abstract BoardEventType getIndex();

    public int getHash() {
        return hash;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(hash);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        hash = in.readInt();
    }
}
