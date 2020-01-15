package rslib.gui.style;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents a color model
 */
public class ColorModel implements Externalizable {

    /** Combined color values */
    private int rgba;

    /***
     * Constructor
     * @param rgba combined color values
     */
    public ColorModel(int rgba) {
        this.rgba = rgba;
    }

    /***
     * Constructor for externalization
     */
    public ColorModel() {
    }

    /***
     * Sets the color opaque or transparent
     * @param opaque opaque or transparent
     */
    public void setOpaque(boolean opaque) {
        if (opaque) {
            rgba = rgba | 0xff000000;
        }
        else {
            rgba = rgba & 0x00ffffff;
        }
    }

    public int getRgba() {
        return rgba;
    }

    public boolean isOpaque() {
        return (rgba | 0x00ffffff) != 0x00ffffff;
    }

    @Override
    public String toString() {
        return "ColorModel{" +
                "rgba=" + rgba +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColorModel)) return false;

        ColorModel that = (ColorModel) o;
        if (rgba != that.rgba) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return rgba;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(rgba);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        rgba = in.readInt();
    }
}