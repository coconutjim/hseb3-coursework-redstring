package gui.board_frame.control;

import gui.SettingsFrame;
import gui.board.BoardPanel;
import gui.util.Images;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import javax.swing.JButton; 
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.filechooser.FileFilter;
import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;
import rslib.cs.client.user.UserClient;
import rslib.cs.protocol.events.main_client.MainClientEvent;
import rslib.commands.board.ClearBoardCommand;
import rslib.commands.board.SetBoardCommand;
import rslib.commands.common.ResizeCommand;
import rslib.gui.board.ExternalizableBoard;
import rslib.gui.container.BoardContainer;
import rslib.listeners.MainClientListener;

/***
 * Represents main control buttons
 */
public class ControlPanel extends JPanel implements MainClientListener {
    
    /** Flavor for container creating */
    public static DataFlavor DNDCONTANER_FLAVOR;
    
    /** Button size */
    public static final Dimension CONTROL_BUTTON_DIMENSION 
            = new Dimension(50, 50);
    
    /** Link to board */
    private BoardPanel board;
    
    /** File chooser */
    private JFileChooser fileChooser;
    
    /** Command facade */
    private CommandFacade commandFacade;
    
    /** Label with user information */
    private JLabel userLabel;
    
    /** Settings frame */
    private SettingsFrame settingsFrame;
    
    /** Buttons except the point mode button */
    private ArrayList<JComponent> components;
    
    static {
        try {
            DNDCONTANER_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType 
                    + ";class=\"" + BoardContainer.ContainerType.class.getName() + "\"");
        }
        catch (ClassNotFoundException e) {
            //TODO: ????
        }
    }
    
    /***
     * Constructor
     * @param board link to board
     * @param commandFacade link to command facade
     * @param userClient link to client
     */
    public ControlPanel(BoardPanel board, CommandFacade commandFacade,
            UserClient userClient) {
        if (board == null) {
            throw new IllegalArgumentException("ControlPanel: board is null!");
        }
        if (commandFacade == null) {
            throw new IllegalArgumentException("ControlPanel: commandFacade is null!");
        }
        this.board = board;
        this.commandFacade = commandFacade;
        settingsFrame = new SettingsFrame(userClient, board);
        userClient.addMainClientListener(settingsFrame);
        userClient.addDisconnectListener(settingsFrame);
        components = new ArrayList<>();
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public String getDescription() {
                return "Interactive boards (*.brd)";
            }

            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                } else {
                    return file.getName().toLowerCase().endsWith(".brd");
                }
            }
        });
        
        setLayout(new MigLayout("", "center", "center"));
        int width = CONTROL_BUTTON_DIMENSION.width;
        int height = CONTROL_BUTTON_DIMENSION.height;
        CC buttonsSettings = new CC()
                .width("" + width + ":" + width + ":" + width)
                .height("" + height + ":" + height + ":" + height);
        
        // Button open board
        JButton buttonOpen = new JButton();
        buttonOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setSelectedFile(new File(""));
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (! file.getName().toLowerCase().endsWith(".brd")) {
                        file = new File(file.getAbsolutePath() + ".brd");
                    }
                    ExternalizableBoard newBoard = readBoardFromFile(file);
                    if (newBoard != null) {
                        commandFacade.doCommand(new SetBoardCommand(board, 
                                newBoard), true);
                    }
                }
                fileChooser.setSelectedFile(null);
            }
        });
        buttonOpen.setPreferredSize(CONTROL_BUTTON_DIMENSION);
        buttonOpen.setIcon(Images.OPEN_ICON);
        buttonOpen.setToolTipText("Open new board");
        add(buttonOpen, buttonsSettings);
        components.add(buttonOpen);
        
        // Button save board
        JButton buttonSave = new JButton();
        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setSelectedFile(new File(""));
                int result = fileChooser.showSaveDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (! file.getName().toLowerCase().endsWith(".brd")) {
                        file = new File(file.getAbsolutePath() + ".brd");
                    }
                    if (writeBoardToFile(file)) {
                        JOptionPane.showMessageDialog(null, 
                                "Board was saved successfully!", "Info", 
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                fileChooser.setSelectedFile(null);
            }
        });
        buttonSave.setPreferredSize(CONTROL_BUTTON_DIMENSION);
        buttonSave.setIcon(Images.SAVE_ICON);
        buttonSave.setToolTipText("Save board");
        add(buttonSave, buttonsSettings);
        components.add(buttonSave);
        
        add(commandFacade.getUndoButton(), buttonsSettings);
        components.add(commandFacade.getUndoButton());
        add(commandFacade.getRedoButton(), buttonsSettings);
        components.add(commandFacade.getRedoButton());
        //add(new JSeparator(SwingConstants.VERTICAL));
        //add(new JSeparator(SwingConstants.VERTICAL));
        //add(new JSeparator(SwingConstants.VERTICAL));
        //add(new JSeparator(SwingConstants.VERTICAL));
        //add(new JSeparator(SwingConstants.VERTICAL));
        
        // Button extend
        JButton buttonExtend = new JButton();
        buttonExtend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int width = board.getComponentWidth();
                int height = board.getComponentHeight();
                int maxWidth = board.getComponentMaximumWidth();
                int maxHeight = board.getComponentMaximumHeight();
                if (width == maxWidth && height == maxHeight) {
                    JOptionPane.showMessageDialog(null, 
                            "You can not extend board more!");
                    return;
                }
                width = Math.min(width * 2, maxWidth);
                height = Math.min(height  *2, maxHeight);
                commandFacade.doCommand(new ResizeCommand(board, board,
                        board.getComponentLeft(), board.getComponentTop(),
                        width, height, false), true);
            }
        });
        buttonExtend.setPreferredSize(CONTROL_BUTTON_DIMENSION);
        buttonExtend.setIcon(Images.EXTEND_ICON);
        buttonExtend.setToolTipText("Extend board");
        add(buttonExtend, buttonsSettings);
        components.add(buttonExtend);
        
        // Button shrink
        JButton buttonShrink = new JButton();
        buttonShrink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int width = board.getComponentWidth();
                int height = board.getComponentHeight();
                Dimension minimums = board.calculateShrunkBoard();
                int minWidth = minimums.width;
                int minHeight = minimums.height;
                if (width == minWidth && height == minHeight) {
                    JOptionPane.showMessageDialog(null, 
                            "You can not shrink board more!");
                    return;
                }
                width = Math.max(width / 2, minWidth);
                height = Math.max(height / 2, minHeight);
                commandFacade.doCommand(new ResizeCommand(board, board,
                        board.getComponentLeft(), board.getComponentTop(),
                        width, height, false), true);
            }
        });
        buttonShrink.setPreferredSize(CONTROL_BUTTON_DIMENSION);
        buttonShrink.setIcon(Images.SHRINK_ICON);
        buttonShrink.setToolTipText("Shrink board");
        add(buttonShrink, buttonsSettings);
        components.add(buttonShrink);
        
        // Button clear
        JButton buttonClear = new JButton();
        buttonClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandFacade.doCommand(new ClearBoardCommand(board), true);
            }
        });
        buttonClear.setPreferredSize(CONTROL_BUTTON_DIMENSION);
        buttonClear.setIcon(Images.CLEAR_ICON);
        buttonClear.setToolTipText("Clear board");
        add(buttonClear, buttonsSettings);
        components.add(buttonClear);
        
        //add(new JSeparator(SwingConstants.VERTICAL));
        //add(new JSeparator(SwingConstants.VERTICAL));
        //add(new JSeparator(SwingConstants.VERTICAL));
        //add(new JSeparator(SwingConstants.VERTICAL));
        //add(new JSeparator(SwingConstants.VERTICAL));
        
        // Button point mode
        final JToggleButton buttonPointMode = new JToggleButton();
        buttonPointMode.setToolTipText("Enable/Disable point mode");
        buttonPointMode.setIcon(Images.POINTER_ICON);
        buttonPointMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = buttonPointMode.isSelected();
                board.setPointMode(enabled);
            }
        });
        buttonPointMode.setPreferredSize(CONTROL_BUTTON_DIMENSION);
        add(buttonPointMode, buttonsSettings);
        
        // Button add text container
        DNDContainer buttonTextC = new DNDContainer(
                BoardContainer.ContainerType.TEXT_CONTAINER);
        buttonTextC.setToolTipText("<html>Add new text container<br>"
                + "Drag and drop it to the board</html>");
        add(buttonTextC, buttonsSettings);
        components.add(buttonTextC);
        
        // Button add image container
        DNDContainer buttonImageC = new DNDContainer(
                BoardContainer.ContainerType.IMAGE_CONTAINER);
        buttonImageC.setToolTipText("<html>Add new image container<br>"
                + "Drag and drop it to the board</html>");
        add(buttonImageC, buttonsSettings);
        components.add(buttonImageC);
        
        // Button add file container
        DNDContainer buttonFileC = new DNDContainer(
                BoardContainer.ContainerType.FILE_CONTAINER);
        buttonImageC.setToolTipText("<html>Add new file container<br>"
                + "Drag and drop it to the board</html>");
        add(buttonFileC, buttonsSettings);
        components.add(buttonFileC);
        
        // Label with user data
        userLabel = new JLabel(commandFacade.getUsername());
        userLabel.setLayout(new MigLayout("", "center", "top"));
        userLabel.setIcon(Images.STATUS_ICONS.get(commandFacade.getUserStatus()));
        add(userLabel);
              
        // Button settings
        JButton buttonSettings = new JButton();
        buttonSettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                settingsFrame.showFrame();
            }
        });
        buttonSettings.setIcon(Images.SETTINGS_ICON);
        buttonSettings.setPreferredSize(CONTROL_BUTTON_DIMENSION);
        buttonSettings.setToolTipText("Lobby settings");
        add(buttonSettings, buttonsSettings);
        components.add(buttonSettings);
        
        // Button check validance
        JButton buttonHash = new JButton();
        buttonHash.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandFacade.sendHash(board.hashCode());
            }
        });
        buttonHash.setIcon(Images.SYNC_ICON);
        buttonHash.setPreferredSize(CONTROL_BUTTON_DIMENSION);
        buttonHash.setToolTipText("Check board validance");
        add(buttonHash, buttonsSettings);
        components.add(buttonHash);
        
        // Button delete board
        JButton buttonDelete = new JButton();
        buttonDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandFacade.deleteBoard();
            }
        });
        buttonDelete.setIcon(Images.DELETE_ICON);
        buttonDelete.setPreferredSize(CONTROL_BUTTON_DIMENSION);
        buttonDelete.setToolTipText("Delete board");
        add(buttonDelete, buttonsSettings);
        components.add(buttonDelete);
    }
    
    /***
     * Sets components enabled or disabled while point mode
     * @param enabled enabled/disabled
     */
    private void setComponentsEnabled(boolean enabled) {
        for (JComponent component : components) {
            component.setEnabled(enabled);
        }
    }

    @Override
    public void hear(MainClientEvent mce) {
        switch(mce.getIndex()) {
            case CHANGE_USERNAME_E: {
                userLabel.setText(commandFacade.getUsername());
                break;
            }
            case CHANGE_USER_STATUS_E: {
                userLabel.setIcon(Images.STATUS_ICONS.get(commandFacade.getUserStatus()));
                break;
            }
        }
    }   
    
    /***
     * Writes board to file
     * @param file file
     * @return if board was saved successfully
     */
    private boolean writeBoardToFile(File file) {
        ObjectOutputStream oos = null;
        FileOutputStream fout;
        boolean success = false;
        try {
            fout = new FileOutputStream(file, false);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(board.toExternalizable());
            success = true;
        } 
        catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error while saving board!", 
                    "Error", JOptionPane.ERROR);
        }
        finally {
            if (oos != null) {
                try {
                    oos.close();
                }
                catch (IOException ex1) {
                    
                }
            } 
        }
        return success;
    }
    
    /***
     * Reads board from file
     * @param file file
     * @return board or null in case of error
     */
    private ExternalizableBoard readBoardFromFile(File file) {
        ObjectInputStream ois = null;
        FileInputStream fin;
        ExternalizableBoard newBoard = null;
        try {
            fin = new FileInputStream(file);
            ois = new ObjectInputStream(fin);
            newBoard = (ExternalizableBoard) ois.readObject();
        } 
        catch (IOException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Error while opening board!", 
                    "Error", JOptionPane.ERROR);
        }
        finally {
            if (ois != null) {
                try {
                    ois.close();
                }
                catch (IOException ex1) {
                    
                }
            } 
        }
        return newBoard;
    }

    public void setBoard(BoardPanel board) {
        this.board = board;
        settingsFrame.setBoard(board);
    }

    /***
     * Clears command facade
     */
    public void clearCommandFacade() {
        commandFacade.clear();
    }

    public CommandFacade getCommandFacade() {
        return commandFacade;
    }
}
