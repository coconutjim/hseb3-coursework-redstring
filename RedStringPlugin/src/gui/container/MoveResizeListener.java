package gui.container;

import gui.board.BoardPanel;
import gui.board_frame.control.CommandFacade;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import rslib.commands.common.ChangeBlockCommand;
import rslib.commands.common.MoveCommand;
import rslib.commands.common.ResizeCommand;

/***
 * Represents a listener that allows 
 * board container moving and resizing via mouse control
 */
public class MoveResizeListener extends MouseAdapter {
    
    /** The container to move */
    private final ContainerPanel container;
    
    /** Link to board */
    private final BoardPanel board;
    
    /** Link to command facade */
    private final CommandFacade commandFacade;
    
    /** Container shadow */
    private final ContainerShadow containerShadow;
    
    /** If container was blocked before resizing or moving */
    private boolean wasBlocked;
    
    /** If the changing was too fast */
    private boolean fastChanging;
    
    /** If was dragged already */
    private boolean dragged;
  
    /** Pressed point */
    private Point pressed;
   
    /** Old container bounds */
    private Rectangle oldBounds;
    
    /** New container bounds */
    private Rectangle newBounds;

    /** Source cursor */
    private Cursor sourceCursor;
    
    /** If moving */
    private boolean moving;

    /** Border delta (not to move out of board bounds) */
    final private static int BORDER = BoardPanel.BORDER_DELTA;
    
    /** If resizing */
    private boolean resizing;
    
    /** Error (for displaying resize cursors )*/
    final private static int ERROR = 10;
    
     /** For defining resizing direction */
    private final static Map<Integer, Integer> CURSORS = new HashMap<>();
    
    static {
        CURSORS.put(1, Cursor.N_RESIZE_CURSOR);
        CURSORS.put(2, Cursor.W_RESIZE_CURSOR);
        CURSORS.put(4, Cursor.S_RESIZE_CURSOR);
        CURSORS.put(8, Cursor.E_RESIZE_CURSOR);
        CURSORS.put(3, Cursor.NW_RESIZE_CURSOR);
        CURSORS.put(9, Cursor.NE_RESIZE_CURSOR);
        CURSORS.put(6, Cursor.SW_RESIZE_CURSOR);
        CURSORS.put(12, Cursor.SE_RESIZE_CURSOR);
    }

    /** Resizing directions */
    private static final int NORTH = 1;
    private static final int WEST = 2;
    private static final int SOUTH = 4;
    private static final int EAST = 8;
    
    /** Counted direction */
    private int direction;
        
    /***
     * Constructor
     * @param container container to move
     * @param board link to board
     * @param commandFacade link to command facade
     */
    public MoveResizeListener(final ContainerPanel container, BoardPanel board,
            CommandFacade commandFacade) {
        if (container == null) {
            throw new IllegalArgumentException("MoveListener: "
                    + "container is null!");
        }
        if (board == null) {
            throw new IllegalArgumentException("MoveListener: "
                    + "board is null!");
        }
        if (commandFacade == null) {
            throw new IllegalArgumentException("MoveListener: "
                    + "commandFacade is null!");
        }
        this.container = container;
        this.board = board;
        this.commandFacade = commandFacade;
        containerShadow = new ContainerShadow(container);
        dragged = false;
        wasBlocked = false;
        fastChanging = false;
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        if (! (resizing || moving)) {
            sourceCursor = container.getCursor();
        }
    }

    
    @Override
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        if (! (resizing || moving)) {
            container.setCursor(sourceCursor);
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        Component component = e.getComponent();
        Point location = e.getPoint();
        direction = 0;

        if (location.x < ERROR)
            direction += WEST;
        if (location.x >= component.getWidth() - ERROR)
            direction += EAST;
        if (location.y < ERROR)
            direction += NORTH;
        if (location.y >= component.getHeight() - ERROR)
            direction += SOUTH;
        if (direction == 0) {
            container.setCursor(sourceCursor);
        }
        else {
            container.setCursor(Cursor.getPredefinedCursor(
                    CURSORS.get(direction)));
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if (! SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        if (direction == 0) {
            moving = true;
            container.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            containerShadow.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }
        else {
            resizing = true;
            containerShadow.setCursor(Cursor.getPredefinedCursor(
                    CURSORS.get(direction)));
        }        
        pressed = e.getPoint();
        SwingUtilities.convertPointToScreen(pressed, container);
        oldBounds = container.getBounds(); 
        newBounds = new Rectangle(oldBounds);    
        dragged = false;
        fastChanging = false;
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
        if (! SwingUtilities.isLeftMouseButton(e) || oldBounds == null) {
            return;
        }
        if (! dragged) {
            wasBlocked = container.isBlocked() &&
                    container.getBlockOwner().equals(commandFacade.getUsername());
            if (! wasBlocked) {
                //System.out.println("xent blovk");
                if (! commandFacade.doCommand(new ChangeBlockCommand(board, 
                        container, true), false)) {
                    return;
                }
            }
            containerShadow.setBounds(oldBounds);
            board.setLayer(containerShadow, container.getLayer(), BoardPanel.SHADOW_POSITION);
            board.add(containerShadow);
            dragged = true;
        }
        Point current  = e.getPoint();
        SwingUtilities.convertPointToScreen(current, container);
        if (moving) {
            move(current);
        }
        else {
            resize(current);
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
       super.mouseReleased(e);
       if (! SwingUtilities.isLeftMouseButton(e) || oldBounds == null) {
            return;
       }
       // Because of event speed
       boolean wasDragged = dragged;
       dragged = false;
       cancelProcessing();
       if (wasDragged && ! container.isBlocked()) {
           container.setBlocked(true, commandFacade.getUsername());
           fastChanging = true;
       }
       if (! oldBounds.equals(newBounds)) {
            if (moving) {
                 moving = false;
                 commandFacade.doCommand(new MoveCommand(board, container, 
                 newBounds.x, newBounds.y, ! wasBlocked), true);
            }
             else {
                 resizing = false;
                 commandFacade.doCommand(new ResizeCommand(board, container, 
                 newBounds.x, newBounds.y, newBounds.width, newBounds.height,
                 ! wasBlocked), true);
                 if (fastChanging && ! wasBlocked) {
                     container.setBlocked(false, null);
                 }
            }
       }
       else {
           if (wasDragged && ! wasBlocked) {
                commandFacade.doCommand(new ChangeBlockCommand(board, 
                    container, false), false);
                if (fastChanging) {
                     container.setBlocked(false, null);
                }
            }
       }
       oldBounds = null;
       newBounds = null;
       pressed = null;
       sourceCursor = null;
       wasBlocked = false;
       fastChanging = false;
    }
    
    /***
     * Moves the container to current point
     * @param current current point
     */
    private void move(Point current) {
        int x = oldBounds.x + current.x - pressed.x;
        int y = oldBounds.y + current.y - pressed.y;

        if (x + container.getComponentWidth() > 
                board.getComponentWidth() - BORDER) {
            x = board.getComponentWidth() - BORDER - 
                    container.getComponentWidth();
        }
        if (x < BORDER) {
            x = BORDER;
        } 
        if (y + container.getComponentHeight() > 
                board.getComponentHeight() - BORDER) {
            y = board.getComponentHeight() - BORDER - 
                    container.getComponentHeight();
        }
        if (y < BORDER) {
            y = BORDER;
        }
 
        containerShadow.setLocation(x, y);
        newBounds.setLocation(x, y);
    }
    
    /***
     * Resizes the container to current point
     * @param current current point
     */
    private void resize(Point current) {
        int x = oldBounds.x;
        int y = oldBounds.y;
        int width = oldBounds.width;
        int height = oldBounds.height;

        int dragX = current.x - pressed.x;
        int dragY = current.y - pressed.y;

        if (WEST == (direction & WEST)) {
            x += dragX;
            width -= dragX;
        }
        if (NORTH == (direction & NORTH)) {
            y += dragY;
            height -= dragY;
        }
        if (EAST == (direction & EAST)) {
            width += dragX;
        }
        if (SOUTH == (direction & SOUTH)) {
            height += dragY;
        }

        width = Math.max(width, container.getComponentMinimumWidth());
        height = Math.max(height, container.getComponentMinimumHeight());

        // TODO: see
        if (width == container.getComponentMinimumWidth()) {
            x = newBounds.x;
        }
        if (height == container.getComponentMinimumHeight()) {
            y = newBounds.y;
        }
        
        width = Math.min(width, container.getComponentMaximumWidth());
        height = Math.min(height, container.getComponentMaximumHeight());

        // TODO: see
        if (width == container.getComponentMaximumWidth()) {
            x = newBounds.x;
        }
        if (height == container.getComponentMaximumHeight()) {
            y = newBounds.y;
        }

        // TODO: see
        if (width == oldBounds.width) {
            x = oldBounds.x;
        }
        if (height == oldBounds.height) {
            y = oldBounds.y;
        }
        
        if (x + width > board.getComponentWidth() - BORDER || x < BORDER) {
            x = newBounds.x;
            width = newBounds.width;
        } 
        if (y + height > board.getComponentHeight() - BORDER || y < BORDER) {
            y = newBounds.y;
            height = newBounds.height;
        }
        containerShadow.setBounds(x, y, width, height);
        newBounds = new Rectangle(x, y, width, height);
    }
    
    /***
     * Cancels processing
     */
    private void cancelProcessing() {
        container.setCursor(sourceCursor);
        containerShadow.setCursor(Cursor.getDefaultCursor());
        board.remove(containerShadow);
        board.repaint();
    }
    
    public void setComplexPainting(boolean complexPainting) {
        containerShadow.setComplexPainting(complexPainting);
    }
}
