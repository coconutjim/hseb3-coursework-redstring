package rslib.gui.container;

import rslib.cs.common.Status;
import rslib.gui.ExternalizableComponent;
import rslib.gui.style.ColorModel;
import rslib.gui.style.FontModel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/***
 * Info about board container that can be serialized
 */
public abstract class ExternalizableContainer extends ExternalizableComponent implements BoardContainer {

    /** For better parsing */
    public static final long serialVersionUID = 25781656255126223L;

    /** Container layer */
    protected int layer;

    /** Container type */
    protected BoardContainer.ContainerType type;

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
     * @param layer container layer
     * @param type container type
     * @param blocked component blocked status
     * @param blockOwner block owner
     */
    public ExternalizableContainer(String owner, Status status, int left, int top, int width,
                                   int height,
                                   int minimumWidth, int minimumHeight, int maximumWidth, int maximumHeight,
                                   String name, int id, FontModel font, boolean opaque,
                                   ColorModel foreground, ColorModel background, int layer,
                                   BoardContainer.ContainerType type, boolean blocked, String blockOwner) {
        super(owner, status, left, top, width, height,
                minimumWidth, minimumHeight,  maximumWidth, maximumHeight, name, id, font, opaque, foreground,
                background,
                blocked, blockOwner);
        this.layer = layer;
        this.type = type;
        this.blocked = blocked;
        this.blockOwner = blockOwner;
    }

    /***
     * Constructor for externalization
     */
    public ExternalizableContainer() {
    }

    @Override
    public BoardContainer.ContainerType getType() {
        return type;
    }

    @Override
    public void setLayer(int layer) {
        this.layer = layer;
    }

    @Override
    public int getLayer() {
        return layer;
    }

    @Override
    public String toString() {
        return super.toString() +
                "layer=" + layer + '\n' +
                ", type=" + type + '\n' +
                ',';
    }

    @Override
    public boolean equals(Object o) {
        if (! super.equals(o)) {
            return false;
        }
        if (this == o) return true;
        if (!(o instanceof ExternalizableContainer)) return false;

        ExternalizableContainer container = (ExternalizableContainer) o;

        if (layer != container.layer) return false;
        if (type != container.type) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + layer;
        result = 31 * result + (type != null ? type.toString().hashCode() : 0);
        return result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(layer);
        out.writeObject(type);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        layer = in.readInt();
        type = (BoardContainer.ContainerType) in.readObject();
    }
}
