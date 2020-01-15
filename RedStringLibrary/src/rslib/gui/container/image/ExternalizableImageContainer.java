package rslib.gui.container.image;

import rslib.cs.common.Status;
import rslib.gui.container.ExternalizableContainer;
import rslib.gui.style.ColorModel;
import rslib.gui.style.FontModel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Info about image container that can be serialized
 */
public class ExternalizableImageContainer extends ExternalizableContainer implements ImageContainer {

    /** For better parsing */
    public static final long serialVersionUID = 719763334441L;

    /** Container image */
    protected ImageModel image;

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
     * @param image container image
     */
    public ExternalizableImageContainer(String owner, Status status, int left, int top,
                                        int width, int height,
                                        int minimumWidth, int minimumHeight, int maximumWidth, int maximumHeight,
                                        String name, int id,
                                        FontModel font, boolean opaque, ColorModel foreground, ColorModel background,
                                        int layer, ContainerType type, boolean blocked, String blockOwner,
                                        ImageModel image) {
        super(owner, status, left, top, width, height,
                minimumWidth, minimumHeight, maximumWidth, maximumHeight, name, id, font, opaque,
                foreground, background, layer, type, blocked, blockOwner);
        this.image = image;
    }

    /***
     * Constructor for externalization
     */
    public ExternalizableImageContainer() {
    }

    @Override
    public void setContent(ExternalizableContainer serializableContainer) {
        image = ((ExternalizableImageContainer) serializableContainer).getImage();
    }

    @Override
    public void clearContainer() {
        image = null;
    }

    @Override
    public void setImage(ImageModel image) {
        this.image = image;
    }

    @Override
    public ImageModel getImage() {
        return image;
    }

    @Override
    public ExternalizableContainer toExternalizable() {
        return this;
    }

    @Override
    public String toString() {
        return "ImageContainer{" +
                super.toString() +
                "image='" + (image == null? "no image" : image.toString()) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExternalizableImageContainer)) return false;
        if (!super.equals(o)) return false;

        ExternalizableImageContainer that = (ExternalizableImageContainer) o;

        if (image != null ? !image.equals(that.image) : that.image != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (blocked) {
            return 0;
        }
        int result = super.hashCode();
        result = 31 * result + (image != null ? image.hashCode() : 0);
        return result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(image);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        image = (ImageModel) in.readObject();
    }
}
