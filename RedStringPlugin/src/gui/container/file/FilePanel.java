package gui.container.file;

import gui.board.animation.Animatable;
import gui.board.animation.Animation;
import gui.board.animation.AnimationManager;
import gui.board.animation.Geometry;
import gui.board.animation.PointAnimation;
import gui.board.animation.Pointable;
import gui.container.AnimatableLabel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import rslib.gui.container.file.FileModel;

/***
 * Represents a panel that hold file info
 */
public class FilePanel extends JPanel implements Pointable, Animatable {
    
    /** Link to container */
    private FileContainerPanel container;
    
    /** File itself */
    private FileModel file;
    
    /** Icon label */
    private FileIconLabel iconLabel;
    
    /** Name label */
    private AnimatableLabel filenameLabel;
    
    /** Animations */
    private final List<Animation> animations;
    
    /***
     * Constructor
     * @param container link to container
     * @param file file 
     */
    public FilePanel(FileContainerPanel container, FileModel file) {
        if (container == null) {
            throw new IllegalArgumentException("FilePanel: "
                    + "container is null!");
        }
        this.container = container;
        this.file = file;
        animations = new CopyOnWriteArrayList<>();
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        setOpaque(true);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setForeground(container.getForeground());
        iconLabel = new FileIconLabel(this);
        iconLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(iconLabel);
        filenameLabel = new AnimatableLabel();
        filenameLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(filenameLabel);
        filenameLabel.setBackground(container.getBackground());
        filenameLabel.setForeground(container.getForeground());
        filenameLabel.setFont(container.getFont());
        processFile(file);
    }

    public void setPanelBackground(Color bg) {
        setBackground(bg);
    }
    
    public void setPanelForeground(Color fg) {
        setForeground(fg);
        filenameLabel.setForeground(fg);
        iconLabel.createIcon(fg);
    }
    
    public void setPanelFont(Font font) {
        filenameLabel.setFont(font);
    }

    public FileModel getFile() {
        return file;
    }

    public void setFile(FileModel file) {
        processFile(file);
    }
    
    /***
     * Processes and sets new file to panel
     * @param file file
     */
    private void processFile(FileModel file) {
        this.file = file;
        if (file == null) {
            filenameLabel.setText("");
            iconLabel.createIcon(null);
        }
        else {
            filenameLabel.setText(file.getFilename()+ "(" + 
                    (int)(file.getLength() / 1024.0) + " kb)");
            iconLabel.createIcon(container.getForeground());
        }
    }
    
    @Override
    public void point(AnimationManager am, int i, int i1, Color color) {
        if (Geometry.hasPoint(iconLabel, i, i1)) {
            am.addAnimation(
                    new PointAnimation(iconLabel, i - iconLabel.getX(), 
                    i1 - iconLabel.getY(), color));
            return;
        }
        if (Geometry.hasPoint(filenameLabel, i, i1)) {
            am.addAnimation(
                    new PointAnimation(filenameLabel, i - filenameLabel.getX(), 
                    i1 - filenameLabel.getY(), color));
            return;
        }        
        am.addAnimation(new PointAnimation(this, i, i1, color));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			RenderingHints.VALUE_ANTIALIAS_ON);
        for (Animation animation : animations) {
            animation.draw(g2d);
        }
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
