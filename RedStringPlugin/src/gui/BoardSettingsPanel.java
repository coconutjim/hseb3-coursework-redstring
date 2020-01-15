package gui;

import gui.board.BoardPanel;
import gui.board_frame.control.CommandFacade;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import rslib.cs.common.Status;

/***
 * Represents board settings panel
 */
public class BoardSettingsPanel extends JPanel {
    
    /** Link to frame */
    private SettingsFrame frame;
    
    /** Link to board */
    private BoardPanel board;
    
    /** Link to command facade */
    private CommandFacade commandFacade;
    
    /** Complex painting check box */
    private JCheckBox complexPaintingCB;
    
    /** Sync mode check box */
    private JCheckBox syncModeCB;

    /***
     * Constructor
     * @param frame link to frame
     * @param board link to board 
     */
    public BoardSettingsPanel(SettingsFrame frame, BoardPanel board) {
        if (frame == null) {
            throw new IllegalArgumentException("BoardSettingPanel:"
                    + "frame is null!");
        }
        if (board == null) {
            throw new IllegalArgumentException("BoardSettingPanel:"
                    + "board is null!");
        }
        this.frame = frame;
        this.board = board;
        this.commandFacade = board.getCommandFacade();
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        setLayout(new MigLayout("", "center", "center"));
        complexPaintingCB = new JCheckBox("Complex container painting");
        add(complexPaintingCB, "span");
        syncModeCB = new JCheckBox("Asynchronous mode");
        add(syncModeCB, "span");
        final JButton buttonOK = new JButton("Apply");
        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean asynchronous = syncModeCB.isSelected();
                if (asynchronous != board.isAsynchronous()) {
                    if (commandFacade.getUserStatus().ordinal() < Status.MODERATOR.ordinal()) {
                        JOptionPane.showMessageDialog(BoardSettingsPanel.this, 
                                "Unable to perform operation: " + 
                            "You have no rigths to change sync mode!");
                        return;
                    }
                    commandFacade.changeSyncMode(board.hashCode(), asynchronous);
                }
                boolean complexPainting = complexPaintingCB.isSelected();
                if (complexPainting != board.isComplexPainting()) {
                    board.setComplexPainting(complexPainting);
                }
                frame.dispose();
            }
        });
        add(buttonOK);            
    }
    
    /***
     * Does correct settings showing
     */
    public void prepareData() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                complexPaintingCB.setSelected(board.isComplexPainting());
                syncModeCB.setSelected(board.isAsynchronous());
            }
        });
    }

    public void setBoard(BoardPanel board) {
        this.board = board;
    }
}
