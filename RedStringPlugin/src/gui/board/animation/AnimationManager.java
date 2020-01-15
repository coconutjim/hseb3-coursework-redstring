package gui.board.animation;

import gui.board.BoardPanel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/***
 * Represents a animation handler
 */
public class AnimationManager implements Runnable {
    
    /** Working flag */
    private boolean working;
    
    /** Link to board */
    private BoardPanel board;
    
    /** List of animations */
    private final List<Animation> animations;

    /***
     * Constructor
     * @param board link to board 
     */
    public AnimationManager(BoardPanel board) {
        if (board == null) {
            throw new IllegalArgumentException("AnimationManager: "
                    + "board is null!");
        }
        this.board = board;
        animations = new CopyOnWriteArrayList<>();
        working = true;
    }

    @Override
    public void run() {
        while (working) {
            if (animations.isEmpty()) {
                try {
                    synchronized(animations) {
                        animations.wait(1000);
                    }
                }
                catch (InterruptedException | IllegalMonitorStateException e) {
                    JOptionPane.showMessageDialog(board, 
                            "Fatal animating error", "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    stop();
                }
            }
            else {
                while (! animations.isEmpty()) {
                    for (Animation animation: animations) {
                        if (! animation.checkIfNew()) {
                            animation.decrement();
                            if (animation.isEnded()) {
                                animation.removeAnimation();
                                animations.remove(animation);
                            }
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            board.repaint();
                        }
                    }); 
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e) {
                       JOptionPane.showMessageDialog(board, 
                            "Fatal animating error", "Error", 
                            JOptionPane.ERROR_MESSAGE);
                       stop(); 
                    }
                }
            }
        }
    }
    
    /***
     * Adds animation
     * @param animation animation 
     */
    public void addAnimation(Animation animation) {
        animations.add(animation);
        try {
            synchronized (animations) {
                animations.notify();
            }
        }
        catch (IllegalMonitorStateException e) {
            JOptionPane.showMessageDialog(board, 
                            "Fatal animating error", "Error", 
                            JOptionPane.ERROR_MESSAGE);
            stop(); 
        }
    }
    
    /***
     * Stops the thread
     */
    public void stop() {
        working = false;
    }
}
