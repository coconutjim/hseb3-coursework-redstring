package gui.board.animation;

import gui.board.BoardPanel;
import gui.board_frame.control.CommandFacade;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

/***
 * Represents a point catcher
 */
public class PointPanel extends JPanel {
    
    /** Link to board */
    private final BoardPanel board;
    
    /** Link to command facade */
    private final CommandFacade commandFacade;
    
    /** Source cursor */
    private Cursor sourceCursor;

    /***
     * Constructor
     * @param board link to board 
     */
    public PointPanel(BoardPanel board) {
        if (board == null) {
            throw new IllegalArgumentException("PointPanel: "
                    + "board is null!");
        }
        this.board = board;
        commandFacade = board.getCommandFacade();
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        setOpaque(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                int x = e.getX();
                int y = e.getY();
                String username = commandFacade.getUsername();
                commandFacade.point(board.hashCode(), x, y, username);
            }            
        });
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ToolTipManager.sharedInstance().registerComponent(this);
    }
    
    @Override
    public String getToolTipText() {
        return "Layer: " + board.getLayer(this);
    }  

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
    } 
}
