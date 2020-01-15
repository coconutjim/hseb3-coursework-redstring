package client_server.protocol.command_to_client.from_node.from_server;

import client_server.protocol.ChatInfo;

/***
 * Represents an answer from server
 */
public class AnswerCommand implements ChatInfo {

    /** Server answer */
    private byte answer;

    /** Notification message */
    private String message;

    /***
     * Constructor
     * @param answer server answer
     * @param message message itself
     */
    public AnswerCommand(byte answer, String message) {
        if (message == null) {
            throw new NullPointerException("NotificationCommand: message is null");
        }
        this.answer = answer;
        this.message = message;
    }

    public byte getAnswer() {
        return answer;
    }

    public String getMessage() {
        return message;
    }
}
