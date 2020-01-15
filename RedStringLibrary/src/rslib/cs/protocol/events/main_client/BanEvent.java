package rslib.cs.protocol.events.main_client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a banning event
 */
public class BanEvent extends MainClientEvent {

    /** For better parsing */
    public static final long serialVersionUID = 834610058656333L;

    @Override
    public MainClientEventType getIndex() {
        return MainClientEventType.BAN_E;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
