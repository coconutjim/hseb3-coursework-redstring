package gui;

import client_server.LobbyInfo;
import client_server.client.UserClient;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/***
 * Represents a gui with an available lobby list
 */
public class NewLobbyFrame extends JFrame {

    /** Lobby list model */
    private DefaultListModel lobbyListModel;

    /** Link to window */
    private ChatWindow window;

    /***
     * Constructor
     * @param window link to window;
     */
    public NewLobbyFrame(ChatWindow window) {
        if (window == null) {
            throw new NullPointerException("NewLobbyFrame: window is null!");
        }
        this.window = window;
        initComponents();
    }

    /**
     * Initializes the GUI components
     */
    private void initComponents() {

        setTitle("Lobby list");
        setLayout(new MigLayout("", "center", "top"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // User list
        lobbyListModel = new DefaultListModel();
        final JList lobbyList = new JList(lobbyListModel);
        lobbyList.setCellRenderer(new LobbyCellRenderer());
        JScrollPane lobbyScrollPane = new JScrollPane(lobbyList);
        lobbyScrollPane.setPreferredSize(new Dimension(150, 300));
        add(lobbyScrollPane, "wrap");


        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new MigLayout("", "center", "center"));

        // Button for connecting to existing lobby
        JButton buttonSend = new JButton("Connect to lobby");
        buttonSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lobbyList.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(null, "No lobby selected!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                connectToLobby((LobbyInfo)lobbyList.getSelectedValue());
            }
        });
        buttonPanel.add(buttonSend);

        // Button for chat cleaning
        JButton buttonClear = new JButton("Create new lobby");
        buttonClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createLobby();
            }
        });
        buttonPanel.add(buttonClear);

        add(buttonPanel, "wrap");

        // Button for refreshing lobby list
        JButton buttonRefreshLobbies = new JButton("Refresh");
        buttonRefreshLobbies.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<LobbyInfo> lobbyInfos = UserClient.getLobbies();
                if (lobbyInfos != null) {
                    setLobbies(lobbyInfos);
                }
            }
        });
        buttonPanel.add(buttonRefreshLobbies);

        pack();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dimension.width / 2 - getSize().width / 2, dimension.height / 2 - getSize().height / 2);
    }

    /***
     * Shows lobby frame
     */
    public void showFrame() {
        ArrayList<LobbyInfo> lobbyInfos = UserClient.getLobbies();
        if (lobbyInfos != null) {
            setLobbies(lobbyInfos);
            setVisible(true);
        }
    }

    /***
     * Sets lobby list to gui
     * @param lobbyInfos info about lobbies
     */
    private void setLobbies(ArrayList<LobbyInfo> lobbyInfos) {
        lobbyListModel.removeAllElements();
        for (LobbyInfo lobbyInfo : lobbyInfos) {
            lobbyListModel.addElement(lobbyInfo);
        }
    }

    /** Connects to lobby */
    public void connectToLobby(LobbyInfo lobbyInfo) {
        String lobbyName = lobbyInfo.getLobbyName();
        boolean secured = lobbyInfo.isSecured();
        String password = null;
        if (secured) {
            password = JOptionPane.showInputDialog(null, "Enter password for this lobby:");
            if (password == null) {
                return;
            }
        }

        UserClient userClient = UserClient.login(window.getGeneralName(),
                new LobbyInfo(lobbyName, secured, password));
        if (userClient != null) {
            window.createChat(userClient, lobbyName);
            dispose();
        }
    }

    /** Creates the lobby */
    public void createLobby() {
        String lobbyName = JOptionPane.showInputDialog(null, "Enter new lobby name:");
        if (lobbyName == null || lobbyName.equals("")) {
            return;
        }
        String password = JOptionPane.showInputDialog(null, "Enter password for this lobby:");
        if (password == null) {
            return;
        }
        boolean secured = ! password.equals("");
        UserClient userClient = UserClient.createLobby(window.getGeneralName(),
                new LobbyInfo(lobbyName, secured, secured ? password : null));
        if (userClient != null) {
            window.createChat(userClient, lobbyName);
            dispose();
        }
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
}
