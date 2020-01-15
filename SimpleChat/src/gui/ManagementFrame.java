package gui;

import client_server.client.UserClient;
import client_server.User;
import client_server.server.util.Status;
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
public class ManagementFrame extends JFrame {

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

    public static final Map<Byte, ImageIcon> STATUS_IMAGES;

    static {
        STATUS_IMAGES = new HashMap<Byte, ImageIcon>();
        STATUS_IMAGES.put(Status.USER_STATUS_READONLY,
                new ImageIcon(ManagementFrame.class.getResource("/icons/readonly_user.png")));
        STATUS_IMAGES.put(Status.USER_STATUS_COMMON_USER,
                new ImageIcon(ManagementFrame.class.getResource("/icons/common_user.png")));

        STATUS_IMAGES.put(Status.USER_STATUS_ROUTE,
                new ImageIcon(ManagementFrame.class.getResource("/icons/chat_route.png")));
        STATUS_IMAGES.put(Status.USER_STATUS_MODERATOR,
                new ImageIcon(ManagementFrame.class.getResource("/icons/moderator.png")));
    }

    /***
     * Constructor
     * @param userClient link to client
     */
    public ManagementFrame(UserClient userClient) {
        if (userClient == null) {
            throw new NullPointerException("AdministrationPanel: client is null!");
        }
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
        userStatusLabel = new JLabel("User status: " + Status.STATUS_STRINGS.get(userClient.getUserStatus()));
        userDataPanel.add(userStatusLabel, "wrap");

        // Change username
        JButton changeUsername = new JButton("Change username");
        changeUsername.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newName = JOptionPane.showInputDialog(null, "Enter new username:");
                if (newName == null || newName.equals("")) {
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
                if (newName == null || newName.equals("")) {
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
                if (newPassword == null || newPassword.equals("")) {
                    return;
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
        String[] statuses = new String[Status.STATUS_STRINGS.size()];
        int index = 0;
        Set<Map.Entry<Byte, String>> entries = Status.STATUS_STRINGS.entrySet();
        for (Map.Entry<Byte, String> entry : entries) {
            if (entry.getKey().equals(Status.USER_STATUS_ADMINISTRATOR)) {
                continue;
            }
            statuses[index ++] = entry.getValue();
        }
        final JComboBox statusBox = new JComboBox(statuses);
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
                byte newStatus = Status.USER_STATUS_COMMON_USER;
                Set<Map.Entry<Byte, String>> entries = Status.STATUS_STRINGS.entrySet();
                for (Map.Entry<Byte, String> entry : entries) {
                    if (entry.getValue().equals(statusBox.getSelectedItem())) {
                        newStatus = entry.getKey();
                    }
                }
                if (newStatus == Status.USER_STATUS_ROUTE) {
                    int result = JOptionPane.showConfirmDialog(null, "You will delegate your status to the current" +
                                    "user. Do you want to continue?",
                            "Delegating route status", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        userClient.delegateRouteRequest(((Map.Entry<User, String>) userList.getSelectedValue()).
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

        if (userClient.getUserStatus() < Status.USER_STATUS_MODERATOR) {
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
        if (userClient.getUserStatus() >= Status.USER_STATUS_MODERATOR) {
            userClient.getBanListRequest();
        }
        setVisible(true);
    }

    /***
     * Sets user list to gui
     * @param users users
     */
    public void setUsers(Map<User, String> users) {
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
    public void setBanList(Map<String, String> users) {
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
    public void changeUsername(String username) {
        usernameLabel.setText("Username: " + username);
    }

    /***
     * Displays actions when user status is changed
     * @param status new status
     */
    public void changeUserStatus(byte status) {
        userStatusLabel.setText("User status: " + Status.STATUS_STRINGS.get(status));
        if (status >= Status.USER_STATUS_MODERATOR) {
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
