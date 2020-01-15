package gui.container;

import gui.board.BoardPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import rslib.commands.common.ChangeBlockCommand;

/***
 * Represents a save edit button
 */
public abstract class SaveEditButton extends ContainerIconButton {

    /** Button state: true for edit, false for save */
    private boolean state;
    
    /** Edit icon */
    private final List<GeneralPath> editIcon;
    
    /** Save icon */
    private final List<GeneralPath> saveIcon;
    
    /***
     * Constructor
     * @param board link to board
     * @param container link to container
     * @param commandFacade link to commandFacade
     */
    public SaveEditButton(BoardPanel board, 
            ContainerPanel container) {
        super(board, container);        
        state = true;
        editIcon = new ArrayList<>();
        saveIcon = new ArrayList<>();
        initComponents();
    }
    
    /***
     * Initializes GUI components
     */
    private void initComponents() {
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (state) {
                    if (commandFacade.doCommand(
                            new ChangeBlockCommand(board, container, true), false)) {
                        state = false;
                        editActions();
                        createIcon(container.getForeground(), container.isComponentOpaque());
                    }
                }
                else {
                    state = true;
                    saveActions();
                    createIcon(container.getForeground(), container.isComponentOpaque());
                    commandFacade.doCommand(
                            new ChangeBlockCommand(board, container, false), false);
                }
            }
        });
        createPaths();
        createIcon(container.getForeground(), container.isComponentOpaque());
    }
    
    /***
     * Does edit actions
     */
    protected void editActions() {
    }
    
    /***
     * Does save actions
     */
    protected void saveActions() {
    }

    @Override
    public void createIcon(Color color, boolean opaque) {
        BufferedImage ic = createTransparentIcon();
        if (! opaque) {
            icon = ic;
            return;
        }
        Graphics2D g2d = ic.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);    
        g2d.setColor(color);
        if (state) {
            for (GeneralPath path : editIcon) {
                g2d.fill(path);
                g2d.draw(path);
            }
        }
        else {
            for (GeneralPath path : saveIcon) {
                g2d.fill(path);
                g2d.draw(path);
            }
        }      
        icon = ic;
    }
    
    /***
     * Creates icon paths
     */
    private void createPaths() {        
        
        // Edit icon       
        int[] handleX = { 18, 23, 10, 4 };
        int[] handleY = { 0, 5, 19, 13 };
        GeneralPath handle = createPath(handleX, handleY);

        int[] edgeX = { 2, 7, 0 };
        int[] edgeY = { 16, 21, 23 };
        GeneralPath edge = createPath(edgeX, edgeY);
        
        editIcon.add(handle);
        editIcon.add(edge);
        
        // Save icon        
        int[] borderX = { 1, 0, 0, 1, 20, 23, 23, 22, 20, 20, 19, 4, 4, 3, 3 };
        int[] borderY = { 23, 22, 1, 0, 0, 3, 22, 23, 23, 3, 2, 2, 11, 11, 23 };
        GeneralPath border = createPath(borderX, borderY);

        int[] upperBlockX = { 12, 17, 17, 12 };
        int[] upperBlockY = { 4, 4, 7, 7 };
        GeneralPath upperBlock = createPath(upperBlockX, upperBlockY);

        int[] septumX = { 4, 19, 19, 4 };
        int[] septumY= { 11, 11, 9, 9 };
        GeneralPath septum = createPath(septumX, septumY);

        int[] lowerBlockX = { 6, 17, 17, 6 };
        int[] lowerBlockY = { 14, 14, 23, 23 };
        GeneralPath lowerBlock = createPath(lowerBlockX, lowerBlockY);
        
        saveIcon.add(border);
        saveIcon.add(upperBlock);
        saveIcon.add(septum);
        saveIcon.add(lowerBlock);
    }

    public boolean isState() {
        return state;
    }
}
