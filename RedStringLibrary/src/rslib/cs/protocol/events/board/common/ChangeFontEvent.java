package rslib.cs.protocol.events.board.common;

import rslib.gui.style.FontModel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents change font event
 */
public class ChangeFontEvent extends ComponentEvent {

    /** For better parsing */
    public static final long serialVersionUID = 272517723656234L;

    /** New font */
    private FontModel font;

    /***
     * Constructor
     * @param hash board hash
     * @param id component id
     * @param font new font
     */
    public ChangeFontEvent(int hash, int id, FontModel font) {
        super(hash, id);
        if (font == null) {
            throw new IllegalArgumentException("ChangeFontEvent: font is null!");
        }
        this.font = font;
    }

    /***
     * Constructor for externalization
     */
    public ChangeFontEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CHANGE_FONT_E;
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
