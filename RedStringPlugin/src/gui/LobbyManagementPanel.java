package gui;

import gui.util.Images;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import rslib.cs.client.user.UserClient;
import rslib.cs.common.DataChecking;
import rslib.cs.common.Status;
import rslib.cs.common.User;
import rslib.cs.protocol.events.main_client.ChangeUserStatusEvent;
import rslib.cs.protocol.events.main_client.ChangeUsernameEvent;
import rslib.cs.protocol.events.main_client.MainClientEvent;
import rslib.cs.protocol.events.main_client.SetBanListEvent;
import rslib.cs.protocol.events.main_client.SetUserListEvent;
import rslib.listeners.MainClientListener;
import net.miginfocom.swing.MigLayout;

/***
 * Represents a panel with user management
 */
public class LobbyManagementPanel extends JPanel 
        implements MainClientListener {
    
    
    /** Constants */
    private static final int LIST_WIDTH = 100;
    private static final int LIST_HEIGHT = 150;
    
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

    /***
     * Constructor
     * @param userClient link to client
     */
    public LobbyManagementPanel(UserClient userClient) {
        if (userClient == null) {
            throw new IllegalArgumentException("LobbyManagementPanel: "
                    + "userClient is null!");
        }
        this.userClient = userClient;
        initComponents();
    }

    /**
     * Initializes the GUI components
     */
    private void initComponents() {
        setLayout(new MigLayout("", "center", "top"));

        // User data panel
        JPanel userDataPanel = new JPanel();
        userDataPanel.setLayout(new MigLayout("", "center", "center"));
        userDataPanel.add(setLabelFontBold(new JLabel("Your data:")), "wrap");

        // Username label
        usernameLabel = new JLabel("Username: " + userClient.getUsername());
        userDataPanel.add(usernameLabel, "wrap");


        // User status label
        userStatusLabel = new JLabel("User status: " + 
                userClient.getUserStatus().toString());
        userDataPanel.add(userStatusLabel, "wrap");

        // Change username
        JButton changeUsername = new JButton("Change username");
        changeUsername.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newName = JOptionPane.showInputDialog(LobbyManagementPanel.this, 
                        "Enter new username:");
                if (newName == null) {
                    return;
                }
                if (! DataChecking.isUsernameValid(newName)) {
                    JOptionPane.showMessageDialog(LobbyManagementPanel.this, 
                            DataChecking.USERNAME_RULES,
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
                String newName = JOptionPane.showInputDialog(LobbyManagementPanel.this,
                        "Enter new lobby name:");
                if (newName == null) {
                    return;
                }
                if (! DataChecking.isLobbyNameValid(newName)) {
                    JOptionPane.showMessageDialog(LobbyManagementPanel.this, 
                            DataChecking.LOBBY_NAME_RULES,
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
                String newPassword = JOptionPane.showInputDialog(LobbyManagementPanel.this, 
                        "Enter new lobby password:");
                if (newPassword == null) {
                    return;
                }
                if (! DataChecking.isLobbyPasswordValid(newPassword)) {
                    JOptionPane.showMessageDialog(LobbyManagementPanel.this, 
                            DataChecking.LOBBY_PASSWORD_RULES,
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
                    JOptionPane.showMessageDialog(LobbyManagementPanel.this,
                            "No user selected!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String name = ((Map.Entry<User, String>) userList.getSelectedValue()).
                        getKey().getUsername();
                if (name.equals(userClient.getUsername())) {
                    JOptionPane.showMessageDialog(LobbyManagementPanel.this, 
                            "You can not kick yourself!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }        
                userClient.kickRequest(name);
            }
        });
        userAdministrationPanel.add(kickUser, "wrap");

        // Ban user
        JButton banUser = new JButton("Ban user");
        banUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (userList.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(LobbyManagementPanel.this, 
                            "No user selected!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String name = ((Map.Entry<User, String>) userList.getSelectedValue()).
                        getKey().getUsername();
                if (name.equals(userClient.getUsername())) {
                    JOptionPane.showMessageDialog(LobbyManagementPanel.this, 
                            "You can not ban yourself!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                userClient.banRequest(name);
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
                    JOptionPane.showMessageDialog(LobbyManagementPanel.this, 
                            "No user selected!", 
                            "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (statusBox.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(LobbyManagementPanel.this, 
                            "No status selected!", "Warning", JOptionPane.WARNING_MESSAGE);
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
                String name = ((Map.Entry<User, String>) userList.getSelectedValue()).
                                getKey().getUsername();
                if (name.equals(userClient.getUsername())) {
                    JOptionPane.showMessageDialog(LobbyManagementPanel.this, 
                            "You can not change status of yourself!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (newStatus == Status.LOBBY_ROOT) {
                    int result = JOptionPane.showConfirmDialog(LobbyManagementPanel.this, 
                            "You will delegate your status to the current " +
                                    "user. Do you want to continue?",
                            "Delegating root status", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        userClient.delegateRootRequest(name);
                    }
                } else {
                    userClient.changeUserStatusRequest(name, newStatus);
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
                    JOptionPane.showMessageDialog(LobbyManagementPanel.this, 
                            "No user selected!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                userClient.unbanRequest(((Map.Entry<String, String>) banList.getSelectedValue()).getValue());
            }
        });
        banListPanel.add(unbanUser, "wrap");

        add(banListPanel);

        if (userClient.getUserStatus().ordinal() < 
                Status.MODERATOR.ordinal()) {
            setAdministrationPanelsEnabled(false);
        }
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
    private void setUsers(final Map<User, String> users) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                userListModel.removeAllElements();
                Set<Map.Entry<User, String>> entries = users.entrySet();
                for (Map.Entry<User, String> entry : entries) {
                    userListModel.addElement(entry);
                }
            }
        });
    }

    /***
     * Sets ban list to gui
     * @param users ban list
     */
    private void setBanList(final Map<String, String> users) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                banListModel.removeAllElements();
                Set<Map.Entry<String, String>> entries = users.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    banListModel.addElement(entry);
                }
            }
        });
    }

    /***
     * Displays actions when username is changed
     * @param username new name
     */
    private void changeUsername(final String username) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                usernameLabel.setText("Username: " + username);
            }
        });
    }

    /***
     * Displays actions when user status is changed
     * @param status new status
     */
    private void changeUserStatus(final Status status) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                userStatusLabel.setText("User status: " + status.toString());
                if (status.ordinal() >= Status.MODERATOR.ordinal()) {
                    setAdministrationPanelsEnabled(true);
                }
                else {
                    banListModel.removeAllElements();
                    setAdministrationPanelsEnabled(false);
                }
            }
        });
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
            String username = user.getUsername();
            setText(host == null ? username : "<html>" + username + "<br>" + host + "</html>");
            /*int hash = username.hashCode();
            Color temp = new Color(hash);
            Color fg = new Color(temp.getRed(), 
                    temp.getGreen(), temp.getBlue(), 255);*/
            setIcon(Images.STATUS_ICONS.get(user.getStatus()));
            if (isSelected) {
                //setBackground(highlightColor);
                //setForeground(Color.WHITE);
                setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            } else {
                //setBackground(Color.WHITE);
                //setForeground(Color.BLACK);
                setBorder(null);
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
