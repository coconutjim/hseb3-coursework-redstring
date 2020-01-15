package gui.container.image;

import gui.board_frame.control.PaintChooser;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/***
 * Represents a pop up menu with paint settings
 */
public class PaintSettingsPopupMenu extends JPopupMenu {
    
    /** Link to paint panel */
    private PaintPanel paintPanel;

    /***
     * Constructor
     * @param paintPanel link to paint panel 
     */
    public PaintSettingsPopupMenu(PaintPanel paintPanel) {
        if (paintPanel == null) {
            throw new IllegalArgumentException("PaintSettingsPopupMenu:"
                    + "paintPanel is null!");
        }
        this.paintPanel = paintPanel;
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        // Paint settings
        JMenuItem setPaintSettingsItem = new JMenuItem("Configure paint settings");
        setPaintSettingsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {   
                PaintSettings result = PaintChooser.getInstance().
                        showDialog(paintPanel, paintPanel.getPaintSettings());
                if (result != null) {
                    paintPanel.setPaintSettings(result);
                }
            }
        });
        add(setPaintSettingsItem);
        
        // Fill image
        JMenuItem fillItem = new JMenuItem("Fill image");
        fillItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {     
                Color result = JColorChooser.showDialog(paintPanel, 
                        "Choose color", Color.BLACK);
                if (result != null) {
                    paintPanel.fillImage(result);
                }
            }
        });
        add(fillItem);
        
        // Set image transparent
        JMenuItem setTransparentItem = new JMenuItem("Set transparent");
        setTransparentItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {     
                paintPanel.fillImage(new Color(0, 0, 0, 0));
            }
        });
        add(setTransparentItem);
        
        // Recreate image
        JMenuItem recreateItem = new JMenuItem("Recreate image");
        recreateItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {     
                paintPanel.setTransparentImage();
            }
        });
        add(recreateItem);
    }
    
    /***
     * Creates pop up menu
     * @return pop up menu
     */
    public MouseAdapter createPopUpMenu() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                pop(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                pop(e);
            }

            private void pop(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3 
                        && e.isPopupTrigger()) {
                    show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
    }   
}
