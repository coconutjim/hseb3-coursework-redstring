package gui.board_frame.control;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import rslib.gui.style.FontModel;
import net.miginfocom.swing.MigLayout;

/***
 * Represents a font chooser dialog, singleton
 */
public class FontChooser {
    
    /** Fonts chooser */
    private final JComboBox<String> namesCB;
    
    /** Styles chooser */
    private final JComboBox<String> stylesCB;
    
    /** Sizes chooser */
    private final JComboBox<Integer> sizesCB;
    
    /** Dialog panel */
    private final JPanel panel;
    
    /** Link to instance */
    private static FontChooser instance;
    
    /***
     * Returns a singleton
     * @return singleton instance 
     */
    public static FontChooser getInstance() {
        if (instance == null) {
            instance = new FontChooser();
        }
        return instance;
    }

    /***
     * Private constructor
     */
    private FontChooser() {
        String[] fonts = { "Courier New", 
            "Calibri", "Times New Roman", "Arial", "Verdana" };
        namesCB = new JComboBox<>(fonts);
        String[] styles = { "Common", "Bold", "Italic", "Bold italic" };
        stylesCB = new JComboBox<>(styles);
        int SIZES_MIN = 10;
        int SIZES_MAX = 50;
        int SIZES_STEP = 5;
        Integer[] sizes = 
                new Integer[(SIZES_MAX - SIZES_MIN) / SIZES_STEP + 1];
        for (int i = SIZES_MIN, index = 0; i <= SIZES_MAX; 
                i += SIZES_STEP, ++ index) {
            sizes[index] = i;
        }
        sizesCB = new JComboBox<>(sizes);
        
        panel = new JPanel();
        panel.setLayout(new MigLayout("", "center", "center"));
        panel.add(new JLabel("Name:"));
        panel.add(new JLabel("Style:"));
        panel.add(new JLabel("Size:"), "wrap");
        panel.add(namesCB);
        panel.add(stylesCB);
        panel.add(sizesCB);
    }
    
    /***
     * Shows a dialog with font choosing
     * @param parent parent component
     * @param oldFont current font
     * @return new font
     */
    public FontModel showDialog(Component parent, FontModel oldFont) {
        namesCB.setSelectedItem(oldFont.getName());
        stylesCB.setSelectedIndex(oldFont.getStyle());
        sizesCB.setSelectedItem(oldFont.getSize());
        FontModel font = null;
        int result = JOptionPane.showConfirmDialog(parent, panel,
                "Choose new font", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            font = new FontModel((String) namesCB.getSelectedItem(), 
            stylesCB.getSelectedIndex(), (Integer) sizesCB.getSelectedItem());
        }
        return font;
    }
}
