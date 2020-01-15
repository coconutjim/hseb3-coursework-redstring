package gui;

import client_server.client.UserClient;
import client_server.client.util.UserClientConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/***
 * Represents main window
 */
public class ChatWindow extends JFrame {

    /** Chats GUIs*/
    private JTabbedPane tabs;

    /** Chat clients (String for lobby name)*/
    private Map<String, UserClient> clients;

    /** General user name */
    private String generalName;

    /** Info label */
    private JLabel infoLabel;

    /***
     * The constructor.
     */
    public ChatWindow() {
        clients = new HashMap<String, UserClient>();
        generalName = "Lev";
        initComponents();
    }

    /**
     * Initializes the GUI components
     */
    private void initComponents() {

        // General params
        setTitle("Chat");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // Control buttons
        JPanel controlPanel = new JPanel();

        final NewLobbyFrame newLobbyFrame = new NewLobbyFrame(this);
        // Button new chat
        JButton connect = new JButton("New chat");
        connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newLobbyFrame.showFrame();
            }
        });
        controlPanel.add(connect);

        // Button close chat
        JButton closeChat = new JButton("Close chat");
        closeChat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeSelectedChat();
            }
        });
        controlPanel.add(closeChat);

        add(controlPanel, BorderLayout.NORTH);

        // Tabs with chats
        tabs = new JTabbedPane();
        add(tabs, BorderLayout.CENTER);

        // Info label
        infoLabel = new JLabel("General username: " + generalName);
        add(infoLabel, BorderLayout.SOUTH);

        // Constructing menu bar
        setJMenuBar(createMenu());

        pack();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dimension.width / 2 - getSize().width / 2, dimension.height / 2 - 3 * getSize().height);
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
                JOptionPane.showMessageDialog(null, "Chat");
            }
        });
        menuBar.add(buttonAbout);

        return menuBar;

    }

    /***
     * Created new chat panel
     * @param userClient associated client
     * @param lobbyName lobby name
     */
    public void createChat(UserClient userClient, String lobbyName) {
        // Chat itself
        clients.put(lobbyName, userClient);
        ChatPanel chat = new ChatPanel(this, userClient, lobbyName);
        userClient.setChat(chat);
        tabs.addTab(lobbyName, chat);
        pack();
    }

    /***
     * Changing lobby name
     * @param oldName old name
     * @param newName new name
     */
    public void changeLobbyName(String oldName, String newName) {
        tabs.setTitleAt(tabs.indexOfTab(oldName), newName);
        UserClient userClient = clients.remove(oldName);
        clients.put(newName, userClient);
    }

    /***
     * Closes selected chat
     */
    private void closeSelectedChat() {
        int index = tabs.getSelectedIndex();
        if (index >= 0) {
            String lobbyName = tabs.getTitleAt(index);
            clients.get(lobbyName).disconnect("Disconnection by user!");
        }
    }

    /***
     * Disconnect gui actions
     * @param lobbyName name of closed lobby
     * @param message message to user
     */
    public void disconnectGUIActions(String lobbyName, String message) {
        clients.remove(lobbyName);
        tabs.remove(tabs.getComponentAt(tabs.indexOfTab(lobbyName)));
        if (message != null) {
            JOptionPane.showMessageDialog(null, message, "Disconnection", JOptionPane.ERROR_MESSAGE);
        }
        else {
            JOptionPane.showMessageDialog(null, "Disconnection by server!", "Disconnection", JOptionPane.ERROR_MESSAGE);
        }
    }

    /***
     * Reads config file and gets essential info
     * @throws IllegalArgumentException if data is not correct
     */
    private static void readConfig() throws IllegalArgumentException {

        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader("user.properties"));
            boolean readPort = false;
            boolean readHost = false;
            while ((line = br.readLine()) != null && ! line.equals("")) {
                String[] result = line.split(" ");
                if (result[0].equals("UserServerPort:") && result.length == 2) {
                    UserClientConfiguration.setPort(Integer.parseInt(result[1]));
                    readPort = true;
                }
                if (result[0].equals("ServerHost:") && result.length == 2) {
                    UserClientConfiguration.setHost(result[1]);
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
                new ChatWindow();
            }
        });
    }

    public void setGeneralName(String generalName) {
        //TODO: set name in all lobbies
        this.generalName = generalName;
        infoLabel.setText("General username: " + generalName);
    }

    public String getGeneralName() {
        return generalName;
    }
}
