package rslib.cs.protocol.events.admin;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents admin log event
 */
public class ServerLogEvent extends AdminEvent {

    /** For better parsing */
    public static final long serialVersionUID = 539567375892L;

    /** Log message */
    private String message;

    /***
     * Constructor
     * @param message log message
     */
    public ServerLogEvent(String message) {
        if (message == null) {
            throw new NullPointerException("ServerLogEvent: message is null!");
        }
        this.message = message;
    }

    /***
     * Constructor for externalization
     */
    public ServerLogEvent() {
    }

    public String getMessage() {
        return message;
    }

    @Override
    public AdminEventType getIndex() {
        return AdminEventType.SERVER_LOG_E;
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
