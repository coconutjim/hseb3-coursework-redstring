package gui.board_frame.control;

import gui.util.Images;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import rslib.cs.client.user.UserClient;
import rslib.cs.common.Status;
import rslib.cs.common.User;
import rslib.cs.protocol.events.board.PointEvent;
import rslib.cs.protocol.events.board.board.ChangeSyncModeEvent;
import rslib.cs.protocol.events.board.board.SendHashEvent;
import rslib.commands.Command;
import rslib.commands.UndoRedo;

/** Represents a panel that controls UndoRedo facility */
public class CommandFacade {
        
    /** Link to UndoRedo facility */    
    private UndoRedo undoRedo;
    
    /** Link to client */
    private UserClient userClient;
    
    /** Undo button */
    private JButton undoButton;
    
    /** Redo button */
    private JButton redoButton;

    /***
    * Constructor
    * @param userClient link to client
    */
    public CommandFacade(UserClient userClient) {
        if (userClient  == null) {
            throw new IllegalArgumentException("UndoRedoPanel: "
                    + "userClient is null!");
            }
        undoRedo = new UndoRedo(userClient);
        this.userClient = userClient;
        initComponents();    
    }
    
    /***
    * Initializes GUI components
    */
    private void initComponents() {
        
        // Button undo
        undoButton = new JButton();
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undo();
            } 
        });
        undoButton.setPreferredSize(ControlPanel.CONTROL_BUTTON_DIMENSION);
        undoButton.setIcon(Images.UNDO_ICON);
        undoButton.setToolTipText("Undo");
        undoButton.setEnabled(false);
  
        // Button redo
        redoButton = new JButton();
        redoButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    redo();
                }                
            });
        redoButton.setPreferredSize(ControlPanel.CONTROL_BUTTON_DIMENSION);
        redoButton.setIcon(Images.REDO_ICON);
        redoButton.setToolTipText("Redo");
        redoButton.setEnabled(false);
    }    
    
    /***
     * Undoes the command
     */
    private void undo() {
        try {
            undoRedo.undo(true);
            if (undoRedo.canRedo()) {
                if (! redoButton.isEnabled()) {
                    redoButton.setEnabled(true);
                }
            }
        }
        catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(null, "Unable to perform operation: " +
                    e.getMessage());
        }
        if (! undoRedo.canUndo()) {
            if (undoButton.isEnabled()) {
                undoButton.setEnabled(false);
            }
        }
    }
    
    /***
     * Redoes the command
     */
    private void redo() {
        try {
            undoRedo.redo();
            if (undoRedo.canUndo()) {
                if (! undoButton.isEnabled()) {
                    undoButton.setEnabled(true);
                }
            }
        }
        catch(IllegalStateException e) {
            JOptionPane.showMessageDialog(null, "Unable to perform operation: " + 
                    e.getMessage());
        }
        if (! undoRedo.canRedo()) {
            if (redoButton.isEnabled()) {
                redoButton.setEnabled(false);
            }
        }
    }
    
    /***
     * Does the command
     * @param command command to do
     * @param undoable if the command can be undone
     * @return if operation succeeded
     */
    public boolean doCommand(Command command, boolean undoable) {
        try {
            undoRedo.did(command, undoable);
            if (undoRedo.canUndo()) {
                if (! undoButton.isEnabled()) {
                    undoButton.setEnabled(true);
                }
            }
            if (redoButton.isEnabled()) {
                redoButton.setEnabled(false);
            }
        }
        catch(IllegalStateException e) {
            JOptionPane.showMessageDialog(null, "Unable to perform operation: " + 
                    e.getMessage());
            return false;
        }
        return true;
    }
    
    /***
     * Clear undo redo history 
     */
    public void clear() {
        undoRedo.clear();
        if (redoButton.isEnabled()) {
            redoButton.setEnabled(false);
        }
        if (undoButton.isEnabled()) {
            undoButton.setEnabled(false);
        }
    }
    
    /***
     * Sends hash to check if board is valid
     * @param hash board hash
     */
    public void sendHash(int hash) {
        userClient.addBoardEvent(new SendHashEvent(hash));
    }
    
    /***
     * Changes board sync mode
     * @param hash board hash
     * @param asynchronous mode
     */
    public void changeSyncMode(int hash, boolean asynchronous) {
        userClient.addBoardEvent(new ChangeSyncModeEvent(hash, asynchronous));
    }
    
    /***
     * Points the part of the board
     * @param hash board hash
     * @param x absolute x-coordinate
     * @param y absolute y-coordinate
     * @param username point author
     */
    public void point(int hash, int x, int y, String username) {
        userClient.addBoardEvent(new PointEvent(hash, x, y, username));
    }
    
    /***
     * Deletes board
     */
    public void deleteBoard() {
        userClient.deleteBoard();
    }
    
    /***
     * Gets username
     * @return username
     */
    public String getUsername() {
        return userClient.getUsername();
    }
    
    /***
     * Gets user status
     * @return user status
     */
    public Status getUserStatus() {
        return userClient.getUserStatus();
    }
    
    /***
     * Returns array of users except the current user
     * @return array of users except the current user
     */
    public String[] getRestUsers() {
        Map<User, String> userInfo = userClient.getUsers();
        Set<User> users = userInfo.keySet();
        String[] result = new String[users.size()];
        int i = 0;
        for (User user: users) {
            result[i ++] = user.getUsername();
        }
        return result;
    }

    public JButton getUndoButton() {
        return undoButton;
    }

    public JButton getRedoButton() {
        return redoButton;
    }
}