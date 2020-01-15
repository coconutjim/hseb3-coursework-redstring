package gui.board_frame.control;

import gui.container.image.PaintSettings;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/***
 * Represents a paint chooser dialog, singleton
 */
public class PaintChooser {
    
    /** Colored label */
    private final JLabel label;
    
    /** Thickness */
    private final JComboBox<Integer> thicknessCB;
    
    /** Dialog panel */
    private final JPanel panel;
    
    /** Link to instance */
    private static PaintChooser instance;
    
    /***
     * Returns a singleton
     * @return singleton instance 
     */
    public static PaintChooser getInstance() {
        if (instance == null) {
            instance = new PaintChooser();
        }
        return instance;
    }

    /***
     * Private constructor
     */
    private PaintChooser() {
        label = new JLabel();
        label.setPreferredSize(new Dimension(30, 20));
        label.setOpaque(true);
        int TH_MIN = PaintSettings.MIN_THICKNESS;
        int TH_MAX = PaintSettings.MAX_THICKNESS;
        int TH_STEP = 1;
        Integer[] ths = 
                new Integer[(TH_MAX - TH_MIN) / TH_STEP + 1];
        for (int i = TH_MIN, index = 0; i <= TH_MAX; 
                i += TH_STEP, ++ index) {
            ths[index] = i;
        }
        final JButton colorButton = new JButton("Change");
        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color result = JColorChooser.showDialog(colorButton, 
                        "Choose color", label.getBackground());
                if (result != null) {
                    label.setBackground(result);
                }
            }
        });
        thicknessCB = new JComboBox<>(ths);
        panel = new JPanel();
        panel.setLayout(new MigLayout("", "center", "center"));
        panel.add(new JLabel("Paint color:"), "cell 0 0");
        panel.add(new JLabel("Paint thickness:"), "cell 1 0");
        panel.add(label, "cell 0 1");
        panel.add(colorButton, "cell 0 2");
        panel.add(thicknessCB, "cell 1 2");
    }
    
    /***
     * Shows a dialog with paint settings choosing
     * @param parent parent component
     * @param oldSettings current settings
     * @return new settings
     */
    public PaintSettings showDialog(Component parent, 
            PaintSettings oldSettings) {
        label.setBackground(oldSettings.getColor());
        thicknessCB.setSelectedItem(oldSettings.getThickness());
        PaintSettings settings = null;
        int result = JOptionPane.showConfirmDialog(parent, panel,
                "Choose new paint settings", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            settings = new PaintSettings(
                    (Integer) thicknessCB.getSelectedItem(),
                    label.getBackground());
        }
        return settings;
    }
}
