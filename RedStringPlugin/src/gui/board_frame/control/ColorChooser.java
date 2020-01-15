package gui.board_frame.control;

import gui.util.ColorScheme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/***
 * Represents a color chooser dialog, singleton
 */
public class ColorChooser {
    
    /** Colored foreground label */
    private final JLabel foregroundLabel;
    
    /** Colored background label */
    private final JLabel backgroundLabel;
    
    /** None selection */
    private final String none;
    
    /** Schemes combo box */
    private final JComboBox<String> schemesCB;
    
    /** Dialog panel */
    private final JPanel panel;
    
    /** Link to instance */
    private static ColorChooser instance;
    
    /***
     * Returns a singleton
     * @return singleton instance 
     */
    public static ColorChooser getInstance() {
        if (instance == null) {
            instance = new ColorChooser();
        }
        return instance;
    }

    /***
     * Private constructor
     */
    private ColorChooser() {
        foregroundLabel = new JLabel();
        foregroundLabel.setPreferredSize(new Dimension(30, 20));
        foregroundLabel.setOpaque(true);
        backgroundLabel = new JLabel();
        backgroundLabel.setPreferredSize(new Dimension(30, 20));
        backgroundLabel.setOpaque(true);
        final JButton foregroundColorButton = new JButton("Change");
        foregroundColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color result = JColorChooser.showDialog(foregroundColorButton, 
                        "Choose color", foregroundColorButton.getBackground());
                if (result != null) {
                    foregroundLabel.setBackground(result);
                }
            }
        });
        final JButton backgroundColorButton = new JButton("Change");
        backgroundColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color result = JColorChooser.showDialog(backgroundColorButton, 
                        "Choose color", backgroundColorButton.getBackground());
                if (result != null) {
                    backgroundLabel.setBackground(result);
                }
            }
        });
        Set<String> schemesNames = ColorScheme.COLOR_SCHEMES.keySet();
        String[] names = new String[schemesNames.size() + 1];
        none = "None";
        names[0] = none;
        int i = 1;
        for (String name : schemesNames) {
            names[i ++] = name;
        }
        schemesCB = new JComboBox<>(names);
        schemesCB.setPreferredSize(new Dimension(50, 20));
        schemesCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = (String) schemesCB.getSelectedItem();
                if (name != null && ! name.equals(none)) {
                    ColorScheme scheme = ColorScheme.COLOR_SCHEMES.get(name);
                    if (scheme != null) {
                        foregroundLabel.setBackground(scheme.getForeground());
                        backgroundLabel.setBackground(scheme.getBackground());
                    }
                }
            }
        });
        panel = new JPanel();
        panel.setLayout(new MigLayout("", "center", "center"));
        panel.add(new JLabel("Default color schemes:"), "span");
        panel.add(schemesCB, "span");
        panel.add(new JLabel("Foreground:"));
        panel.add(new JLabel("Background:"), "wrap");
        panel.add(foregroundLabel);
        panel.add(backgroundLabel, "wrap");
        panel.add(foregroundColorButton);
        panel.add(backgroundColorButton);
    }
    
    /***
     * Shows a dialog with color scheme choosing
     * @param parent parent component
     * @param foreground old foreground color
     * @param background old background color
     * @return new color scheme
     */
    public ColorScheme showDialog(Component parent, 
            Color foreground, Color background) {
        foregroundLabel.setBackground(foreground);
        backgroundLabel.setBackground(background);
        schemesCB.setSelectedItem(none);
        int result = JOptionPane.showConfirmDialog(parent, panel,
                "Choose new color scheme", JOptionPane.OK_CANCEL_OPTION);
        ColorScheme newScheme = null;
        if (result == JOptionPane.OK_OPTION) {
            newScheme = new ColorScheme(foregroundLabel.getBackground(), 
                    backgroundLabel.getBackground());
        }
        return newScheme;
    }
}