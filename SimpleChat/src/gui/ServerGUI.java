package gui;


import client_server.client.AdminClient;
import client_server.client.util.AdminClientConfiguration;
import client_server.protocol.request_to_node.to_lobby.InternalRequest;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Represents server's GUI as a frame
 */
public class ServerGUI extends JFrame {

    /** Log text area */
    private JTextArea textArea;

    /** Field for typing */
    private JTextField typeField;

    /** Admin client */
    private AdminClient client;

    /** General user name */
    private String generalName;

    /** Info label */
    private JLabel infoLabel;

    /***
     * Constructor
     */
    public ServerGUI() {
        generalName = "Admin";
        initComponents();
    }

    /***
     * Initializes GUI components
     */
    private void initComponents() {

        // General params
        setTitle("Server");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLayout(new MigLayout("", "center", "center"));

        // Test area
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane pane = new JScrollPane(textArea);
        pane.setPreferredSize(new Dimension(600, 350));
        add(pane, "wrap");

        // Type panel
        JPanel typePanel = new JPanel();
        typePanel.setLayout(new MigLayout("", "center", "center"));

        // Field for typing
        typeField = new JTextField();
        typeField.setPreferredSize(new Dimension(300, 20));
        typePanel.add(typeField, "wrap");
        typeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendCommand();
                }
            }
        });

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new MigLayout("", "center", "center"));

        // Button for connecting to API
        JButton buttonConnect = new JButton("Connect to API");
        buttonConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });
        buttonPanel.add(buttonConnect);

        // Button for message sending
        JButton buttonSend = new JButton("Send");
        buttonSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendCommand();
            }
        });
        buttonPanel.add(buttonSend);

        // Button for chat cleaning
        JButton buttonClear = new JButton("Clear");
        buttonClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        });
        buttonPanel.add(buttonClear);

        typePanel.add(buttonPanel);

        add(typePanel, "wrap");

        // Info label
        infoLabel = new JLabel("General username: " + generalName);
        add(infoLabel);

        // Constructing menu bar
        setJMenuBar(createMenu());


        pack();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dimension.width / 2 - getSize().width / 2, dimension.height / 2 - getSize().height / 2);
        setVisible(true);

    }

    /**
     * Constructs menu Bar;
     */
    private JMenuBar createMenu() {

        final JMenuBar menuBar = new JMenuBar();

        final JMenu menuSettings = new JMenu("Settings");
        final JMenuItem menuItemChangeName = new JMenuItem("Change general username");
        menuItemChangeName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = JOptionPane.showInputDialog(null, "Enter new username:");
                if (username != null && ! username.equals("")) {
                    //TODO: more corrections
                    setGeneralName(username);
                }
            }
        });
        menuSettings.add(menuItemChangeName);

        menuBar.add(menuSettings);

        JLabel buttonAbout = new JLabel("About");
        buttonAbout.setOpaque(false);
        buttonAbout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, "Server API");
            }
        });
        menuBar.add(buttonAbout);

        return menuBar;

    }

    /***
     * Sends internal command to server
     */
    private void sendCommand() {
        if (client == null) {
            showMessageToUser("You are not connected to server!");
            return;
        }
        client.addCommand(new InternalRequest(typeField.getText()));
        typeField.setText("");
    }

    /***
     * Connecting to server API
     */
    private void connectToServer() {
        if (client != null) {
            client.disconnect("");
        }
        String password = JOptionPane.showInputDialog(null, "Enter password:");
        if (password == null) {
            return;
        }
        //TODO: remove hardcode
        client = AdminClient.login(this, generalName, password);
    }

    /***
     * Disconnect gui actions
     * @param message message to user
     */
    public void disconnectGUIActions(String message) {
        if (message != null) {
            if (! message.equals("")) {
                JOptionPane.showMessageDialog(null, message, "Disconnection", JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            JOptionPane.showMessageDialog(null, "Disconnection by server!", "Disconnection", JOptionPane.ERROR_MESSAGE);
        }
        client = null;
    }

    /***
     * Logs the message
     * @param message log message
     */
    public void log(String message) {
        textArea.append(message + "\n");
    }

    /***
     * Shows message to user
     * @param message the message
     */
    public void showMessageToUser(String message) {
        JOptionPane.showMessageDialog(null, message, "Network error", JOptionPane.ERROR_MESSAGE);
    }

    /***
     * Reads config file and gets essential info
     * @throws IllegalArgumentException if data is not correct
     */
    private static void readConfig() throws IllegalArgumentException {

        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader("admin.properties"));
            boolean readPort = false;
            boolean readHost = false;
            while ((line = br.readLine()) != null && ! line.equals("")) {
                String[] result = line.split(" ");
                if (result[0].equals("AdminServerPort:") && result.length == 2) {
                    AdminClientConfiguration.setPort(Integer.parseInt(result[1]));
                    readPort = true;
                }
                if (result[0].equals("ServerHost:") && result.length == 2) {
                    AdminClientConfiguration.setHost(result[1]);
                    readHost = true;
                }
            }
            if (! (readHost && readPort)) {
                throw new IllegalArgumentException("Lack of data!");
            }
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Problems reading the file!");
        }
        finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e1) {
                // ????????
            }
        }
    }

    public static void main(String[] args) {
        try {
            readConfig();
        }
        catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Fatal error: Could not " +
                    "read configuration file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (UnsupportedLookAndFeelException e) {
            // handle exception
        }
        catch (ClassNotFoundException e) {
            // handle exception
        }
        catch (InstantiationException e) {
            // handle exception
        }
        catch (IllegalAccessException e) {
            // handle exception
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ServerGUI();
            }
        });
    }

    public void setGeneralName(String generalName) {
        this.generalName = generalName;
        infoLabel.setText("General username: " + generalName);
    }
}
