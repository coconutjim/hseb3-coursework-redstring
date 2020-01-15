package rslib.gui.container.text;

import rslib.cs.common.Status;
import rslib.gui.container.ExternalizableContainer;
import rslib.gui.style.ColorModel;
import rslib.gui.style.FontModel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Info about text container that can be serialized
 */
public class ExternalizableTextContainer extends ExternalizableContainer implements TextContainer {

    /** For better parsing */
    public static final long serialVersionUID = 4467919654782L;

    /** Container text */
    protected String text;

    /***
     * Constructor
     * @param owner component owner username
     * @param status component status
     * @param left component left
     * @param top component top
     * @param width component width
     * @param height component height
     * @param minimumWidth component minimum width
     * @param minimumHeight component minimum height
     * @param maximumWidth component maximum width
     * @param maximumHeight component maximum height
     * @param name component name
     * @param id component id
     * @param font component font
     * @param opaque component opaque
     * @param foreground component foreground color
     * @param background component background color
     * @param layer component layer
     * @param type container type
     * @param blocked component blocked status
     * @param blockOwner block owner
     * @param text container text
     */
    public ExternalizableTextContainer(String owner, Status status, int left, int top,
                                       int width, int height,
                                       int minimumWidth, int minimumHeight, int maximumWidth, int maximumHeight,
                                       String name, int id,
                                       FontModel font, boolean opaque, ColorModel foreground, ColorModel background,
                                       int layer, ContainerType type, boolean blocked, String blockOwner,
                                       String text) {
        super(owner, status, left, top, width, height,
                minimumWidth, minimumHeight,  maximumWidth, maximumHeight, name, id, font, opaque,
                foreground, background, layer, type, blocked, blockOwner);
        if (text == null) {
            throw new IllegalArgumentException("SerializableTextContainer: "
                    + "text is null!");
        }
        this.text = text;
    }

    /***
     * Constructor for externalization
     */
    public ExternalizableTextContainer() {
    }

    @Override
    public void setContent(ExternalizableContainer serializableContainer) {
        text = ((ExternalizableTextContainer) serializableContainer).getText();
    }

    @Override
    public void clearContainer() {
        text = "";
    }

    @Override
    public void appendText(String text) {
        this.text += text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public ExternalizableContainer toExternalizable() {
        return this;
    }

    @Override
    public String toString() {
        return "TextContainer{" +
                super.toString() +
                "text='" + text +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExternalizableTextContainer)) return false;
        if (!super.equals(o)) return false;

        ExternalizableTextContainer that = (ExternalizableTextContainer) o;

        if (text != null ? !text.equals(that.text) : that.text != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (blocked) {
            return 0;
        }
        int result = super.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
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
