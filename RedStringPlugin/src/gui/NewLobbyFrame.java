package gui;

import gui.chat.ChatPanel;
import rslib.cs.client.user.UserClient;
import rslib.cs.common.LobbyInfo;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import rslib.cs.common.DataChecking;
import org.lev.redstringplugin.RedStringAction;
import rslib.cs.common.ConnectConfiguration;

/***
 * Represents a GUI with an available lobby list
 */
public class NewLobbyFrame extends JFrame {

    /** Link to action */
    private RedStringAction action;
    
    /** Lobby list model */
    private DefaultListModel lobbyListModel;
    
    /** Current server host label */
    private JLabel hostLabel;
    
    /** Current server host */
    private String host;
    
    /** Connection status label */
    private JLabel statusLabel;
    
    /** Link to server frame */
    private LocalServerFrame serverFrame;

    /***
     * Constructor
     * @param window link to action;
     */
    public NewLobbyFrame(RedStringAction action) {
        if (action == null) {
            throw new NullPointerException("NewLobbyFrame: action is null!");
        }
        this.action = action;
        host = ConnectConfiguration.REMOTE_HOST;
        initComponents();
    }

    /**
     * Initializes GUI components
     */
    private void initComponents() {

        setTitle("New lobby");
        setLayout(new MigLayout("", "center", "center"));
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        hostLabel = new JLabel("Current server host: " + getHost());
        add(hostLabel, "wrap");
        
        // Button for changing server
        JButton buttonChangeServer = new JButton("Change server");
        buttonChangeServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newHost = HostChooser.getInstance()
                        .showDialog(NewLobbyFrame.this, host);
                if (newHost != null) {
                    setServerHost(newHost);
                    getLobbies();
                }
            }
        });
        add(buttonChangeServer, "wrap");

        // Lobby list
        lobbyListModel = new DefaultListModel();
        final JList lobbyList = new JList(lobbyListModel);
        lobbyList.setCellRenderer(new LobbyCellRenderer());
        JScrollPane lobbyScrollPane = new JScrollPane(lobbyList);
        lobbyScrollPane.setPreferredSize(new Dimension(150, 300));
        add(lobbyScrollPane, "wrap");
        
        statusLabel = new JLabel();
        add(statusLabel, "wrap");

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new MigLayout("", "center", "center"));
        
        // Button for connecting to existing lobby
        JButton buttonConnectLobby = new JButton("Connect to lobby");
        buttonConnectLobby.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lobbyList.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(NewLobbyFrame.this,
                            "No lobby selected!", 
                            "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                connectToLobby((LobbyInfo)lobbyList.getSelectedValue());
            }
        });
        buttonPanel.add(buttonConnectLobby);

        // Button creating lobby
        JButton buttonCreateLobby = new JButton("Create new lobby");
        buttonCreateLobby.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createLobby();
            }
        });
        buttonPanel.add(buttonCreateLobby);

        add(buttonPanel, "wrap");

        // Button for refreshing lobby list
        JButton buttonRefreshLobbies = new JButton("Refresh");
        buttonRefreshLobbies.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getLobbies();
            }
        });
        buttonPanel.add(buttonRefreshLobbies);
        
        // Button for creating server
        JButton buttonCreateServer = new JButton("Create/show local server");
        buttonCreateServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (serverFrame != null) {
                    serverFrame.setVisible(true);
                }
                else {
                    serverFrame = new LocalServerFrame(NewLobbyFrame.this);
                    serverFrame.setVisible(true);
                }
                setServerHost("localhost");
                getLobbies();
            }
        });
        add(buttonCreateServer, "wrap");

        pack();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dimension.width / 2 - getSize().width / 2, dimension.height / 2 - getSize().height / 2);
    }
    
    /***
     * Connects to server and gets lobbies
     */
    private void getLobbies() {
        lobbyListModel.removeAllElements();
        System.out.println(host);
        statusLabel.setText("Connecting...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<LobbyInfo> lobbyInfos = 
                    UserClient.getLobbies(host, action, null);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (lobbyInfos != null) {
                            setLobbies(lobbyInfos);
                            statusLabel.setText("Sucessfully got lobby list from " + getHost() + "!");
                        }
                        else {
                            statusLabel.setText("Failed to connect to " + getHost() + "!");
                        }
                    }
                });
            }
        }).start();
    }

    /***
     * Shows lobby frame
     */
    public void showFrame() {
        getLobbies();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
            }
        });
    }
    
    /***
     * Creates GUI for new chat
     * @param userClient link to client
     */
    private void createGUI(UserClient userClient) {
        
        // Management frame
        LobbyManagementFrame lobbyManagementFrame = new LobbyManagementFrame(userClient);
        userClient.addDisconnectListener(lobbyManagementFrame);
        userClient.addMainClientListener(lobbyManagementFrame);
        
        // Chat
        final ChatPanel chat = new ChatPanel(userClient, lobbyManagementFrame);
        userClient.addDisconnectListener(chat);
        userClient.addChatListener(chat);
        userClient.addMainClientListener(chat);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chat.open();
                chat.requestActive();
            }
        });     
    }

    /***
     * Sets lobby list to GUI
     * @param lobbyInfos info about lobbies
     */
    private void setLobbies(final ArrayList<LobbyInfo> lobbyInfos) {
        lobbyListModel.removeAllElements();
        for (LobbyInfo lobbyInfo : lobbyInfos) {
            lobbyListModel.addElement(lobbyInfo);
        }
    }

    
    /***
     * Connects to lobby
     * @param lobbyInfo 
     */
    public void connectToLobby(LobbyInfo lobbyInfo) {
        String lobbyName = lobbyInfo.getLobbyName();
        boolean secured = lobbyInfo.isSecured();
        String password = null;
        if (secured) {
            password = JOptionPane.showInputDialog(this, "Enter password for this lobby:");
            if (password == null) {
                return;
            }
        }

        UserClient userClient = UserClient.login(host, "PluginUser",
                new LobbyInfo(lobbyName, secured, password), action, null);
        if (userClient != null) {
            createGUI(userClient);
            dispose();
        }
    }

    /***
     * Creates new lobby
     */
    public void createLobby() {
        String lobbyName = JOptionPane.showInputDialog(this, "Enter new lobby name:");
        if (lobbyName == null) {
            return;
        }
        if (! DataChecking.isLobbyNameValid(lobbyName)) {
            JOptionPane.showMessageDialog(this, DataChecking.LOBBY_NAME_RULES,
                    "Input error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String password = JOptionPane.showInputDialog(this, "Enter password for this lobby:");
        if (password == null) {
            return;
        }
        if (! DataChecking.isLobbyPasswordValid(password)) {
            JOptionPane.showMessageDialog(this, DataChecking.LOBBY_PASSWORD_RULES,
                    "Input error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean secured = ! password.equals("");
        UserClient userClient = UserClient.createLobby(host, "PluginUser",
                new LobbyInfo(lobbyName, secured, secured ? password : null),
                action, null);
        if (userClient != null) {
            createGUI(userClient);
            dispose();
        }
    }
    
    private void setServerHost(String host) {
        this.host = host;
        hostLabel.setText("Current server host: " + getHost());
        
    }
    
    private String getHost() {
        return (host != null && 
                host.equals(ConnectConfiguration.REMOTE_HOST))?
                "main server" : "\"" + host + "\"";
    }

    /***
     * For processing lobby list model
     */
    class LobbyCellRenderer extends JLabel implements ListCellRenderer {

        /** Highlight color */
        private final Color highlightColor = new Color(0, 0, 128);

        /***
         * Constructor
         */
        public LobbyCellRenderer() {
            setOpaque(true);
            setIconTextGap(12);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            LobbyInfo lobbyInfo = (LobbyInfo) value;
            String lobbyName = lobbyInfo.getLobbyName();
            boolean secured = lobbyInfo.isSecured();
            setText(lobbyName + "    " + (secured ? "Secured" : "Not secured"));
            if (isSelected) {
                setBackground(highlightColor);
                setForeground(Color.white);
            } else {
                setBackground(Color.white);
                setForeground(Color.black);
            }
            return this;
        }
    }
    
    public void setServerFrame(LocalServerFrame serverFrame) {
        this.serverFrame = serverFrame;
    }
}
