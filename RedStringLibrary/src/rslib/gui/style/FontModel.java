package rslib.gui.style;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a font model
 */
public class FontModel implements Externalizable {

    /** Font name */
    private String name;

    /** Font style */
    private int style;

    //TODO: mb float?
    /** Font size */
    private int size;

    /***
     * Constructor
     * @param name font name
     * @param style font style
     * @param size font size
     */
    public FontModel(String name, int style, int size) {
        if (name == null) {
            throw new IllegalArgumentException("FontModel: name is null!");
        }
        this.name = name;
        this.style = style;
        this.size = size;
    }

    /***
     * Constructor for externalization
     */
    public FontModel() {
    }

    public String getName() {
        return name;
    }

    public int getStyle() {
        return style;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "FontModel{" +
                "name='" + name + '\'' +
                ", style=" + style +
                ", size=" + size +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FontModel)) return false;

        FontModel fontModel = (FontModel) o;

        if (size != fontModel.size) return false;
        if (style != fontModel.style) return false;
        if (name != null ? !name.equals(fontModel.name) : fontModel.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + style;
        result = 31 * result + size;
        return result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(name);
        out.writeInt(style);
        out.writeInt(size);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
        style = in.readInt();
        size = in.readInt();
    }
}
