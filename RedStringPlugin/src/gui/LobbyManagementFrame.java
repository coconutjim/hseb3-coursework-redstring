package gui;

import rslib.cs.client.user.UserClient;
import rslib.cs.common.Status;
import rslib.cs.protocol.events.main_client.*;
import rslib.listeners.DisconnectListener;
import rslib.listeners.MainClientListener;

import javax.swing.*;
import java.awt.*;
import static rslib.cs.protocol.events.main_client.MainClientEvent.MainClientEventType.CHANGE_LOBBY_NAME_E;

/***
 * Represents a frame with chat administrative functions
 */
public class LobbyManagementFrame extends JFrame implements DisconnectListener, 
        MainClientListener {
    
    /** Link to panel */
    private LobbyManagementPanel panel;
    
    /** Link to client */
    private UserClient userClient;

    /***
     * Constructor
     * @param userClient link to client
     */
    public LobbyManagementFrame(UserClient userClient) {
        if (userClient == null) {
            throw new IllegalArgumentException("LobbyManagementFrame: "
                    + "userClient is null!");
        }
        panel = new LobbyManagementPanel(userClient);
        userClient.addMainClientListener(panel);
        this.userClient = userClient;
        initComponents();
    }
    
    /**
     * Initializes the GUI components
     * @param userClient link to client
     */
    private void initComponents() {
        setTitle("Management: Lobby " + userClient.getLobbyName());       
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        add(panel);
        pack();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dimension.width / 2 - getSize().width / 2, 
                        dimension.height / 2 - getSize().height / 2);
    }
    
    @Override
    public void hear(final MainClientEvent event) {
        if (event.getIndex() == CHANGE_LOBBY_NAME_E) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setTitle("Management: Lobby " + 
                        ((ChangeLobbyNameEvent) event).getLobbyName());
                }
            });
        }
    }
    
    @Override
    public void hearDisconnection() {
        userClient.removeDisconnectListener(this);
        userClient.removeMainClientListener(this);
        userClient.removeMainClientListener(panel);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dispose();
            }
        });
    }
    
    /***
     * Shows management frame
     */
    public void showFrame() {
        userClient.getUserListRequest();
        if (userClient.getUserStatus().ordinal() >= 
                Status.MODERATOR.ordinal()) {
            userClient.getBanListRequest();
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
            }
        });
    }
}
