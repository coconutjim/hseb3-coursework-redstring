package gui;

import gui.board.BoardPanel;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import rslib.cs.client.user.UserClient;
import rslib.cs.common.Status;
import rslib.cs.protocol.events.main_client.ChangeLobbyNameEvent;
import rslib.cs.protocol.events.main_client.MainClientEvent;
import static rslib.cs.protocol.events.main_client.MainClientEvent.MainClientEventType.CHANGE_LOBBY_NAME_E;
import rslib.listeners.DisconnectListener;
import rslib.listeners.MainClientListener;

/***
 * Represents a frame with board settings
 */
public class SettingsFrame extends JFrame implements MainClientListener,
        DisconnectListener {
    
    /** Link to client */
    private UserClient userClient;
    
    /** Lobby settings */
    private LobbyManagementPanel lmp;
    
    /** Link to board */
    private BoardPanel board;
    
    /** Board settings */
    private BoardSettingsPanel bsp;

    /***
     * Constructor
     * @param userClient link to client
     * @param board link to board
     */
    public SettingsFrame(UserClient userClient, BoardPanel board) {
        if (userClient == null) {
            throw new IllegalArgumentException("SettingsFrame: "
                    + "userClient is null!");
        }
        if (board == null) {
            throw new IllegalArgumentException("SettingsFrame: "
                    + "board is null!");
        }
        this.userClient = userClient;
        this.board = board;
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        setTitle("Settings: Lobby " + userClient.getLobbyName());  
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        JTabbedPane tabs = new JTabbedPane();
        lmp = new LobbyManagementPanel(userClient);
        userClient.addMainClientListener(lmp);
        tabs.addTab("Lobby", lmp);
        bsp = new BoardSettingsPanel(this, board);
        tabs.addTab("Board", bsp);
        
        add(tabs);
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
                    setTitle("Settings: Lobby " + 
                        ((ChangeLobbyNameEvent) event).getLobbyName());
                }
            });
        }
    }
    
    @Override
    public void hearDisconnection() {
        userClient.removeDisconnectListener(this);
        userClient.removeMainClientListener(this);
        userClient.removeMainClientListener(lmp);
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
        bsp.prepareData();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
            }
        });
    }

    public void setBoard(BoardPanel board) {
        this.board = board;
        bsp.setBoard(board);
    }
}
