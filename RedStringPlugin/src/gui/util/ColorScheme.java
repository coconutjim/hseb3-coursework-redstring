package gui.util;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

/***
 * Represents a color scheme
 */
public class ColorScheme {
    
    /** Default color schemes */
    public final static Map<String, ColorScheme> COLOR_SCHEMES;
    
    /** Foreground */
    private Color foreground;
    
    /** Background */
    private Color background;
    
    static {
        COLOR_SCHEMES = new LinkedHashMap<>();
        COLOR_SCHEMES.put("Text container #1", new ColorScheme(
                new Color(0x1E0339, false), new Color(0x214F70, false)));
        COLOR_SCHEMES.put("Text container #2", new ColorScheme(
                new Color(0x1A6969, false), new Color(0x273535, false)));
        COLOR_SCHEMES.put("Text container #3", new ColorScheme(
                new Color(0x513000, false), new Color(0xD39A47, false)));
        COLOR_SCHEMES.put("Image container #1", new ColorScheme(
                new Color(0x470019, false), new Color(0xB93E69, false)));
        COLOR_SCHEMES.put("Image container #2", new ColorScheme(
                new Color(0x344734, false), new Color(0x238C23, false)));
        COLOR_SCHEMES.put("Image container #3", new ColorScheme(
                new Color(0x555000, false), new Color(0xAFA72B, false)));
        COLOR_SCHEMES.put("File container #1", new ColorScheme(
                new Color(0x1E0339, false), new Color(0x4D2476, false)));
        COLOR_SCHEMES.put("File container #2", new ColorScheme(
                new Color(0x031C35, false), new Color(0x356089, false)));
        COLOR_SCHEMES.put("File container #3", new ColorScheme(
                new Color(0x553000, false), new Color(0xAF762B, false)));
        COLOR_SCHEMES.put("Board scheme #1", new ColorScheme(
                new Color(0x2C20BA, false), new Color(0x3B62BE, false)));
        COLOR_SCHEMES.put("Board scheme #2", new ColorScheme(
                new Color(0x701168, false), new Color(0xD579CD, false)));
        COLOR_SCHEMES.put("Board scheme #3", new ColorScheme(
                new Color(0x7C7C7D, false), new Color(0x191919, false)));
    }

    /***
     * Constructor
     * @param foreground foreground color
     * @param background background color
     */
    public ColorScheme(Color foreground, Color background) {
        if (foreground == null) {
            throw new IllegalArgumentException("ColorSheme: "
                    + "foreground is null!");
        }
        if (background == null) {
            throw new IllegalArgumentException("ColorSheme: "
                    + "background is null!");
        }
        this.foreground = foreground;
        this.background = background;
    }

    public Color getForeground() {
        return foreground;
    }

    public Color getBackground() {
        return background;
    }
}
