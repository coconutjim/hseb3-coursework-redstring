package gui.container.image;

import java.awt.image.BufferedImage;
import java.util.Objects;

/***
 * Represents a class that holds info about image and its scale
 */
public class BufferedImageModel {
    
    /** Image itself */
    private BufferedImage image;
    
    /** Image scale */
    private int scale;
    
    /** Horizontal offset */
    private int hOffset;
    
    /** Vertical offset */
    private int vOffset;

    /***
     * Constructor
     * @param image image itself
     * @param scale image scale
     * @param hOffset horizontal image offset
     * @param vOffset vertical image offset
     */
    public BufferedImageModel(BufferedImage image, int scale, 
            int hOffset, int vOffset) {
        if (image == null) {
            throw new IllegalArgumentException("BufferedImageModel:"
                    + "image is null!");
        }
        this.image = image;
        this.scale = scale;
        this.hOffset = hOffset;
        this.vOffset = vOffset;
    }
    
    public void setImage(BufferedImage image, int scale) {
        if (image == null) {
            throw new IllegalArgumentException("BufferedImageModel:"
                    + "image is null!");
        }
        this.image = image;
        this.scale = scale;
    }

    public void setImage(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("BufferedImageModel:"
                    + "image is null!");
        }
        this.image = image;
        this.scale = 100;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public void sethOffset(int hOffset) {
        this.hOffset = hOffset;
    }

    public void setvOffset(int vOffset) {
        this.vOffset = vOffset;
    }
    
    public BufferedImage getImage() {
        return image;
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
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.image);
        hash = 97 * hash + this.scale;
        hash = 97 * hash + this.hOffset;
        hash = 97 * hash + this.vOffset;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BufferedImageModel other = (BufferedImageModel) obj;
        if (!Objects.equals(this.image, other.image)) {
            return false;
        }
        if (this.scale != other.scale) {
            return false;
        }
        if (this.vOffset != other.vOffset) {
            return false;
        }
        return true;
    }
}
