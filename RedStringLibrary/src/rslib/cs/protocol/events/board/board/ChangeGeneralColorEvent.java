package rslib.cs.protocol.events.board.board;

import rslib.cs.protocol.events.board.BoardEvent;
import rslib.gui.style.ColorModel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a change general container color event
 */
public class ChangeGeneralColorEvent extends BoardEvent {

    /** For better parsing */
    public static final long serialVersionUID = 38726876726487L;

    /** Component foreground color */
    private ColorModel foreground;

    /** Component background color */
    private ColorModel background;

    /***
     * Constructor
     * @param hash board hash
     * @param foreground new component foreground color
     * @param background new component background color
     */
    public ChangeGeneralColorEvent(int hash, ColorModel foreground, ColorModel background) {
        super(hash);
        if (foreground == null && background == null) {
            throw new IllegalArgumentException("ChangeColorEvent: both colors are null!");
        }
        this.foreground = foreground;
        this.background = background;
    }

    /***
     * Constructor for externalization
     */
    public ChangeGeneralColorEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CHANGE_GENERAL_COLOR_E;
    }

    public ColorModel getForeground() {
        return foreground;
    }

    public ColorModel getBackground() {
        return background;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        if (foreground != null) {
            out.writeInt(1);
            out.writeObject(foreground);
        }
        else {
            out.writeInt(0);
        }
        if (background != null) {
            out.writeInt(1);
            out.writeObject(background);
        }
        else {
            out.writeInt(0);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        if (in.readInt() == 1) {
            foreground = (ColorModel) in.readObject();
        }
        if (in.readInt() == 1) {
            background = (ColorModel) in.readObject();
        }
    }
}
