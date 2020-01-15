package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import net.miginfocom.swing.MigLayout;
import rslib.cs.common.DataChecking;
import rslib.cs.common.Status;
import rslib.cs.server.admin.AdminLobby;
import rslib.cs.server.admin.AdminServer;
import rslib.cs.server.user.UserServer;
import rslib.cs.server.util.LobbySession;
import rslib.util.DataManagement;

/***
 * Represents a local server and its GUI
 */
public class LocalServerFrame extends JFrame {
    
    /** Link to lobby frame */
    private final NewLobbyFrame lobbyFrame;
    
    /** Link to text area */
    private final JTextArea textArea;
    
    /** Link to type field */
    private final JTextField typeField;
    
    /** Link to server */
    private LocalServer server;

    /***
     * Constructor
     * @param lobbyFrame link to lobby frame 
     */
    public LocalServerFrame(NewLobbyFrame lobbyFrame) {
        if (lobbyFrame == null) {
            throw new IllegalArgumentException("LocalServerFrame: "
                    + "lobbyFrame is null!");
        }
        this.lobbyFrame = lobbyFrame;
        textArea = new JTextArea();
        typeField = new JTextField();
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        // General params
        String result;
        try {
            result = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e) {
            result = "unknown";
        }
        setTitle("Local server. Host: " + result);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new MigLayout("", "center", "center"));

        // Text area
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane pane = new JScrollPane(textArea);
        pane.setPreferredSize(new Dimension(400, 350));
        add(pane, "wrap");
        // Type panel
        JPanel typePanel = new JPanel();
        typePanel.setLayout(new MigLayout("", "center", "center"));

        // Field for typing
        typeField.setPreferredSize(new Dimension(400, typeField.getFont().getSize()));
        typePanel.add(typeField, "wrap");
        typeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendCommand();
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
                sendCommand();
            }
        });
        buttonPanel.add(buttonSend);
        // Button for cleaning
        JButton buttonClear = new JButton("Clear");
        buttonClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        });
        buttonPanel.add(buttonClear);
        
        // Button for closing server
        JButton buttonClose = new JButton("Shutdown server");
        buttonClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (server != null) {
                    server.shutdown();
                }
                lobbyFrame.setServerFrame(null);
                dispatchEvent(new WindowEvent(
                        LocalServerFrame.this, WindowEvent.WINDOW_CLOSING));
            }
        });
        buttonPanel.add(buttonClose);

        typePanel.add(buttonPanel);

        add(typePanel, "wrap");
        JLabel helpLabel = new JLabel("Enter -help to see the list of "
                + "available commands");
        add(helpLabel);
        pack();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dimension.width / 2 + lobbyFrame.getSize().width / 2 + 10, 
                dimension.height / 2 - getSize().height / 2);
        
        try {
            server = new LocalServer();
            server.start();
            textArea.append("Server started successfully!\n");
        }
        catch (IOException e) {
            textArea.append("Failed starting server: " + e.getMessage() + "\n");
        }
    }
    
    /***
     * Sends internal command to server
     */
    private void sendCommand() {
        if (typeField.getText().equals("-help")) {
            textArea.append(AdminLobby.COMMANDS + "\n");
            typeField.setText("");
            return;
        }
        if (server == null || ! server.isWorking()) {
            textArea.append("Server is not working!" + "\n");
            return;
        }
        if (! typeField.getText().equals("")) {
            server.processCommand(typeField.getText());
            typeField.setText("");
        }
    }
    
    
    /***
     * Represents a local server
     */
    class LocalServer extends AdminServer {

        public LocalServer() throws IOException {
            setAllLogsEnabled(false);
        }
        
        /***
         * Appends log message
         * @param message message
         */
        private void appendLog(String message) {
            if (textArea.getLineCount() > 100) {
                try {
                    textArea.getDocument().remove(0, 
                            textArea.getText().indexOf("\n") + 1);
                }
                catch (BadLocationException e) {
                    textArea.setText("");
                }
            }
            textArea.append(message + "\n");
        }

        @Override
        public void foldLog(String message) {
            if (! allLogsEnabled) {
                return;
            }
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            appendLog(dateFormat.format(date)
                    + ": Admin server: " + message);
        }        
        
        /***
         * Processes command from server owner
         * @param command command
         */
        public void processCommand(String command) {
            appendLog(command);
            String[] result = command.split(" ");
            if (result[0].equals("-start") && result.length == 1) {
                if (userServer != null) {
                    appendLog("Server is already working!");
                    return;
                }
                try {
                    userServer = new UserServer(this);
                    userServer.setAdminLobby(adminLobby);
                    setUserServer(userServer);
                    appendLog("Successfull!");
                    userServer.start();
                }
                catch (IOException e) {
                    appendLog("Error while launching user server: " + e.getMessage());
                }
                return;
            }
            if (result[0].equals("-shutdown") && result.length == 1) {
                if (userServer == null) {
                    appendLog("No server is running!");
                    return;
                }
                appendLog("Successfull!");
                userServer.shutdown();
                userServer = null;
                return;
            }
            if (result[0].equals("-shutdownlobby") && result.length == 2) {
                appendLog("Successfull!");
                userServer.shutdownLobbyCommand(result[1]);
                return;
            }
            if (result[0].equals("-lobbies") && result.length == 1) {
                ArrayList<LobbySession> lobbies = userServer.getLobbies();
                if (lobbies.isEmpty()) {
                    appendLog("No active lobbies!");
                    return;
                }
                String info = "";
                for (LobbySession lobbySession : lobbies) {
                    info += lobbySession.toString() + "\n";
                }
                appendLog(info);
                return;
            }
            if (result[0].equals("-changelobbyname") && result.length == 3) {
                if (DataChecking.isLobbyNameValid(result[2])) {
                    userServer.changeLobbyNameCommand(result[1], result[2]);
                }
                else {
                    appendLog("Illegal lobby name!");
                }
                return;
            }
            if (result[0].equals("-changelobbypassword") && result.length == 3) {
                String password = result[2];
                if (DataChecking.isLobbyPasswordValid(password)) {
                    userServer.changeLobbyPasswordCommand(result[1], DataManagement.digest(password));
                }
                else {
                    appendLog("Illegal password!");
                }
                return;
            }
            if (result[0].equals("-deletelobbypassword") && result.length == 2) {
                userServer.changeLobbyPasswordCommand(result[1], DataManagement.digest(null));
                return;
            }
            if (result[0].equals("-changeusername") && result.length == 4) {
                if (DataChecking.isUsernameValid(result[3])) {
                    userServer.changeUsernameCommand(result[1], result[2], result[3]);
                }
                else {
                    appendLog("Illegal username!");
                }
                return;
            }
            if (result[0].equals("-changeuserstatus") && result.length == 4) {
                Status status = null;
                String str = result[3];
                if (str.equals("readonly")) {
                    status = Status.READONLY;
                }
                if (str.equals("common")) {
                    status = Status.COMMON;
                }
                if (str.equals("moderator")) {
                    status = Status.MODERATOR;
                }
                if (str.equals("root")) {
                    status = Status.LOBBY_ROOT;
                }
                if (status == null) {
                    foldLog("Illegal status!");
                    return;
                }
                userServer.changeUserStatusCommand(result[1], result[2], status);
                return;
            }
            if (result[0].equals("-kick") && result.length == 3) {
                userServer.kickUserCommand(result[1], result[2]);
                return;
            }
            if (result[0].equals("-ban") && result.length == 3) {
                userServer.banUserCommand(result[1], result[2]);
                return;
            }
            if (result[0].equals("-unban") && result.length == 3) {
                userServer.unbanUserCommand(result[1], result[2]);
                return;
            }
            if (result[0].equals("-enablelogs") && result.length == 1) {
                appendLog("All logs were enabled!");
                setAllLogsEnabled(true);
                return;
            }
            if (result[0].equals("-disablelogs") && result.length == 1) {
                appendLog("All logs were disabled!");
                setAllLogsEnabled(false);
                return;
            }
            appendLog("Illegal command!");
        }
    }
}
