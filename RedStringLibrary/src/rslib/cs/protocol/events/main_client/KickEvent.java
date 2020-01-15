package rslib.cs.protocol.events.main_client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a kicking event
 */
public class KickEvent extends MainClientEvent {

    /** For better parsing */
    public static final long serialVersionUID = 92222764619234L;

    @Override
    public MainClientEventType getIndex() {
        return MainClientEventType.KICK_E;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
