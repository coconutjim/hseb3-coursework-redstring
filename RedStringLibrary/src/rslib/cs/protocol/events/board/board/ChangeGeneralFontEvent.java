package rslib.cs.protocol.events.board.board;

import rslib.cs.protocol.events.board.BoardEvent;
import rslib.gui.style.FontModel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents change general container font event
 */
public class ChangeGeneralFontEvent extends BoardEvent {

    /** For better parsing */
    public static final long serialVersionUID = 836274902009926L;

    /** New font */
    private FontModel font;

    /***
     * Constructor
     * @param hash board hash
     * @param font new font
     */
    public ChangeGeneralFontEvent(int hash, FontModel font) {
        super(hash);
        if (font == null) {
            throw new IllegalArgumentException("ChangeGeneralFontEvent: font is null!");
        }
        this.font = font;
    }

    /***
     * Constructor for externalization
     */
    public ChangeGeneralFontEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CHANGE_GENERAL_FONT_E;
    }

    public FontModel getFont() {
        return font;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(font);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        font = (FontModel) in.readObject();
    }
}
