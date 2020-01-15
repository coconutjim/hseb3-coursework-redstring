package rslib.cs.protocol.events;

import rslib.cs.protocol.RedStringInfo;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents an answer from server
 */
public class AnswerCommand implements RedStringInfo {

    /** For better parsing */
    public static final long serialVersionUID = 72525627224422L;

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

    /***
     * Constructor for externalization
     */
    public AnswerCommand() {
    }

    public byte getAnswer() {
        return answer;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeByte(answer);
        out.writeUTF(message);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        answer = in.readByte();
        message = in.readUTF();
    }
}
