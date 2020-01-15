package gui;

import rslib.cs.client.user.UserClient;
import rslib.cs.common.LobbyInfo;
import rslib.cs.protocol.events.chat.MessageEvent;
import rslib.cs.protocol.events.message.ShowMessageEvent;
import rslib.listeners.DisconnectListener;
import rslib.listeners.MessageListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/***
 * Represents spam bot
 */
public class SpamBotGUI extends JFrame implements Runnable, DisconnectListener, MessageListener {

    /** Constants */
    private static final String USERNAME = "SpamBot";
    private static final LobbyInfo LOBBY_INFO = new LobbyInfo("testlobby", false, null);
    private static String MESSAGE_SENT = "Message sent: ";
    private static String TIMEOUT = "Timeout: ";

    /** Associated client */
    private UserClient userClient;

    /** Message timeout */
    private long timeout;

    /** Message counter */
    private int counter;

    /** Info counterLabel */
    private JLabel counterLabel;

    /***
     * Constructor
     */
    public SpamBotGUI() {
        counter = 0;
        initComponents();
    }

    /***
     * Initializes GUI components
     */
    private void initComponents() {
        // General params
        setTitle("Spam bot");
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLayout(new MigLayout("center", "center"));

        // Counter label
        counterLabel = new JLabel(MESSAGE_SENT + counter);
        add(counterLabel, "wrap");

        // Timeout label
        timeout = 100;
        final JLabel timeoutLabel = new JLabel(TIMEOUT + timeout);
        add(timeoutLabel, "wrap");

        JPanel timeouts = new JPanel();

        // Timeout spinner
        final SpinnerNumberModel model = new SpinnerNumberModel(timeout, 0, 1000, 10);
        final JSpinner spinner = new JSpinner(model);
        timeouts.add(spinner);

        // Timeout button
        JButton setTimeout = new JButton("Set timeout");
        setTimeout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeout = (int)((double)model.getNumber());
                timeoutLabel.setText(TIMEOUT + timeout);
            }
        });
        timeouts.add(setTimeout);

        add(timeouts, "wrap");

        JPanel buttons = new JPanel();

        // Start button
        JButton start = new JButton("Start");
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(SpamBotGUI.this).start();
            }
        });
        buttons.add(start);

        // Stop button
        JButton stop = new JButton("Stop");
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (userClient != null) {
                    userClient.disconnect("Disconnection by user!");
                }
            }
        });
        buttons.add(stop);

        add(buttons);

        pack();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dimension.width / 2 - getSize().width / 2, dimension.height / 2 - getSize().height);
        setVisible(true);
    }

    /***
     * Main processing
     */
    @Override
    public void run() {
        if (userClient != null) {
            userClient.disconnect("");
        }
        counter = 0;
        userClient = UserClient.login(USERNAME, LOBBY_INFO, this, null);
        if (userClient != null) {
            String message = JOptionPane.showInputDialog(null, "Enter spam message");
            if (message == null || message.equals("")) {
                userClient.disconnect("");
                return;
            }

            // go
            while (userClient.isConnected()) {
                userClient.addChatEvent(new MessageEvent(userClient.getUsername(), message));
                counterLabel.setText(MESSAGE_SENT + ++counter);
                try {
                    Thread.sleep(timeout);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void hearDisconnection() {
        userClient = null;
    }

    @Override
    public void hear(ShowMessageEvent event) {
        JOptionPane.showMessageDialog(null, event.getMessage(),
                "Server information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SpamBotGUI();
            }
        });
    }
}
