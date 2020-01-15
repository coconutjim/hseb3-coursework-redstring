package rslib.cs.protocol.events.board.container.text;

import rslib.cs.protocol.events.board.common.ComponentEvent;
import rslib.gui.container.text.TextContainer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents set text event
 */
public class ChangeTextEvent extends ComponentEvent {

    /** Text */
    private String text;

    /***
     * Constructor
     * @param hash board hash
     * @param id container id
     * @param text text
     */
    public ChangeTextEvent(int hash, int id, String text) {
        super(hash, id);
        this.text = text;
    }

    /***
     * Constructor for externalization
     */
    public ChangeTextEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CHANGE_TEXT_E;
    }

    public String getText() {
        return text;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        if (text == null || text.length() == 0) {
            out.writeInt(0);
        }
        else {
            out.writeInt(text.length());
            out.writeUTF(text);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        if (in.readInt() != 0) {
            text = in.readUTF();
        }
    }
}
