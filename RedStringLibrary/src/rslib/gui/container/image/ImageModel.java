package rslib.gui.container.image;

import java.io.*;
import java.util.Arrays;

/***
 * Represents an image model
 */
public class ImageModel implements Externalizable {

    /** Image width */
    private int width;

    /** Image height */
    private int height;

    /** Image pixels */
    private int[] pixels;

    /** Scale */
    private int scale;

    /** Horizontal offset */
    private int hOffset;

    /** Vertical offset */
    private int vOffset;

    /***
     * Constructor
     * @param width image width
     * @param height image height
     * @param pixels image pixels
     * @param scale image scale
     * @param hOffset horizontal offset of image
     * @param vOffset vertical offset of image
     */
    public ImageModel(int width, int height, int[] pixels, int scale, int hOffset, int vOffset) {
        if (pixels == null) {
            throw new IllegalArgumentException("ContainerImage: pixels is null!");
        }
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.scale = scale;
        this.hOffset = hOffset;
        this.vOffset = vOffset;
    }

    /***
     * Constructor for externalization
     */
    public ImageModel() {
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getPixels() {
        return pixels;
    }

    public int getScale() {
        return scale;
    }

    public int gethOffset() {
        return hOffset;
    }

    public int getvOffset() {
        return vOffset;
    }

    @Override
    public String toString() {
        return "ContainerImage{" +
                "width=" + width +
                ", height=" + height +
                ", pixels hash=" + Arrays.hashCode(pixels) +
                ", scale=" + scale +
                ", horizontal offset=" + hOffset +
                ", vertical offset=" + vOffset +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageModel)) return false;

        ImageModel that = (ImageModel) o;

        if (height != that.height) return false;
        if (width != that.width) return false;
        if (!Arrays.equals(pixels, that.pixels)) return false;
        if (scale != that.scale) return false;
        if (hOffset != that.hOffset) return false;
        if (vOffset != that.vOffset) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + (pixels != null ? Arrays.hashCode(pixels) : 0);
        result = 31 * result + scale;
        result = 31 * result + hOffset;
        result = 31 * result + vOffset;
        return result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(width);
        out.writeInt(height);
        out.writeObject(pixels);
        out.writeInt(scale);
        out.writeInt(hOffset);
        out.writeInt(vOffset);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        width = in.readInt();
        height = in.readInt();
        pixels = (int[]) in.readObject();
        scale = in.readInt();
        hOffset = in.readInt();
        vOffset = in.readInt();
    }
}
