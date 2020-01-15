package rslib.cs.protocol.events.setup;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents delete board event
 */
public class DeleteBoardEvent implements SetupEvent {

    @Override
    public SetupEventType getIndex() {
        return SetupEventType.DELETE_BOARD_E;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
