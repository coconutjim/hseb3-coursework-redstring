package gui;

import rslib.cs.client.user.UserClient;
import rslib.cs.common.DataChecking;
import rslib.cs.protocol.events.message.ShowMessageEvent;
import rslib.listeners.MessageListener;
import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Represents main window
 */
public class ChatWindow extends JFrame implements MessageListener {

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
        clients = new ConcurrentHashMap<>(); // concurrency
        generalName = "LevOsipov";
        initComponents();
    }

    /**
     * Initializes the GUI components
     */
    private void initComponents() {

        // General params
        setTitle("Chat");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLayout(new MigLayout("fill", "center", "center"));

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

        getContentPane().add(controlPanel, new CC().wrap().grow());

        // Tabs with chats
        tabs = new JTabbedPane();
        getContentPane().add(tabs, new CC().wrap().grow());

        // Info label
        infoLabel = new JLabel("General username: " + generalName);
        getContentPane().add(infoLabel, new CC().wrap().grow());

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
                if (username == null) {
                    return;
                }
                if (! DataChecking.isUsernameValid(username)) {
                    JOptionPane.showMessageDialog(null, DataChecking.USERNAME_RULES,
                            "Input error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                setGeneralName(username);

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
        ChatPanel chat = new ChatPanel(this, userClient);
        userClient.addDisconnectListener(chat);
        userClient.addChatListener(chat);
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
     */
    public void disconnectGUIActions(String lobbyName) {
        clients.remove(lobbyName);
        tabs.remove(tabs.getComponentAt(tabs.indexOfTab(lobbyName)));
    }

    @Override
    public void hear(ShowMessageEvent event) {
        JOptionPane.showMessageDialog(null, event.getMessage(),
                "Server information", JOptionPane.INFORMATION_MESSAGE);
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
        catch ( IllegalAccessException | InstantiationException |
                ClassNotFoundException | UnsupportedLookAndFeelException e) {
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
