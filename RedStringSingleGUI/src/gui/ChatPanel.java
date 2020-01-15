package gui;

import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.chat.ChatEvent;
import rslib.cs.protocol.events.chat.MessageEvent;
import rslib.cs.protocol.events.chat.NotificationEvent;
import rslib.listeners.ChatListener;
import rslib.listeners.DisconnectListener;
import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/***
 * Represents panel with chat and different chat functions
 */
public class ChatPanel extends JPanel implements DisconnectListener, ChatListener {

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
     */
    public ChatPanel(ChatWindow window, UserClient userClient) {
        if (window == null) {
            throw new NullPointerException("ChatPanel: window is null!");
        }
        this.window = window;
        this.userClient = userClient;
        initComponents();
    }

    /**
     * Initializes the GUI components
     */
    private void initComponents() {

        setLayout(new MigLayout("fill", "center", "center"));

        // ChatSpace
        JPanel chatSpacePanel = new JPanel();


        chatSpacePanel.setLayout(new MigLayout("fill", "center", "center"));

        // The chat area
        chat = new JTextArea();
        chat.setEditable(false);
        chat.setLineWrap(true);
        DefaultCaret caret = (DefaultCaret) chat.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane pane = new JScrollPane(chat);
        chatSpacePanel.add(pane, new CC().wrap().grow().width("300:1000:").height("200:1000:"));

        // Type panel
        JPanel typePanel = new JPanel();
        typePanel.setLayout(new MigLayout("fill", "center", "center"));

        // Field for typing
        typeField = new JTextField();
        typePanel.add(typeField, new CC().wrap().grow().width("300:1000:").height("20:50:"));
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

        add(chatSpacePanel, new CC().span());

        // Chat management
        managementFrame = new ManagementFrame(window, userClient);
        userClient.addDisconnectListener(managementFrame);
        userClient.addMainClientListener(managementFrame);
        JButton buttonManagement = new JButton("Management");
        buttonManagement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                managementFrame.showFrame();
            }
        });
        add(buttonManagement, new CC().span());
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
        if (userClient.checkRights()) {
            addMessage(username, message);
            typeField.setText("");
            userClient.addChatEvent(new MessageEvent(username, message));
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


    @Override
    public void hear(ChatEvent event) {
        switch (event.getIndex()) {
            case NOTIFICATION_E: {
                chat.append(((NotificationEvent) event).getMessage() + "\n");
                break;
            }
            case MESSAGE_E: {
                MessageEvent event1 = (MessageEvent) event;
                addMessage(event1.getUsername(), event1.getMessage());
                break;
            }
        }
    }

    @Override
    public void hearDisconnection() {
        window.disconnectGUIActions(userClient.getLobbyName());
        userClient.removeDisconnectListener(this);
    }
}
