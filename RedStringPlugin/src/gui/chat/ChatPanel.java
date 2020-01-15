package gui.chat;

import gui.LobbyManagementFrame;
import gui.board.BoardPanel;
import gui.board_frame.BoardFrame;
import gui.parsing.Parsing;
import gui.util.ColorScheme;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.chat.ChatEvent;
import rslib.cs.protocol.events.chat.NotificationEvent;
import rslib.listeners.ChatListener;
import rslib.listeners.DisconnectListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import rslib.cs.protocol.events.main_client.MainClientEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import rslib.cs.common.Status;
import rslib.cs.protocol.events.chat.MessageEvent;
import rslib.cs.protocol.events.main_client.ChangeLobbyNameEvent;
import rslib.cs.protocol.events.setup.BoardRequest;
import rslib.cs.protocol.events.setup.CloseBoardEvent;
import rslib.cs.protocol.events.setup.SetBoardEvent;
import rslib.cs.protocol.events.setup.SetupEvent;
import rslib.gui.board.InteractiveBoard;
import rslib.listeners.MainClientListener;
import rslib.listeners.SetupListener;
import net.miginfocom.layout.CC;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import rslib.gui.board.ExternalizableBoard;
import rslib.gui.container.ExternalizableContainer;

/***
 * Represents panel with chat and different chat functions.
 * Gives access to board
 */
public class ChatPanel extends TopComponent implements DisconnectListener, 
        ChatListener, MainClientListener, SetupListener {

    /** Link to associated client */
    private UserClient userClient;
    
    /** Link to board */
    private BoardFrame boardFrame;

    /** The chat itself */
    private JTextArea chat;

    /** Field for typing */
    private JTextField typeField;

    /** Management panel */
    private LobbyManagementFrame lobbyManagementFrame;
    
    /** Inactive counter */
    private int counter;
    
    /** If active */
    private boolean active;
    
    /** For date formating */
    private SimpleDateFormat sdf;

    /***
     * Constructor
     * @param userClient link to client
     * @param managementFrame link to management frame
     */
    public ChatPanel(UserClient userClient, LobbyManagementFrame managementFrame) {
        if (userClient == null) {
            throw new NullPointerException("ChatPanel: userClient is null!");
        }
        if (managementFrame == null) {
            throw new NullPointerException("ChatPanel: managementFrame is null!");
        }
        this.userClient = userClient;
        counter = 0;
        active = true;
        this.lobbyManagementFrame = managementFrame;
        setName(userClient.getLobbyName());
        sdf = new SimpleDateFormat("HH:mm:ss");
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
        pane.setPreferredSize(new Dimension(300, 350));
        chatSpacePanel.add(pane, new CC().wrap());

        // Type panel
        JPanel typePanel = new JPanel();
        typePanel.setLayout(new MigLayout("fill", "center", "center"));

        // Field for typing
        typeField = new JTextField();
        typeField.setPreferredSize(new Dimension(300, 
                typeField.getFont().getSize()));
        typePanel.add(typeField, new CC().wrap());
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

        // Chat management
        JButton buttonManagement = new JButton("Management");
        buttonManagement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lobbyManagementFrame.showFrame();
            }
        });
        buttonPanel.add(buttonManagement, "span");
        
        // Show Board
        JButton buttonBoard = new JButton("Show board");
        buttonBoard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (boardFrame == null) {
                    userClient.addSetupListener(ChatPanel.this);
                    ColorScheme scheme = ColorScheme.COLOR_SCHEMES.get("Board scheme #1");
                    Color fgColor;
                    Color bgColor;
                    if (scheme != null) {
                        fgColor = scheme.getForeground();
                        bgColor = scheme.getBackground();
                    }
                    else {
                        fgColor = Color.BLACK;
                        bgColor = Color.BLUE;
                    }
                    ExternalizableBoard board = new ExternalizableBoard(userClient.getUsername(),
                            Status.COMMON, 0, 0, BoardPanel.MINIMUM_BOARD_WIDTH,
                            BoardPanel.MINIMUM_BOARD_HEIGHT, BoardPanel.MINIMUM_BOARD_WIDTH,
                            BoardPanel.MINIMUM_BOARD_HEIGHT, BoardPanel.MAXIMUM_BOARD_WIDTH,
                            BoardPanel.MAXIMUM_BOARD_HEIGHT,
                            userClient.getLobbyName(),
                            InteractiveBoard.BOARD_ID, Parsing.convertToFontModel(
                                    new Font("Calibri", Font.BOLD, 20)), true,
                            Parsing.convertToColorModel(fgColor), 
                            Parsing.convertToColorModel(bgColor),
                            false, null,
                            new CopyOnWriteArrayList<ExternalizableContainer>(),
                            Parsing.convertToFontModel(
                                    new Font("Calibri", Font.BOLD, 20)), true,
                            Parsing.convertToColorModel(Color.BLACK), 
                            Parsing.convertToColorModel(Color.BLUE), true
                    );
                    userClient.addSetupEvent(new BoardRequest(board));
                }
                else {
                    boardFrame.requestVisible();
                }
            }
        });
        buttonPanel.add(buttonBoard, "span");
        
        typePanel.add(buttonPanel);

        chatSpacePanel.add(typePanel);

        JScrollPane scroll = new JScrollPane(chatSpacePanel);
        scroll.setBorder(null);
        add(scroll);
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
        appendText(username + ": " + message);
    }
    
    /***
     * Appends text to chat. If window is not active, makes a notification
     * in the title
     * @param text 
     */
    private void appendText(String text) {
        chat.append(sdf.format(new Date()) + ": " + text + "\n");
        if (! active) {
            setChatTabName(userClient.getLobbyName() + " +" + 
                        Integer.toString(++ counter));
            requestAttention(true);
            setAttentionHighlight(true);
        }
    }


    @Override
    public void hear(ChatEvent event) {
        switch (event.getIndex()) {
            case NOTIFICATION_E: {
                appendText(((NotificationEvent) event).getMessage());
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
        userClient = null;
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                close();
            }
        }
        ); 
    }

    @Override
    public void hear(MainClientEvent mce) {
        switch (mce.getIndex()) {
            case CHANGE_LOBBY_NAME_E: {
                setChatTabName(((ChangeLobbyNameEvent) mce).getLobbyName());
                break;
            }
        }
    }

    @Override
    public void hear(SetupEvent se) {
        switch(se.getIndex()) {
            case SET_BOARD_E: {
                final SetBoardEvent sbe = (SetBoardEvent) se;
                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                    @Override
                    public void run() {
                        if (boardFrame == null) {
                            boardFrame = new BoardFrame(ChatPanel.this, 
                                    userClient, lobbyManagementFrame, 
                                 sbe.getSerializableBoard());
                            userClient.addDisconnectListener(boardFrame);
                            userClient.addMainClientListener(boardFrame);
                            boardFrame.open();
                            boardFrame.requestActive();
                        }
                        else {
                            boardFrame.setBoard(sbe.getSerializableBoard());
                        }
                    }
                }
                );
                break;
            }
            case DELETE_BOARD_E: {
                JOptionPane.showMessageDialog(null, "Board was closed!");
                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                    @Override
                    public void run() {
                        boardFrame.close();
                    }
                    });
                break;
            }
        }
    }
    
    /***
     * Sets tab name
     * @param name tab name
     */
    public void setChatTabName(final String name) {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                    @Override
                    public void run() {
                        setName(name);
                    }
                }
                );
    }
    
    /***
     * When user closes board frame
     */
    public void boardFrameClosed() {
        boardFrame = null;
        if (userClient != null) {
            userClient.removeSetupListener(this);
        }
        if (userClient != null) {
            userClient.addSetupEvent(new CloseBoardEvent());
            userClient.removeSetupListener(this);
        }
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
        if (userClient != null) {
            userClient.disconnect("Disconnection by user!");
        }
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
        active = false;
    }
    
    @Override
    public void componentActivated() {
        super.componentActivated();
        counter = 0;
        active = true;
        if (userClient != null) {
            setChatTabName(userClient.getLobbyName());
        }
        typeField.requestFocus();
    }
    
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
}
