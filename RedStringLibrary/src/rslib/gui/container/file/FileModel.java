package rslib.gui.container.file;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

/***
 * Represents a file model
 */
public class FileModel implements Externalizable {

    /** File name */
    private String filename;

    /** File data */
    private byte[] data;

    /** Data length */
    private int length;

    /***
     * Constructor
     * @param filename file name
     * @param data file data
     */
    public FileModel(String filename, byte[] data) {
        if (filename == null) {
            throw new IllegalArgumentException("FileModel: filename is null!");
        }
        if (data == null) {
            throw new IllegalArgumentException("FileModel: data is null!");
        }
        this.filename = filename;
        this.data = data;
        length = data.length;
    }

    /***
     * Constructor for externalization
     */
    public FileModel() {
    }

    public String getFilename() {
        return filename;
    }

    public int getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "FileModel{" +
                "filename='" + filename + '\'' +
                "data length='" + length + '\'' +
                ", data hash=" + Arrays.hashCode(data) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileModel)) return false;

        FileModel fileModel = (FileModel) o;

        if (length != fileModel.length) return false;
        if (!Arrays.equals(data, fileModel.data)) return false;
        if (filename != null ? !filename.equals(fileModel.filename) : fileModel.filename != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = filename != null ? filename.hashCode() : 0;
        result = 31 * result + (data != null ? Arrays.hashCode(data) : 0);
        result = 31 * result + length;
        return result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(filename);
        out.writeInt(length);
        out.writeObject(data);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        filename = in.readUTF();
        length = in.readInt();
        data = (byte[]) in.readObject();
        /*data = new byte[length];
        int read = 0;
        boolean readAll = false;
        while (! readAll) {
            read += in.read(data, 0, length);
            if (read == length) {
                readAll = true;
            }
        }*/
    }
}
