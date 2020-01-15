package gui.board_frame;

import gui.board_frame.control.ControlPanel;
import gui.LobbyManagementFrame;
import gui.board.BoardPanel;
import gui.board_frame.control.CommandFacade;
import gui.chat.ChatPanel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;
import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.main_client.ChangeLobbyNameEvent;
import rslib.cs.protocol.events.main_client.MainClientEvent;
import static rslib.cs.protocol.events.main_client.MainClientEvent.MainClientEventType.CHANGE_LOBBY_NAME_E;
import rslib.listeners.DisconnectListener;
import rslib.listeners.MainClientListener;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import rslib.gui.board.ExternalizableBoard;

/***
 * Represents main board frame
 */
public class BoardFrame extends TopComponent implements 
        DisconnectListener, MainClientListener {
    
    /** Link to client */
    private UserClient userClient;
    
    /** Link to chat panel */
    private ChatPanel chatPanel;
    
    /** Board parent */
    private JPanel outerBoardPanel;
    
    /** Board itself */
    private BoardPanel board;
    
    /** Board viewport */
    private JViewport viewport;
    
    /** Control panel */
    private ControlPanel controlPanel;
    
    /** Board message panel */
    private BoardMessagePanel bmPanel;

    /***
     * Constructor
     * @param userClient link to client
     * @param managementFrame link to management frame
     * @param eb board
     */
    public BoardFrame(ChatPanel chatPanel, 
            UserClient userClient, LobbyManagementFrame managementFrame, 
            ExternalizableBoard eb) {
        if (chatPanel == null) {
            throw new IllegalArgumentException("BoardFrame: "
                    + "chatPanel is null!");
        }
        if (userClient == null) {
            throw new IllegalArgumentException("BoardFrame: "
                    + "userClient is null!");
        }
        if (managementFrame == null) {
            throw new IllegalArgumentException("BoardFrame: "
                    + "managemenrFrame is null!");
        }
        this.chatPanel = chatPanel;
        this.userClient = userClient;
        initComponents(managementFrame, eb);
    }
    
    /***
     * Initializes GUI components
     * @param managementFrame link to management frame
     * @param eb board
     */
    private void initComponents(LobbyManagementFrame managementFrame,
            ExternalizableBoard eb) {
        
        setName(userClient.getLobbyName());      
        setLayout(new MigLayout("fill", "center", "top"));
        
        // TODO: add menu
        
        // Board panel
        outerBoardPanel = new JPanel();
        outerBoardPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JScrollPane scrollPane = new JScrollPane(outerBoardPanel);
        viewport = scrollPane.getViewport(); 
        
        CommandFacade commandFacade = new CommandFacade(userClient);
        board = new BoardPanel(commandFacade, eb, viewport);
        userClient.addBoardListener(board);
        userClient.addMainClientListener(board);
        
        outerBoardPanel.add(board);
        
        // Control panel
        controlPanel = new ControlPanel(board, commandFacade, 
                userClient);
        userClient.addMainClientListener(controlPanel);
        
        viewport.setViewSize(new Dimension(viewport.getViewSize().width / 2,
            viewport.getViewSize().height / 2));
        
        JPanel outerGeneralPanel = new JPanel();
        outerGeneralPanel.setLayout(new MigLayout("", "center", "top"));
        outerGeneralPanel.add(controlPanel, "wrap");
        int width = controlPanel.getPreferredSize().width;
        String sizeConstr = "" + width + ":" + width + ":" + width;
        outerGeneralPanel.add(scrollPane, 
                new CC().wrap().width(sizeConstr).height("500:500:500"));
        bmPanel = new BoardMessagePanel(width);
        userClient.addBoardMessageListener(bmPanel);
        outerGeneralPanel.add(bmPanel);
        JScrollPane outerScroll = new JScrollPane(outerGeneralPanel);
        outerScroll.setBorder(null);
        add(outerScroll, new CC().grow());
        scrollPane.setMaximumSize(scrollPane.getPreferredSize());
        scrollPane.setMinimumSize(scrollPane.getPreferredSize());
    }

    @Override
    public void hearDisconnection() {
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
        if (mce.getIndex() == CHANGE_LOBBY_NAME_E) {
            setBoardTabName(((ChangeLobbyNameEvent)mce).getLobbyName());
        }
    }
    
    /***
     * Sets tab name
     * @param name tab name
     */
    public void setBoardTabName(final String name) {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                    @Override
                    public void run() {
                        setName(name);
                    }
                }
                );
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
        closeBoardFrame();
    }
    
    /***
     * Closes board frame
     */
    public void closeBoardFrame() {
        board.getAnimationManager().stop();
        userClient.removeBoardListener(board);
        userClient.removeMainClientListener(board);
        userClient.removeBoardMessageListener(bmPanel);
        chatPanel.boardFrameClosed();
    }
    
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    /***
     * Sets new board to the frame
     * @param eb board
     */
    public void setBoard(ExternalizableBoard eb) {
        board.getAnimationManager().stop();
        outerBoardPanel.remove(board);
        userClient.removeBoardListener(board);
        userClient.removeMainClientListener(board);
        controlPanel.getCommandFacade().clear();
        board = new BoardPanel(controlPanel.getCommandFacade(), eb, viewport);
        outerBoardPanel.add(board);
        userClient.addBoardListener(board);
        userClient.addMainClientListener(board);
        controlPanel.setBoard(board);
        outerBoardPanel.revalidate();
    }
}
