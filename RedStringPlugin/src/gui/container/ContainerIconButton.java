package gui.container;

import gui.board.BoardPanel;
import gui.board.animation.Animatable;
import gui.board.animation.Animation;
import gui.board_frame.control.CommandFacade;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JButton;

/***
 * Represents a button on the container that does some actions
 */
public abstract class ContainerIconButton extends JButton 
        implements Animatable {
    
    /** Link to container */
    protected final ContainerPanel container;
    
    /** Link to command facade */
    protected CommandFacade commandFacade;
    
    /** Link to board */
    protected BoardPanel board;
    
    /** Dimension */
    public static final Dimension CONTAINER_BUT_DIM = new Dimension(24, 24);
    
    /** Icon */
    protected BufferedImage icon;
    
    /** Animations */
    private List<Animation> animations;
    
    /***
     * Constructor
     * @param link to board
     * @param container link to container 
     */
    public ContainerIconButton(BoardPanel board, ContainerPanel container) {
        if (board == null) {
            throw new IllegalArgumentException("SaveEditJButton: "
                    + "board is null!");
        }
        if (container == null) {
            throw new IllegalArgumentException("SaveEditJButton: "
                    + "container is null!");
        }
        this.board = board;
        commandFacade = board.getCommandFacade();
        this.container = container;
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        animations = new CopyOnWriteArrayList<>();
        setPreferredSize(CONTAINER_BUT_DIM);
        setMaximumSize(CONTAINER_BUT_DIM);
        setMinimumSize(CONTAINER_BUT_DIM);
        setContentAreaFilled(false);
        setFocusPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(icon, 0, 0, null);
        for (Animation animation : animations) {
            animation.draw(g2d);
        }
    }
    
    /***
     * Creates icon
     * @param color icon color 
     * @param opaque container opaque
     */
    public abstract void createIcon(Color color, boolean opaque);
    
    /***
     * Creates a transparent icon
     * @return transparent icon
     */
    public static BufferedImage createTransparentIcon() {
        int width = CONTAINER_BUT_DIM.width;
        int height = CONTAINER_BUT_DIM.height;
        BufferedImage ic = new BufferedImage(width, height, 
                BufferedImage.TYPE_INT_ARGB);
        Color tr = new Color(0, 0, 0, 200);
        int[] pixels = new int[width * height];
        for (int pixel : pixels) {
            pixel = tr.getRGB();
        }
        ic.setRGB(0, 0, width, height, pixels, 0, width);
        return ic;
    }

    /***
     * Creates path
     * @param x x-coordinates
     * @param y y-coordinates
     * @return path
     */
    public static GeneralPath createPath(int[] x, int[] y) {
        GeneralPath path = 
                new GeneralPath(GeneralPath.WIND_EVEN_ODD, x.length);
        path.moveTo(x[0], y[0]);
        for (int i = 1; i < x.length; ++ i) {
            path.lineTo(x[i], y[i]);
        }
        path.closePath();
        return path;
    }

    @Override
    public void addAnimation(Animation animation) {
        animations.add(animation);
    }

    @Override
    public void removeAnimation(Animation animation) {
        animations.remove(animation);
    }
}
