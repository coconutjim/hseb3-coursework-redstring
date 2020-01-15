package rslib.cs.protocol;

/***
 * Hols constants connected with the protocol
 */
public class ProtocolConstants {

    /** Meta data */
    public static final int COMMAND_LENGTH = 4; // length of int type
    public static final int INFO_INDEX_LENGTH = 1;

    /** Command index */
    public static final byte CONNECT_INDEX = 63;
    public static final byte CLIENT_INDEX = 64;
    public static final byte ADMIN_INDEX = 65;
    public static final byte BOARD_INDEX = 66;
    public static final byte CHAT_INDEX = 67;
    public static final byte MESSAGE_INDEX = 68;
    public static final byte BOARD_MESSAGE_INDEX = 69;
    public static final byte SETUP_INDEX = 70;

    public static final byte MIN_INDEX = 63;
    public static final byte MAX_INDEX = 70;

    /** Server answer */
    public static final byte ANSWER_OK = 100;
    public static final byte ANSWER_CANCEL = 101;
}
