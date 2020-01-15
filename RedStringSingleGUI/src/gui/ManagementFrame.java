package gui;

import rslib.cs.client.user.UserClient;
import rslib.cs.common.DataChecking;
import rslib.cs.common.Status;
import rslib.cs.common.User;
import rslib.cs.protocol.events.main_client.*;
import rslib.listeners.DisconnectListener;
import rslib.listeners.MainClientListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/***
 * Represents a frame with chat administrative functions
 */
public class ManagementFrame extends JFrame implements DisconnectListener, MainClientListener {

    /** Constants */
    private static final int LIST_WIDTH = 100;
    private static final int LIST_HEIGHT = 150;

    /** Link to window */
    private ChatWindow window;

    /** Link to client */
    private UserClient userClient;

    /** User list model */
    private DefaultListModel userListModel;

    /** Ban userList model */
    private DefaultListModel banListModel;

    /** General administration panel */
    private JPanel generalAdministrationPanel;

    /** User administration panel */
    private JPanel userAdministrationPanel;

    /** Ban list panel */
    private JPanel banListPanel;

    /** Username label */
    private JLabel usernameLabel;

    /** User status label */
    private JLabel userStatusLabel;

    public static final Map<Status, ImageIcon> STATUS_IMAGES;

    static {
        STATUS_IMAGES = new HashMap<>();
        STATUS_IMAGES.put(Status.READONLY,
                new ImageIcon(ManagementFrame.class.getResource("/icons/readonly_user.png")));
        STATUS_IMAGES.put(Status.COMMON,
                new ImageIcon(ManagementFrame.class.getResource("/icons/common_user.png")));

        STATUS_IMAGES.put(Status.LOBBY_ROOT,
                new ImageIcon(ManagementFrame.class.getResource("/icons/chat_root.png")));
        STATUS_IMAGES.put(Status.MODERATOR,
                new ImageIcon(ManagementFrame.class.getResource("/icons/moderator.png")));
    }

    /***
     * Constructor
     * @param window link to window
     * @param userClient link to client
     */
    public ManagementFrame(ChatWindow window, UserClient userClient) {
        if (window == null) {
            throw new NullPointerException("ManagementFrame: window is null!");
        }
        if (userClient == null) {
            throw new NullPointerException("ManagementFrame: userClient is null!");
        }
        this.window = window;
        this.userClient = userClient;
        initComponents();
    }

    /**
     * Initializes the GUI components
     */
    private void initComponents() {

        setTitle("Management: Lobby " + userClient.getLobbyName());
        setLayout(new MigLayout("", "center", "top"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // User data panel
        JPanel userDataPanel = new JPanel();
        userDataPanel.setLayout(new MigLayout("", "center", "center"));
        userDataPanel.add(setLabelFontBold(new JLabel("Your data:")), "wrap");

        // Username label
        usernameLabel = new JLabel("Username: " + userClient.getUsername());
        userDataPanel.add(usernameLabel, "wrap");


        // User status label
        userStatusLabel = new JLabel("User status: " + userClient.getUserStatus().toString());
        userDataPanel.add(userStatusLabel, "wrap");

        // Change username
        JButton changeUsername = new JButton("Change username");
        changeUsername.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newName = JOptionPane.showInputDialog(null, "Enter new username:");
                if (newName == null) {
                    return;
                }
                if (! DataChecking.isUsernameValid(newName)) {
                    JOptionPane.showMessageDialog(null, DataChecking.USERNAME_RULES,
                            "Input error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                userClient.changeUsernameRequest(newName);
            }
        });
        userDataPanel.add(changeUsername, "wrap");

        add(userDataPanel, "span");

        // General administration panel
        generalAdministrationPanel = new JPanel();
        generalAdministrationPanel.setLayout(new MigLayout("", "center", "center"));

        generalAdministrationPanel.add(setLabelFontBold(new JLabel("General lobby settings:")), "wrap");

        // Change lobby name
        JButton changeLobbyName = new JButton("Change name");
        changeLobbyName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newName = JOptionPane.showInputDialog(null, "Enter new lobby name:");
                if (newName == null) {
                    return;
                }
                if (! DataChecking.isLobbyNameValid(newName)) {
                    JOptionPane.showMessageDialog(null, DataChecking.LOBBY_NAME_RULES,
                            "Input error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                userClient.changeLobbyNameRequest(newName);
            }
        });
        generalAdministrationPanel.add(changeLobbyName, "wrap");

        // Change lobby password
        JButton changeLobbyPassword = new JButton("Change password");
        changeLobbyPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newPassword = JOptionPane.showInputDialog(null, "Enter new lobby password:");
                if (newPassword == null) {
                    return;
                }
                if (! DataChecking.isLobbyPasswordValid(newPassword)) {
                    JOptionPane.showMessageDialog(null, DataChecking.LOBBY_PASSWORD_RULES,
                            "Input error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (newPassword.equals("")) {
                    newPassword = null;
                }
                userClient.changeLobbyPasswordRequest(newPassword);
            }
        });
        generalAdministrationPanel.add(changeLobbyPassword, "wrap");

        add(generalAdministrationPanel);

        // User list panel
        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(new MigLayout("", "center", "center"));

        userListPanel.add(setLabelFontBold(new JLabel("User list:")), "wrap");

        // User list
        userListModel = new DefaultListModel();
        final JList userList = new JList(userListModel);
        userList.setCellRenderer(new UserCellRenderer());
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));
        userListPanel.add(userScrollPane, "wrap");

        // Refresh user list
        JButton refreshUserList = new JButton("Refresh user list");
        refreshUserList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userClient.getUserListRequest();
            }
        });
        userListPanel.add(refreshUserList, "wrap");

        // User administration panel
        userAdministrationPanel = new JPanel();
        userAdministrationPanel.setLayout(new MigLayout("", "center", "center"));

        // Kick user
        JButton kickUser = new JButton("Kick user");
        kickUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (userList.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(null, "No user selected!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                userClient.kickRequest(((Map.Entry<User, String>) userList.getSelectedValue()).
                        getKey().getUsername());
            }
        });
        userAdministrationPanel.add(kickUser, "wrap");

        // Ban user
        JButton banUser = new JButton("Ban user");
        banUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (userList.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(null, "No user selected!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                userClient.banRequest(((Map.Entry<User, String>) userList.getSelectedValue()).
                        getKey().getUsername());
            }
        });
        userAdministrationPanel.add(banUser, "wrap");

        // User status combo box
        Status[] statuses = Status.values();
        String[] strings = new String[statuses.length - 1];
        int i = 0;
        for (Status status : statuses) {
            if (status != Status.ADMINISTRATOR) {
                strings[i ++] = status.toString();
            }
        }
        final JComboBox statusBox = new JComboBox(strings);
        statusBox.setSelectedIndex(1);

        // Change user status
        JButton changeStatus = new JButton("Change user status to: ");
        changeStatus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (userList.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(null, "No user selected!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (statusBox.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(null, "No status selected!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Status newStatus = null;
                for (Status status : Status.values()) {
                    if (status.toString().equals(statusBox.getSelectedItem())) {
                        newStatus = status;
                    }
                }
                if (newStatus == null) {
                    return;
                }
                if (newStatus == Status.LOBBY_ROOT) {
                    int result = JOptionPane.showConfirmDialog(null, "You will delegate your status to the current" +
                                    "user. Do you want to continue?",
                            "Delegating root status", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        userClient.delegateRootRequest(((Map.Entry<User, String>) userList.getSelectedValue()).
                                getKey().getUsername());
                    }
                } else {
                    userClient.changeUserStatusRequest(((Map.Entry<User, String>) userList.getSelectedValue()).
                            getKey().getUsername(), newStatus);
                }
            }
        });
        userAdministrationPanel.add(changeStatus, "wrap");

        userAdministrationPanel.add(statusBox, "wrap");

        userListPanel.add(userAdministrationPanel);

        add(userListPanel);

        // Ban list panel
        banListPanel = new JPanel();
        banListPanel.setLayout(new MigLayout("", "center", "center"));

        banListPanel.add(setLabelFontBold(new JLabel("Ban list:")), "wrap");

        // Ban list
        banListModel = new DefaultListModel();
        final JList banList = new JList(banListModel);
        banList.setCellRenderer(new BannedCellRenderer());
        JScrollPane banScrollPane = new JScrollPane(banList);
        banScrollPane.setPreferredSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));
        banListPanel.add(banScrollPane, "wrap");

        // Refresh user list
        JButton refreshBanList = new JButton("Refresh ban list");
        refreshBanList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userClient.getBanListRequest();
            }
        });
        banListPanel.add(refreshBanList, "wrap");

        // Unban user
        JButton unbanUser = new JButton("Unban user");
        unbanUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (banList.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(null, "No user selected!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                userClient.unbanRequest(((Map.Entry<String, String>) banList.getSelectedValue()).getValue());
            }
        });
        banListPanel.add(unbanUser, "wrap");

        add(banListPanel);

        if (userClient.getUserStatus().ordinal() < Status.MODERATOR.ordinal()) {
            setAdministrationPanelsEnabled(false);
        }

        pack();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dimension.width / 2 - getSize().width / 2, dimension.height / 2 - getSize().height / 2);
    }

    /***
     * Shows management frame
     */
    public void showFrame() {
        userClient.getUserListRequest();
        if (userClient.getUserStatus().ordinal() >= Status.MODERATOR.ordinal()) {
            userClient.getBanListRequest();
        }
        setVisible(true);
    }

    @Override
    public void hearDisconnection() {
        userClient.removeDisconnectListener(this);
        dispose();
    }

    @Override
    public void hear(MainClientEvent event) {
        switch (event.getIndex()) {
            case USER_LIST_E: {
                setUsers(((SetUserListEvent)event).getUsers());
                break;
            }
            case BAN_E: {
                setBanList(((SetBanListEvent) event).getUsers());
                break;
            }
            case CHANGE_LOBBY_NAME_E: {
                ChangeLobbyNameEvent event1 = (ChangeLobbyNameEvent) event;
                window.changeLobbyName(event1.getOldName(), event1.getLobbyName());
                setTitle("Management: Lobby " + event1.getLobbyName());
                break;
            }
            case CHANGE_USERNAME_E: {
                changeUsername(((ChangeUsernameEvent)event).getNewName());
                break;
            }
            case CHANGE_USER_STATUS_E: {
                changeUserStatus(((ChangeUserStatusEvent)event).getStatus());
                break;
            }
        }
    }

    /***
     * Sets user list to gui
     * @param users users
     */
    private void setUsers(Map<User, String> users) {
        userListModel.removeAllElements();
        Set<Map.Entry<User, String>> entries = users.entrySet();
        for (Map.Entry<User, String> entry : entries) {
            userListModel.addElement(entry);
        }
    }

    /***
     * Sets ban list to gui
     * @param users ban list
     */
    private void setBanList(Map<String, String> users) {
        banListModel.removeAllElements();
        Set<Map.Entry<String, String>> entries = users.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            banListModel.addElement(entry);
        }
    }

    /***
     * Displays actions when username is changed
     * @param username new name
     */
    private void changeUsername(String username) {
        usernameLabel.setText("Username: " + username);
    }

    /***
     * Displays actions when user status is changed
     * @param status new status
     */
    private void changeUserStatus(Status status) {
        userStatusLabel.setText("User status: " + status.toString());
        if (status.ordinal() >= Status.MODERATOR.ordinal()) {
            setAdministrationPanelsEnabled(true);
        }
        else {
            banListModel.removeAllElements();
            setAdministrationPanelsEnabled(false);
        }
    }

    /***
     * Sets label font to bold
     * @param label current label
     * @return changed label
     */
    private static JLabel setLabelFontBold(JLabel label) {
        Font font = label.getFont();
        // same font but bold
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        label.setFont(boldFont);
        return label;
    }

    /***
     * Set JPanel components enable
     * @param panel panel
     * @param enabled enable or disable
     */
    private static void setPanelEnabled(JPanel panel, boolean enabled) {
        panel.setEnabled(enabled);
        Component[] components = panel.getComponents();
        for (Component component : components) {
            component.setEnabled(enabled);
        }
    }

    /***
     * Sets enable of panels which contain administration functions
     * @param enabled enable or disable
     */
    private void setAdministrationPanelsEnabled(boolean enabled) {
        setPanelEnabled(generalAdministrationPanel, enabled);
        setPanelEnabled(userAdministrationPanel, enabled);
        setPanelEnabled(banListPanel, enabled);
    }

    /***
     * For processing user listModel
     */
    class UserCellRenderer extends JLabel implements ListCellRenderer {

        /** Highlight color */
        private final Color highlightColor = new Color(0, 0, 128);

        /***
         * Constructor
         */
        public UserCellRenderer() {
            setOpaque(true);
            setIconTextGap(12);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            Map.Entry<User, String> entry = (Map.Entry<User, String>) value;
            User user = entry.getKey();
            String host = entry.getValue();
            setText(host == null ? user.getUsername() : "<html>" + user.getUsername() + "<br>" + host + "</html>");
            setIcon(STATUS_IMAGES.get(user.getStatus()));
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

    /***
     * For processing ban listModel
     */
    class BannedCellRenderer extends JLabel implements ListCellRenderer {

        /** Highlight color */
        private final Color highlightColor = new Color(0, 0, 128);

        /***
         * Constructor
         */
        public BannedCellRenderer() {
            setOpaque(true);
            setIconTextGap(12);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) value;
            setText(entry.getValue() + ": " + entry.getKey());
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
