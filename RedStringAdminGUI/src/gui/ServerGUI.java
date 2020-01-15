package gui;

import rslib.cs.common.ConnectConfiguration;
import rslib.cs.client.admin.AdminClient;
import rslib.cs.server.admin.AdminLobby;
import rslib.cs.protocol.events.admin.AdminEvent;
import rslib.cs.protocol.events.admin.LogFileEvent;
import rslib.cs.protocol.events.admin.LogSizeEvent;
import rslib.cs.protocol.events.admin.ServerLogEvent;
import rslib.cs.protocol.events.message.ShowMessageEvent;
import rslib.cs.protocol.requests.to_lobby.admin.InternalRequest;
import rslib.listeners.AdminListener;
import rslib.listeners.DisconnectListener;
import rslib.listeners.MessageListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Represents server's GUI as a frame
 */
public class ServerGUI extends JFrame implements AdminListener, MessageListener, DisconnectListener {

    /** Server host */
    private String host;

    /** Log text area */
    private JTextArea textArea;

    /** Field for typing */
    private JTextField typeField;

    /** Admin client */
    private AdminClient client;

    /** General user name */
    private String generalName;

    /** Name label */
    private JLabel nameLabel;

    /** Host label */
    private JLabel hostLabel;

    /***
     * Constructor
     */
    public ServerGUI() {
        generalName = "Admin";
        host = ConnectConfiguration.REMOTE_HOST;
        initComponents();
    }

    /***
     * Initializes GUI components
     */
    private void initComponents() {

        // General params
        setTitle("Server API");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        setLayout(new MigLayout("", "center", "center"));

        // Text area
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane pane = new JScrollPane(textArea);
        pane.setPreferredSize(new Dimension(400, 350));
        add(pane, "wrap");

        // Type panel
        JPanel typePanel = new JPanel();
        typePanel.setLayout(new MigLayout("", "center", "center"));

        // Field for typing
        typeField = new JTextField();
        typeField.setPreferredSize(new Dimension(400, typeField.getFont().getSize()));
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

        // Button for cleaning
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

        // Name label
        nameLabel = new JLabel("General username: " + generalName);
        add(nameLabel, "wrap");

        // Host label
        hostLabel = new JLabel("Server host: " + (host.equals(ConnectConfiguration.REMOTE_HOST)?
            "main server" : host));
        add(hostLabel, "wrap");
        JLabel helpLabel = new JLabel("Enter -help to see the list of "
                + "available commands");
        add(helpLabel);

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

        final JMenuItem menuItemChangeHost = new JMenuItem("Change server host");
        menuItemChangeHost.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newHost = HostChooser.getInstance().showDialog(ServerGUI.this, host);
                if (newHost != null) {
                    setServerHost(newHost);
                }
            }
        });
        menuSettings.add(menuItemChangeHost);

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
        if (typeField.getText().equals("-help")) {
            textArea.append(AdminLobby.COMMANDS + "\n");
            typeField.setText("");
            return;
        }
        if (client == null) {
            textArea.append("You are not connected to server!" + "\n");
            return;
        }
        if (! typeField.getText().equals("")) {
            client.addAdminCommand(new InternalRequest(typeField.getText()));
            typeField.setText("");
        }
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
        client = AdminClient.login(host, generalName, password, this, null);
        if (client != null) {
            textArea.append("Connected!\n");
            client.addAdminListener(this);
            client.addDisconnectListener(this);
        }
    }

    @Override
    public void hearDisconnection() {
        client.removeDisconnectListener(this);
        client = null;
    }

    @Override
    public void hear(ShowMessageEvent event) {
        JOptionPane.showMessageDialog(null, event.getMessage(),
                "Server information", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void hear(AdminEvent event) {
        switch (event.getIndex()) {
            case SERVER_LOG_E: {
                if (textArea.getLineCount() > 100) {
                    try {
                        textArea.getDocument().remove(0,
                                textArea.getText().indexOf("\n") + 1);
                    }
                    catch (BadLocationException e) {
                        textArea.setText("");
                    }
                }
                textArea.append(((ServerLogEvent) event).getMessage() + "\n");
                break;
            }
            case LOG_FILE_E: {
                try {
                    String filename = "server_logs.txt";
                    ArrayList<String> logs = ((LogFileEvent) event).getContent();
                    inflateToFile(logs, filename);
                    textArea.append("Successfully received " + logs.size() + " logs to " + filename);
                }
                catch (IOException e) {
                    textArea.append("Problems creating text file!");
                }
                break;
            }
            case LOG_SIZE_E: {
                textArea.append("Log file size: " + ((LogSizeEvent) event).getLength() + " bytes");
                break;
            }
        }
    }

    /***
     * Inflates data to text file
     * @param content content
     * @param filename filename
     * @throws IOException if something went wrong
     */
    private static void inflateToFile(ArrayList<String> content, String filename) throws IOException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
        for (String line: content) {
            writer.println(line);
        }
        writer.close();
    }


    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (IllegalAccessException | InstantiationException |
                ClassNotFoundException | UnsupportedLookAndFeelException e) {
            // handle exception lol
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ServerGUI();
            }
        });
    }

    public void setGeneralName(String generalName) {
        this.generalName = generalName;
        nameLabel.setText("General username: " + generalName);
    }

    public void setServerHost(String host) {
        this.host = host;
        hostLabel.setText("Server host: " + (host.equals(ConnectConfiguration.REMOTE_HOST)?
                "main server host" : "\"" + host + "\""));
    }
}
