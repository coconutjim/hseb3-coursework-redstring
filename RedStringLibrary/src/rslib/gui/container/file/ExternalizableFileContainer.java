package rslib.gui.container.file;

import rslib.cs.common.Status;
import rslib.gui.container.ExternalizableContainer;
import rslib.gui.style.ColorModel;
import rslib.gui.style.FontModel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Info about file container that can be serialized
 */
public class ExternalizableFileContainer extends ExternalizableContainer implements FileContainer {

    /** For better parsing */
    public static final long serialVersionUID = 717768129999L;

    /** Container file */
    protected FileModel file;

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
     * @param file container file
     */
    public ExternalizableFileContainer(String owner, Status status, int left, int top,
                                       int width, int height,
                                       int minimumWidth, int minimumHeight, int maximumWidth, int maximumHeight,
                                       String name, int id,
                                       FontModel font, boolean opaque, ColorModel foreground, ColorModel background,
                                       int layer, ContainerType type, boolean blocked, String blockOwner,
                                       FileModel file) {
        super(owner, status, left, top, width, height,
                minimumWidth, minimumHeight, maximumWidth, maximumHeight, name, id, font, opaque,
                foreground, background, layer, type, blocked, blockOwner);
        this.file = file;
    }

    /***
     * Constructor for externalization
     */
    public ExternalizableFileContainer() {
    }

    @Override
    public void setContent(ExternalizableContainer serializableContainer) {
        file = ((ExternalizableFileContainer) serializableContainer).getFile();
    }

    @Override
    public void clearContainer() {
        file = null;
    }

    @Override
    public FileModel getFile() {
        return file;
    }

    @Override
    public void setFile(FileModel file) {
        this.file = file;
    }

    @Override
    public ExternalizableContainer toExternalizable() {
        return this;
    }

    @Override
    public String toString() {
        return "FileContainer{" +
                super.toString() +
                "file hash code='" + (file == null? "no file" : file.hashCode()) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExternalizableFileContainer)) return false;
        if (!super.equals(o)) return false;

        ExternalizableFileContainer that = (ExternalizableFileContainer) o;

        if (file != null ? !file.equals(that.file) : that.file != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (blocked) {
            return 0;
        }
        int result = super.hashCode();
        result = 31 * result + (file != null ? file.hashCode() : 0);
        return result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(file);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        file = (FileModel) in.readObject();
    }
}

