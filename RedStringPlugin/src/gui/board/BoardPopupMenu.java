package gui.board;

import gui.board_frame.control.ColorChooser;
import gui.board_frame.control.CommandFacade;
import gui.parsing.Parsing;
import gui.util.ColorScheme;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import rslib.cs.common.Status;
import rslib.commands.board.ChangeGeneralOpaqueCommand;
import rslib.commands.common.ChangeColorCommand;
import rslib.commands.common.ChangeStatusCommand;

/***
 * Represents a board pop up menu with board options
 */
public class BoardPopupMenu extends JPopupMenu {
    
    /** Link to board */
    private BoardPanel board;
    
    /** Command facade */
    private CommandFacade commandFacade;
    
    private static final String[] statusStrings;
    
    static {
        final Status[] statuses = Status.values();
        statusStrings = new String[statuses.length - 1];
        int i = 0;
        for (Status status : statuses) {
            if (status != Status.ADMINISTRATOR) {
                statusStrings[i ++] = status.toString();
            }
        }
    }
    
    /***
     * Constructor
     * @param board link to board
     * @param container link to container
     */
    public BoardPopupMenu(BoardPanel board) {
        if (board == null) {
            throw new IllegalArgumentException("BoardPopupMenu: "
                    + "board is null!");
        }
        this.board = board;
        commandFacade = board.getCommandFacade();
        initComponents();
    }
    
    /***
     * Initializes GUI
     */
    private void initComponents() {
        // Change board color scheme
        JMenuItem setBoardColorItem = new JMenuItem("Change board color scheme");
        setBoardColorItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color oldForeground = board.getForeground();
                Color oldBackground = board.getBackground();
                ColorScheme scheme = 
                        ColorChooser.getInstance().showDialog(null, 
                                oldForeground, oldBackground);
                if (scheme != null) {
                    Color newForeground = oldForeground.equals(scheme.getForeground()) ?
                            null : scheme.getForeground();
                    Color newBackground = oldBackground.equals(scheme.getBackground()) ?
                            null : scheme.getBackground();
                    if (newForeground != null || newBackground != null) {
                        commandFacade.doCommand(new ChangeColorCommand(board, board, 
                                Parsing.convertToColorModel(newForeground), 
                                Parsing.convertToColorModel(newBackground)), true);
                    }
                }
            }
        });
        this.add(setBoardColorItem);
        
        /* TODO: see
        // Set general container font
        JMenuItem setGeneralFontItem = new JMenuItem("Change general container"
                + " font");
        setGeneralFontItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FontModel newFont = FontChooser.getInstance().
                        showDialog(board, board.getGeneralContainerFont());
                if (newFont != null &&
                        ! newFont.equals(board.getGeneralContainerFont())) {
                    commandFacade.doCommand(new ChangeGeneralFontCommand(
                            board, newFont), true);
                }
            }
        });
        this.add(setGeneralFontItem);
        
        // Set all container transparent
        JMenuItem setAllTransparentItem = new JMenuItem("Set all containers transparent");
        setAllTransparentItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandFacade.doCommand(new ChangeGeneralOpaqueCommand(
                board, false), true);
            }
        });
        this.add(setAllTransparentItem);*/
        
        // Set all container opaque
        JMenuItem setAllOpaqueItem = new JMenuItem("Set all containers opaque");
        setAllOpaqueItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandFacade.doCommand(new ChangeGeneralOpaqueCommand(
                board, true), true);
            }
        });
        this.add(setAllOpaqueItem);
        
        // Set general container background
        /*JMenuItem setGeneralFGItem = new JMenuItem("Change general container"
                + " foreground");
        setGeneralFGItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color old = Parsing.createColor(
                        board.getGeneralContainerForeground());
                Color result = JColorChooser.showDialog(board,
                        "Choose color", old);
                if (result != null && ! old.equals(result)) {
                    commandFacade.doCommand(new ChangeGeneralColorCommand(board, 
                            Parsing.convertToColorModel(result), null), true);
                }
            }
        });
        this.add(setGeneralFGItem);

        // Set general container background
        JMenuItem setGeneralBGItem = new JMenuItem("Change general container"
                + " background");
        setGeneralBGItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color old = Parsing.createColor(
                        board.getGeneralContainerBackground());
                Color result = JColorChooser.showDialog(board,
                        "Choose color", old);
                if (result != null && ! old.equals(result)) {
                    commandFacade.doCommand(new ChangeGeneralColorCommand(board, 
                            null, Parsing.convertToColorModel(result)), true);
                }
            }
        });
        this.add(setGeneralBGItem);
        */
              
        // Change status button
        JMenuItem changeStatusItem = new JMenuItem("Change status");
        changeStatusItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String string = (String) JOptionPane.showInputDialog(board,
                        "Choose new container status",
                        "Changing container status", JOptionPane.QUESTION_MESSAGE, 
                        null, statusStrings, statusStrings[0]);
                if (string != null) {
                    Status newStatus = null;
                    for (Status status : Status.values()) {
                        if (status.toString().equals(string)) {
                            newStatus = status;
                        }
                }
                if (newStatus == null) {
                    return;
                }
                    if (newStatus.equals(board.getComponentStatus())) {
                        return;
                    }
                    commandFacade.doCommand(
                            new ChangeStatusCommand(board, board, newStatus), 
                            false);
                }
            }
        });
        this.add(changeStatusItem);
    }
    
    /***
     * Creates pop up menu
     * @return pop up menu
     */
    public MouseAdapter createPopUpMenu() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                pop(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                pop(e);
            }

            private void pop(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3 
                        && e.isPopupTrigger()) {
                    show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
    }
}
