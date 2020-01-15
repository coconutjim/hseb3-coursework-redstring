package gui.parsing;

import gui.container.image.BufferedImageModel;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import rslib.gui.container.file.FileModel;
import rslib.gui.container.image.ImageModel;
import rslib.gui.style.ColorModel;
import rslib.gui.style.FontModel;

/**
 * Represents converting methods
 */
public final class Parsing {
    
    /***
     * Creates awt.Font instance from board font model instance
     * @param fontModel font model instance
     * @return awt.Font instance
     */
    public static Font createFont(FontModel fontModel) {
        if (fontModel == null) {
            return null;
        }
        return new Font(fontModel.getName(), fontModel.getStyle(),
                fontModel.getSize());
    }
    
    /***
     * Converts awt.Font instance to board font model instance
     * @param font awt.Font instance
     * @return board font model instance
     */
    public static FontModel convertToFontModel(Font font) {
        if (font == null) {
            return null;
        }
        return new FontModel(font.getName(), font.getStyle(), font.getSize());
    }
    
    /***
     * Creates awt.Color instance from board color model instance
     * @param colorModel color model instance
     * @return awt.Color instance
     */
    public static Color createColor(ColorModel colorModel) {
        if (colorModel == null) {
            return null;
        }
        return new Color(colorModel.getRgba(), true);
    }
    
    /***
     * Converts awt.Color instance to board color model instance
     * @param color awt.Color instance
     * @return board color model instance
     */
    public static ColorModel convertToColorModel(Color color) {
        if (color == null) {
            return null;
        }
        return new ColorModel(color.getRGB());
    }
    
    /***
     * Creates awt.BuferredImage instance from board image model instance
     * @param imageModel image model instance
     * @return awt.BuferredImage instance
     */
    public static BufferedImageModel createImage(ImageModel imageModel) {
        if (imageModel == null) {
            return null;
        }
        BufferedImage bi = new BufferedImage(imageModel.getWidth(),
                imageModel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        bi.setRGB(0, 0, imageModel.getWidth(), imageModel.getHeight(), 
                imageModel.getPixels(), 0, imageModel.getWidth());
        return new BufferedImageModel(bi, imageModel.getScale(),
            imageModel.gethOffset(), imageModel.getvOffset());
    }
    
    /***
     * Converts awt.BuferredImage instance to board image model instance
     * @param bim awt.BuferredImage instance
     * @param scale image scale
     * @return board image model instance
     */
    public static ImageModel convertToImageModel(BufferedImageModel bim) {
        if (bim == null) {
            return null;
        }
        BufferedImage image = bim.getImage();
        int width = bim.getImage().getWidth();
        int height = bim.getImage().getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        return new ImageModel(width, height, pixels, bim.getScale(), 
                bim.gethOffset(), bim.getvOffset());
    }
    
    /***
     * Creates io.File instance from board file model instance
     * @param fileModel file model instance
     * @return io.File instance
     */
    /*public static File createFile(FileModel fileModel) {
        if (fileModel == null) {
            return null;
        }
        return null;
    }*/
    
    /***
     * Converts io.File instance to board file model instance
     * @param file io.File instance
     * @return board file model instance
     */
    public static FileModel convertToFileModel(File file) {
        if (file == null) {
            return null;
        }
        Path path = file.toPath();
        byte[] array;
        try {
            array = Files.readAllBytes(path);
        }
        catch (IOException | OutOfMemoryError e) {
            return null;
        }
        FileModel fileModel = new FileModel(file.getName(), array);
        return fileModel; 
    }
}
