package rslib.gui;

import rslib.cs.common.Status;
import rslib.gui.style.ColorModel;
import rslib.gui.style.FontModel;

import java.io.*;

/***
 * Info about component that can be serialized
 */
public abstract class ExternalizableComponent implements Externalizable, BasicComponent {

    /** For better parsing */
    public static final long serialVersionUID = 362767625726726256L;

    /** Component owner */
    protected String owner;

    /** Component status */
    protected Status status;

    /** Component left */
    protected int left;

    /** Component top */
    protected int top;

    /** Component width */
    protected int width;

    /** Component height */
    protected int height;

    /** Component minimum width */
    protected int minimumWidth;

    /** Component minimum height */
    protected int minimumHeight;

    /** Component maximum width */
    protected int maximumWidth;

    /** Component maximum height */
    protected int maximumHeight;

    /** Component name */
    protected String name;

    /** Component id */
    protected int id;

    /** Component font */
    protected FontModel font;

    /** Component opaque */
    protected boolean opaque;

    /** Component foreground color */
    protected ColorModel foreground;

    /** Component background color */
    protected ColorModel background;

    /** Component blocked */
    protected boolean blocked;

    /** Block owner */
    protected String blockOwner;

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
     * @param blocked component blocked status
     * @param blockOwner block owner
     */
    public ExternalizableComponent(String owner, Status status, int left, int top, int width,
                                   int height, int minimumWidth, int minimumHeight, int maximumWidth, int maximumHeight,
                                   String name, int id, FontModel font, boolean opaque,
                                   ColorModel foreground, ColorModel background,
                                   boolean blocked, String blockOwner) {
        if (owner == null) {
            throw new IllegalArgumentException("SerializableContainer: "
                    + "owner is null!");
        }
        if (name == null) {
            throw new IllegalArgumentException("SerializableContainer: "
                    + "name is null!");
        }
        if (font == null) {
            throw new IllegalArgumentException("SerializableContainer: "
                    + "font is null!");
        }
        if (foreground == null) {
            throw new IllegalArgumentException("SerializableContainer: "
                    + "foreground is null!");
        }
        if (background == null) {
            throw new IllegalArgumentException("SerializableContainer: "
                    + "background is null!");
        }
        this.owner = owner;
        this.status = status;
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.minimumWidth = minimumWidth;
        this.minimumHeight = minimumHeight;
        this.maximumWidth = maximumWidth;
        this.maximumHeight = maximumHeight;
        this.name = name;
        this.id = id;
        this.font = font;
        this.opaque = opaque;
        this.foreground = foreground;
        this.background = background;
        this.blocked = blocked;
        this.blockOwner = blockOwner;
    }

    /***
     * Constructor for externalization
     */
    public ExternalizableComponent() {
    }

    @Override
    public void setComponentOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getComponentOwner() {
        return owner;
    }

    @Override
    public void setComponentStatus(Status status) {
        this.status = status;
    }

    @Override
    public Status getComponentStatus() {
        return status;
    }

    @Override
    public void moveComponent(int left, int top) {
        this.left = left;
        this.top = top;
    }

    @Override
    public int getComponentLeft() {
        return left;
    }

    @Override
    public int getComponentTop() {
        return top;
    }

    @Override
    public void resizeComponent(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getComponentWidth() {
        return width;
    }

    @Override
    public int getComponentHeight() {
        return height;
    }

    @Override
    public int getComponentId() {
        return id;
    }

    @Override
    public void setComponentName(String name) {
        this.name = name;
    }

    @Override
    public String getComponentName() {
        return name;
    }

    @Override
    public void setComponentFont(FontModel font) {
        this.font = font;
    }

    @Override
    public FontModel getComponentFont() {
        return font;
    }

    @Override
    public void setComponentForeground(ColorModel color) {
        this.foreground = color;
    }

    @Override
    public ColorModel getComponentForeground() {
        return foreground;
    }

    @Override
    public void setComponentBackground(ColorModel color) {
        color.setOpaque(opaque);
        background = color;
    }

    @Override
    public ColorModel getComponentBackground() {
        return background;
    }

    @Override
    public void setComponentOpaque(boolean opaque) {
        this.opaque = opaque;
        background.setOpaque(opaque);
    }

    @Override
    public boolean isComponentOpaque() {
        return opaque;
    }

    @Override
    public int getComponentMinimumWidth() {
        return minimumWidth;
    }

    @Override
    public int getComponentMinimumHeight() {
        return minimumHeight;
    }

    @Override
    public int getComponentMaximumWidth() {
        return maximumWidth;
    }

    @Override
    public int getComponentMaximumHeight() {
        return maximumHeight;
    }

    @Override
    public void setBlocked(boolean blocked, String blockOwner) {
        this.blocked = blocked;
        if (! blocked) {
            this.blockOwner = null;
        }
        else {
            this.blockOwner = blockOwner;
        }
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }

    @Override
    public String getBlockOwner() {
        return blockOwner;
    }

    @Override
    public String toString() {
        return
                "owner='" + owner + '\'' + '\n' +
                ", status=" + status.toString() + '\n' +
                        ", left=" + left + '\n' +
                        ", top=" + top + '\n' +
                        ", width=" + width + '\n' +
                        ", height=" + height + '\n' +
                        ", minimum width=" + minimumWidth + '\n' +
                        ", minimum height=" + minimumHeight + '\n' +
                        ", maximum width=" + maximumWidth + '\n' +
                        ", maximum height=" + maximumHeight + '\n' +
                        ", name='" + name + '\'' +  '\n' +
                        ", id=" + id + '\n' +
                        ", font=" + font.toString() + '\n' +
                        ", opaque=" + opaque + '\n' +
                        ", foreground=" + foreground.toString() + '\n' +
                        ", background=" + background.toString() + '\n' +
                        ", blocked=" + blocked + '\n' +
                        ", block owner=" + blockOwner + '\n' +
                        ',';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExternalizableComponent)) return false;

        ExternalizableComponent that = (ExternalizableComponent) o;

        if (blocked != that.blocked) return false;
        if (height != that.height) return false;
        if (id != that.id) return false;
        if (left != that.left) return false;
        if (maximumHeight != that.maximumHeight) return false;
        if (maximumWidth != that.maximumWidth) return false;
        if (minimumHeight != that.minimumHeight) return false;
        if (minimumWidth != that.minimumWidth) return false;
        if (status != that.status) return false;
        if (top != that.top) return false;
        if (width != that.width) return false;
        if (opaque != that.opaque) return false;
        if (background != null ? !background.equals(that.background) : that.background != null) return false;
        if (blockOwner != null ? !blockOwner.equals(that.blockOwner) : that.blockOwner != null) return false;
        if (font != null ? !font.equals(that.font) : that.font != null) return false;
        if (foreground != null ? !foreground.equals(that.foreground) : that.foreground != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = owner != null ? owner.hashCode() : 0;
        result = 31 * result + status.ordinal();
        result = 31 * result + left;
        result = 31 * result + top;
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + id;
        result = 31 * result + (font != null ? font.hashCode() : 0);
        result = 31 * result + (opaque? 1: 0);
        result = 31 * result + (foreground != null ? foreground.hashCode() : 0);
        result = 31 * result + (background != null ? background.hashCode() : 0);
        result = 31 * result + (blocked? 1: 0);
        result = 31 * result + (blockOwner != null ? blockOwner.hashCode() : 0);
        return result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(owner);
        out.writeObject(status);
        out.writeInt(left);
        out.writeInt(top);
        out.writeInt(width);
        out.writeInt(height);
        out.writeInt(minimumWidth);
        out.writeInt(minimumHeight);
        out.writeInt(maximumWidth);
        out.writeInt(maximumHeight);
        out.writeUTF(name);
        out.writeInt(id);
        out.writeObject(font);
        out.writeBoolean(opaque);
        out.writeObject(foreground);
        out.writeObject(background);
        out.writeBoolean(blocked);
        if (blockOwner == null || blockOwner.length() == 0) {
            out.writeInt(0);
        }
        else {
            out.writeInt(blockOwner.length());
            out.writeUTF(blockOwner);
        }

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        owner = in.readUTF();
        status = (Status) in.readObject();
        left = in.readInt();
        top = in.readInt();
        width = in.readInt();
        height = in.readInt();
        minimumWidth = in.readInt();
        minimumHeight = in.readInt();
        maximumWidth = in.readInt();
        maximumHeight = in.readInt();
        name = in.readUTF();
        id = in.readInt();
        font = (FontModel) in.readObject();
        opaque = in.readBoolean();
        foreground = (ColorModel) in.readObject();
        background = (ColorModel) in.readObject();
        blocked = in.readBoolean();
        if (in.readInt() != 0) {
            blockOwner = in.readUTF();
        }
    }
}
