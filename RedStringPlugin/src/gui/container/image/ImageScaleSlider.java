package gui.container.image;

import gui.container.ControlSlider;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/***
 * Represents a slider that can scale the image
 */
public class ImageScaleSlider extends ControlSlider {
    
    /** Link to image panel */
    private final PaintPanel paintPanel;
    
    /** Tick labels */
    private List<JLabel> labels;

    /***
     * Constructor
     * @param container link to container
     * @param paintPanel link to panel
     */
    public ImageScaleSlider(ImageContainerPanel container, 
            PaintPanel paintPanel) {
        super(container);
        if (paintPanel == null) {
            throw new IllegalArgumentException("ScaleSlider: "
                    + "panel is null!");
        }
        this.paintPanel = paintPanel;
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        setOpaque(false);
        setMaximum(500);
        setMinimum(0);
        labels = new ArrayList<>();
        Hashtable<Integer, JComponent> table = new Hashtable<>();
        JLabel label1 = new JLabel("0");
        labels.add(label1);
        table.put(0, label1);
        for (int i = 100; i <= 500; i += 100) {
            JLabel label = new JLabel(Integer.toString(i));
            labels.add(label);
            table.put(i, label);
        }
        setLabelTable(table);
        for (JLabel label: labels) {
            label.setForeground(container.getForeground());
            label.setFont(new Font("Calibri", Font.PLAIN, 15));
        }
        setMajorTickSpacing(100);
        setPaintTicks(true);
        setPaintLabels(true);
        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                paintPanel.setScale(getValue());
                paintPanel.centerViewport();
            }
        });
    }

    @Override
    public void changeAppearance(final boolean variable) {
        super.changeAppearance(variable);
        for (JLabel label: labels) {
            label.setForeground(container.getForeground());
        }
    }   

    @Override
    protected int getSliderHeight() {
        return 51; // magic
    } 
}
