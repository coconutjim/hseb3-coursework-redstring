package rslib.cs.protocol.events.setup;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a close board event
 */
public class CloseBoardEvent implements SetupEvent {

    /** For better parsing */
    public static final long serialVersionUID = 972991112983278L;

    @Override
    public SetupEventType getIndex() {
        return SetupEventType.CLOSE_BOARD_E;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
