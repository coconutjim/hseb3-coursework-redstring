package gui;

import client_server.client.UserClient;
import client_server.User;
import client_server.protocol.command_to_client.from_client.SendMessageCommand;
import client_server.server.util.Status;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

/***
 * Represents panel with chat and different chat functions
 */
public class ChatPanel extends JPanel {

    /** Link to window */
    private ChatWindow window;

    /** Link to associated client */
    private UserClient userClient;

    /** The chat itself */
    private JTextArea chat;

    /** Field for typing */
    private JTextField typeField;

    /** Management panel */
    private ManagementFrame managementFrame;

    /***
     * The constructor
     * @param window link to the window
     * @param lobbyName lobbyName
     */
    public ChatPanel(ChatWindow window, UserClient userClient, String lobbyName) {
        if (window == null) {
            throw new NullPointerException("ChatPanel: window is null!");
        }
        if (lobbyName == null) {
            throw new NullPointerException("ChatPanel: lobbyName is null!");
        }
        this.window = window;
        this.userClient = userClient;
        initComponents();
    }

    /**
     * Initializes the GUI components
     */
    private void initComponents() {

        setLayout(new MigLayout("", "center", "center"));

        // ChatSpace
        JPanel chatSpacePanel = new JPanel();


        chatSpacePanel.setLayout(new MigLayout("", "center", "center"));

        // The chat area
        chat = new JTextArea();
        chat.setEditable(false);
        chat.setLineWrap(true);
        DefaultCaret caret = (DefaultCaret) chat.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane pane = new JScrollPane(chat);
        pane.setPreferredSize(new Dimension(300, 200));
        chatSpacePanel.add(pane, "wrap");

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
                    sendTypedText();
                }
            }
        });

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new MigLayout("", "center", "center"));

        // Button for message sending
        JButton buttonSend = new JButton("Send");
        buttonSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendTypedText();
            }
        });
        buttonPanel.add(buttonSend);

        // Button for chat cleaning
        JButton buttonClear = new JButton("Clear");
        buttonClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chat.setText("");
            }
        });
        buttonPanel.add(buttonClear);

        typePanel.add(buttonPanel);

        chatSpacePanel.add(typePanel);

        add(chatSpacePanel, "span");

        // Chat management
        managementFrame = new ManagementFrame(userClient);
        JButton buttonManagement = new JButton("Management");
        buttonManagement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                managementFrame.showFrame();
            }
        });
        add(buttonManagement, "span");
    }

    /***
     * Processes and sends text from field
     */
    private void sendTypedText() {
        if (! typeField.getText().equals("")) {
            sendMessage(typeField.getText());
        }
    }

    /***
     * Process message form user.
     * Add message to chat and send it to other users
     * @param message the message
     */
    private void sendMessage(String message) {
        String username = userClient.getUsername();
        if (userClient.addCommand(new SendMessageCommand(username, message), Status.USER_STATUS_COMMON_USER)) {
            addMessage(username, message);
            typeField.setText("");
        }
    }

    /***
     * Adds message to the chat
     * @param username username
     * @param message the message
     */
    public void addMessage(String username, String message) {
        chat.append(username + ": " + message + "\n");
    }

    /***
     * Adds notification to the chat
     * @param message the message
     */
    public void addNotification(String message) {
        chat.append(message + "\n");
    }

    /***
     * Set user list to gui
     * @param users users
     */
    public void setUsers(Map<User, String> users) {
        managementFrame.setUsers(users);
    }

    /***
     * Set ban list to gui
     * @param users ban list
     */
    public void setBanList(Map<String, String> users) {
        managementFrame.setBanList(users);
    }

    /***
     * Changing lobby name
     * @param oldName old name
     * @param newName new name
     */
    public void changeLobbyName(String oldName, String newName) {
        window.changeLobbyName(oldName, newName);
        managementFrame.setTitle("Management: Lobby " + newName);
    }

    /***
     * Displays actions when username is changed
     * @param username new name
     */
    public void changeUsername(String username) {
        managementFrame.changeUsername(username);
    }

    /***
     * Displays actions when user status is changed
     * @param status new status
     */
    public void changeUserStatus(byte status) {
        managementFrame.changeUserStatus(status);
    }

    /***
     * Displays actions when you are banned
     */
    public void banned() {
        userClient.disconnect("You were banned!");
    }

    /***
     * Displays actions when you are kicked
     */
    public void kicked() {
        userClient.disconnect("You were kicked!");
    }

    /***
     * Gui disconnection actions
     * @param message disconnection message (can be null)
     */
    public void disconnect(String message) {
        managementFrame.dispose();
        window.disconnectGUIActions(userClient.getLobbyName(), message);
    }

    /***
     * Shows message to user
     * @param message the message
     */
    public void showMessageToUser(String message) {
        JOptionPane.showMessageDialog(null, message, "Network error", JOptionPane.ERROR_MESSAGE);
    }
}
