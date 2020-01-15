package gui.container;

import gui.board.BoardPanel;
import gui.board_frame.control.ColorChooser;
import gui.board_frame.control.CommandFacade;
import gui.board_frame.control.FontChooser;
import gui.parsing.Parsing;
import gui.util.ColorScheme;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import rslib.cs.common.Status;
import rslib.commands.common.ChangeColorCommand;
import rslib.commands.common.ChangeFontCommand;
import rslib.commands.common.ChangeNameCommand;
import rslib.commands.common.ChangeOpaqueCommand;
import rslib.commands.common.ChangeOwnerCommand;
import rslib.commands.common.ChangeStatusCommand;
import rslib.commands.container.ChangeContainerLayerCommand;
import rslib.commands.container.ClearContainerCommand;
import rslib.commands.container.DeleteContainerCommand;
import rslib.gui.container.BoardContainer;
import rslib.gui.style.FontModel;

/***
 * Represents a container pop up menu with container options
 */
public class ContainerPopupMenu extends JPopupMenu {
    
    /** Link to board */
    private BoardPanel board;
    
    /** Command facade */
    private CommandFacade commandFacade;
    
    /** Link to this container */
    private ContainerPanel container;
    
    /** String statuses */
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
    public ContainerPopupMenu(BoardPanel board, ContainerPanel container) {
        if (board == null) {
            throw new IllegalArgumentException("ContainerPopupMenu: "
                    + "board is null!");
        }
        if (container == null) {
            throw new IllegalArgumentException("ContainerPopupMenu: "
                    + "container is null!");
        }
        this.board = board;
        commandFacade = board.getCommandFacade();
        this.container = container;
        initComponents();
    }
    
    /***
     * Initializes GUI
     */
    private void initComponents() {        
        // Set name button
        JMenuItem setNameItem = new JMenuItem("Set name");
        setNameItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newName = JOptionPane.showInputDialog(container, 
                        "Enter name:");
                if (newName != null) {
                    commandFacade.doCommand(new ChangeNameCommand(
                            board, container, newName), true);
                }
            }
        });
        add(setNameItem);
        
        // Move container to front layer button
        JMenuItem toFrontItem = new JMenuItem("To front");
        toFrontItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandFacade.doCommand(
                        new ChangeContainerLayerCommand(board, 
                                container, BoardContainer.TO_FRONT), true);
            }
        });
        add(toFrontItem);
        
        // Move container to front layer button
        JMenuItem toBGItem = new JMenuItem("To background");
        toBGItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandFacade.doCommand(
                        new ChangeContainerLayerCommand(board, 
                                container, BoardContainer.TO_BACKGROUND), true);
            }
        });
        add(toBGItem);
        
        // Set container font button
        JMenuItem setFontItem = new JMenuItem("Change font");
        setFontItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FontModel newFont = FontChooser.getInstance().
                        showDialog(container, container.getComponentFont());
                if (newFont != null &&
                        ! newFont.equals(container.getComponentFont())) {
                    commandFacade.doCommand(new ChangeFontCommand(board, 
                            container, newFont), true);
                }
            }
        });
        add(setFontItem);
        
        // Change container color scheme
        JMenuItem setBoardColorItem = new JMenuItem("Change color scheme");
        setBoardColorItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color oldForeground = container.getForeground();
                Color oldBackground = container.getBackground();
                ColorScheme scheme = 
                        ColorChooser.getInstance().showDialog(container, 
                                oldForeground, oldBackground);
                if (scheme != null) {
                    Color newForeground = oldForeground.equals(scheme.getForeground()) ?
                            null : scheme.getForeground();
                    Color newBackground = oldBackground.equals(scheme.getBackground()) ?
                            null : scheme.getBackground();
                    if (newForeground != null || newBackground != null) {
                        commandFacade.doCommand(new ChangeColorCommand(board, container, 
                                Parsing.convertToColorModel(newForeground), 
                                Parsing.convertToColorModel(newBackground)), true);
                    }
                }
            }
        });
        this.add(setBoardColorItem);
        
        // Set transparent button
        JMenuItem setTransparentItem = new JMenuItem("Set transparent");
        setTransparentItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandFacade.doCommand(
                        new ChangeOpaqueCommand(board, container, false), true);
            }
        });
        add(setTransparentItem);
        
        // Set opaque button
        JMenuItem setOpaqueItem = new JMenuItem("Set opaque");
        setOpaqueItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandFacade.doCommand(
                        new ChangeOpaqueCommand(board, container, true), true);
            }
        });
        add(setOpaqueItem);
        
        // Clear container button
        JMenuItem clearItem = new JMenuItem("Clear");
        clearItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandFacade.doCommand(
                        new ClearContainerCommand(board, container), true);
            }
        });
        add(clearItem);
        
        // Change owner button
        JMenuItem changeOwnerItem = new JMenuItem("Change owner");
        changeOwnerItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (container.isBlocked()) {
                    JOptionPane.showMessageDialog(container, 
                            "You can not change container owner while it is blocked!", 
                            "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String[] users = commandFacade.getRestUsers();
                String username = (String) JOptionPane.showInputDialog(container,
                        "Choose new container owner",
                        "Changing container owner", JOptionPane.QUESTION_MESSAGE, 
                        null, users, users[0]);
                if (username != null) {
                    if (username.equals(container.getComponentOwner())) {
                        return;
                    }
                    commandFacade.doCommand(
                            new ChangeOwnerCommand(board, container, username), 
                            false);
                }
            }
        });
        add(changeOwnerItem);
        
        // Change status button
        JMenuItem changeStatusItem = new JMenuItem("Change status");
        changeStatusItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String string = (String) JOptionPane.showInputDialog(container,
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
                    if (newStatus.equals(container.getComponentStatus())) {
                        return;
                    }
                    commandFacade.doCommand(
                            new ChangeStatusCommand(board, container, newStatus), 
                            false);
                }
            }
        });
        add(changeStatusItem);
        
        // Delete container button
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandFacade.doCommand(
                        new DeleteContainerCommand(board, container), true);
            }
        });
        add(deleteItem);
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
