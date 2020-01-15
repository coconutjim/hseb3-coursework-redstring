package gui.container.file;

import gui.board.BoardPanel;
import gui.board.animation.AnimationManager;
import gui.board.animation.Geometry;
import gui.board.animation.PointAnimation;
import gui.container.ContainerPanel;
import gui.container.ContentScroll;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.Box;
import rslib.commands.container.file.ChangeFileCommand;
import rslib.gui.container.ExternalizableContainer;
import rslib.gui.container.file.ExternalizableFileContainer;
import rslib.gui.container.file.FileContainer;
import rslib.gui.container.file.FileModel;
import rslib.gui.style.ColorModel;
import rslib.gui.style.FontModel;

/***
 * Represents a container that can hold file
 */
public class FileContainerPanel extends ContainerPanel implements FileContainer {
    
    /** File content */
    private final FilePanel filePanel;
    
    /** File scroll */
    private ContentScroll scroll;
    
    /** Load button */
    private DownloadButton downloadButton;
    
    /***
     * Constructor
     * @param type container type
     * @param owner container owner
     * @param textId text container id
     * @param id container id
     * @param board link to board
     * @param origin origin point
     * @param file file
     */
    public FileContainerPanel(ContainerType type, 
            String owner, int id, BoardPanel board,
            Point origin, FileModel file) {
        this(type, owner, id, board, origin);
        filePanel.setFile(file);
    }
    
    /***
     * Constructor
     * @param type container type
     * @param owner container owner
     * @param textId text container id
     * @param id container id
     * @param board link to board
     * @param origin origin point
     */
    public FileContainerPanel(ContainerType type, 
            String owner, int id, BoardPanel board,
            Point origin) {
        super(type, owner, id, board, origin);
        filePanel = new FilePanel(this, null);
        initComponents();
    }
    
    /***
     * Constructor
     * @param board link to board
     * @param efc saved data
     */
    public FileContainerPanel(BoardPanel board, ExternalizableFileContainer
            efc) {
        super(board, efc);
        filePanel = new FilePanel(this, efc.getFile());
        initComponents();
    }
    
    /***
     * Initializes GUI components and features
     */
    private void initComponents() {  
        scroll = new ContentScroll(this, filePanel);
        filePanel.setOpaque(true);
        filePanel.setBackground(getBackground());
        filePanel.repaint();
        scroll.setAlignmentX(CENTER_ALIGNMENT);
        add(scroll, "wrap");        
        scroll.changeAppearance();
        add(Box.createVerticalStrut(vertical_gap));
        
        downloadButton = new DownloadButton(board, this, filePanel);
        downloadButton.setAlignmentX(CENTER_ALIGNMENT);
        add(downloadButton, "wrap");
        add(Box.createVerticalStrut(vertical_gap));
        initFeatures();
    }
    
    /***
     * Sets file container features
     */
    private void initFeatures() {
        filePanel.setTransferHandler(new FileTransferHandler(this));
    }
    
    @Override
    public int getFreeHeight() {
        return super.getFreeHeight() - 
                downloadButton.getHeight() - 2 * vertical_gap;
    }
    
    /***
     * Handles file from DND
     * @param file DND file
     */
    public void handleFile(FileModel file) {
        board.getCommandFacade().doCommand(new 
            ChangeFileCommand(board, this, file), true);
    }

    @Override
    public void setComponentBackground(ColorModel cm) {
        super.setComponentBackground(cm); 
        scroll.changeAppearance();
        filePanel.setPanelBackground(getBackground());
    }
    
    @Override
    public void setComponentOpaque(boolean bln) {
        super.setComponentOpaque(bln);
        scroll.changeAppearance();
        Color color = getForeground();
        Color fg = bln ? color : new Color(color.getRed(), color.getGreen(),
                color.getBlue(), 150);
        downloadButton.createIcon(fg, true);
        repaint();
    }
    
    @Override
    public void setComponentForeground(ColorModel cm) {
        super.setComponentForeground(cm);
        scroll.changeAppearance();
        filePanel.setPanelForeground(getForeground());
        downloadButton.createIcon(getForeground(), isComponentOpaque());
    }

    @Override
    public void setComponentFont(FontModel fm) {
        super.setComponentFont(fm); 
        filePanel.setPanelFont(getFont());
        revalidate();
    }

    @Override
    public void setFile(FileModel file) {
        filePanel.setFile(file);
    }

    @Override
    public FileModel getFile() {
        return filePanel.getFile();
    }
   
    @Override
    public void clearContainer() {
        filePanel.setFile(null);
    }

    @Override
    public void setContent(ExternalizableContainer ec) {
        setFile(((ExternalizableFileContainer) ec).getFile());
    }
    
    @Override
    public ExternalizableContainer toExternalizable() {
        return new ExternalizableFileContainer(getComponentOwner(), 
                getComponentStatus(), getComponentLeft(), 
                getComponentTop(), getComponentWidth(), getComponentHeight(),
                getComponentMinimumWidth(), getComponentMinimumHeight(),
                getComponentMaximumWidth(), getComponentMaximumHeight(),
                getComponentName(), getComponentId(),
                getComponentFont(), isComponentOpaque(),
                getComponentForeground(), getComponentBackground(),
                getLayer(), getType(), isBlocked(), getBlockOwner(), getFile());
    }
    
    @Override
    public void point(AnimationManager am, int i, int i1, Color color) {
        if (Geometry.hasPoint(blockLabel, i, i1)) {
            am.addAnimation(
                    new PointAnimation(blockLabel, i - blockLabel.getX(), 
                    i1 - blockLabel.getY(), color));
            return;
        }
        if (Geometry.hasPoint(nameLabel, i, i1)) {
            am.addAnimation(
                    new PointAnimation(nameLabel, i - nameLabel.getX(), 
                    i1 - nameLabel.getY(), color));
            return;
        }
        if (Geometry.hasPoint(scroll, i, i1)) {
            Rectangle bounds = scroll.getViewport().getViewRect();
            filePanel.point(am, i - scroll.getX() + bounds.x, 
                    i1 - scroll.getY() + bounds.y, color);
            return;
        }
        if (Geometry.hasPoint(downloadButton, i, i1)) {
            am.addAnimation(
                    new PointAnimation(downloadButton, i - downloadButton.getX(), 
                    i1 - downloadButton.getY(), color));
            return;
        }
        am.addAnimation(new PointAnimation(this, i, i1, color));
    }
}