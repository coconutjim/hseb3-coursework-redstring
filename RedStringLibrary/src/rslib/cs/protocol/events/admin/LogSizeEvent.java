package rslib.cs.protocol.events.admin;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents log size events
 */
public class LogSizeEvent extends AdminEvent {

    /** For better parsing */
    public static final long serialVersionUID = 6544545457892L;

    /** File length */
    private long length;

    /***
     * Constructor
     * @param length file length
     */
    public LogSizeEvent(long length) {
        this.length = length;
    }

    /***
     * Constructor for externalization
     */
    public LogSizeEvent() {
    }

    public long getLength() {
        return length;
    }

    @Override
    public AdminEvent.AdminEventType getIndex() {
        return AdminEventType.LOG_SIZE_E;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(length);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        length = in.readLong();
    }
}
