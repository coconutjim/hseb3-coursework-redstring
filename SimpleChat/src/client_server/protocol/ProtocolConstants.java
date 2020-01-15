package client_server.protocol;

/***
 * Hols constants connected with the protocol
 */
public class ProtocolConstants {

    /** Meta data */
    public static final int COMMAND_LENGTH = 4; // length of int type
    public static final int INFO_INDEX_LENGTH = 1;
    public static final byte LOBBY_COMMAND_INDEX = 33;
    public static final byte SERVER_COMMAND_INDEX = 66;
    public static final byte CLIENT_COMMAND_INDEX = 99;

    /** Server answer */
    public static final byte ANSWER_OK = 100;
    public static final byte ANSWER_CANCEL = 101;
}
