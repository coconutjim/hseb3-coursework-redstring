package gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.miginfocom.swing.MigLayout;
import rslib.cs.common.ConnectConfiguration;

import java.awt.*;

/***
* Represents a host chooser dialog, singleton
*/
public class HostChooser {

    /** Text field with host */
    private final JTextField textField;

    /** Check box with remote host */
    private final JCheckBox localServerHostCB;

    /** Dialog panel */
    private final JPanel panel;

    /** Link to instance */
    private static HostChooser instance;

    /***
     * Returns a singleton
     * @return singleton instance
     */
    public static HostChooser getInstance() {
        if (instance == null) {
            instance = new HostChooser();
        }
        return instance;
    }

    /***
     * Private constructor
     */
    private HostChooser() {
        textField = new JTextField();
        textField.setPreferredSize(new Dimension(110, textField.getFont().getSize()));
        localServerHostCB = new JCheckBox("Local server");
        localServerHostCB.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                textField.setEnabled(localServerHostCB.isSelected());
            }
        });
        localServerHostCB.setSelected(false);
        textField.setEnabled(false);
        panel = new JPanel();
        panel.setLayout(new MigLayout("", "center", "center"));
        panel.add(new JLabel("Enter a new host:"), "span");
        panel.add(textField, "span");
        panel.add(localServerHostCB);
    }

    /***
     * Shows a dialog with host choosing
     * @param parent parent component
     * @param host old host
     * @return new host
     */
    public String showDialog(Component parent, String host) {
        if (host != null && host.equals(ConnectConfiguration.REMOTE_HOST)) {
            localServerHostCB.setSelected(false);
        }
        else {
            localServerHostCB.setSelected(true);
            textField.setText(host);
        }
        int result = JOptionPane.showConfirmDialog(parent, panel,
                "Choose new host", JOptionPane.OK_CANCEL_OPTION);
        String newHost = null;
        if (result == JOptionPane.OK_OPTION) {
            if (localServerHostCB.isSelected()) {
                newHost = textField.getText();
            }
            else {
                newHost = ConnectConfiguration.REMOTE_HOST;
            }
        }
        return newHost;
    }
}
