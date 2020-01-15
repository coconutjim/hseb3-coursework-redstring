package gui.util;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import rslib.cs.common.Status;
import rslib.gui.container.BoardContainer;

/***
 * Hold info about images
 */
public final class Images {
    
    /** Icons for user statuses */
    public final static Map<Status, Icon> STATUS_ICONS;
    
    /** Container icons */
    public final static Map<BoardContainer.ContainerType, Icon> CONTAINER_ICONS;
    
    /** Container drag images */
    public final static Map<BoardContainer.ContainerType, Image> CONTAINER_IMAGES;
    
    /** Container icons */
    public final static Icon CONTAINER_SAVE_ICON;
    public final static Icon CONTAINER_EDIT_ICON;
    public final static Icon CONTAINER_DOWNLOAD_ICON;
    
    /** Control icons */
    public final static Icon OPEN_ICON;
    public final static Icon SAVE_ICON;
    public final static Icon UNDO_ICON;
    public final static Icon REDO_ICON;
    public final static Icon EXTEND_ICON;
    public final static Icon SHRINK_ICON;
    public final static Icon CLEAR_ICON;
    public final static Icon POINTER_ICON;
    public final static Icon SETTINGS_ICON;
    public final static Icon SYNC_ICON;
    public final static Icon DELETE_ICON;
    
    static {
        STATUS_ICONS = new HashMap<>();
        STATUS_ICONS.put(Status.READONLY,
                checkIcon(Images.class.getResource("/icons/statuses/readonly.png")));
        STATUS_ICONS.put(Status.COMMON,
                checkIcon(Images.class.getResource("/icons/statuses/common.png")));

        STATUS_ICONS.put(Status.LOBBY_ROOT,
                checkIcon(Images.class.getResource("/icons/statuses/root.png")));
        STATUS_ICONS.put(Status.MODERATOR,
                checkIcon(Images.class.getResource("/icons/statuses/moderator.png")));
        
        CONTAINER_ICONS = new HashMap<>();
        CONTAINER_ICONS.put(BoardContainer.ContainerType.TEXT_CONTAINER, 
                checkIcon(Images.class.getResource("/icons/control/containers/text.png")));
        CONTAINER_ICONS.put(BoardContainer.ContainerType.IMAGE_CONTAINER, 
                checkIcon(Images.class.getResource("/icons/control/containers/image.png")));
        CONTAINER_ICONS.put(BoardContainer.ContainerType.FILE_CONTAINER, 
                checkIcon(Images.class.getResource("/icons/control/containers/file.png")));
        
        CONTAINER_IMAGES = new HashMap<>();
        CONTAINER_IMAGES.put(BoardContainer.ContainerType.TEXT_CONTAINER, 
                checkImage(Images.class.getResource("/icons/control/containers/textDrag.png")));
        CONTAINER_IMAGES.put(BoardContainer.ContainerType.IMAGE_CONTAINER, 
                checkImage(Images.class.getResource("/icons/control/containers/imageDrag.png")));
        CONTAINER_IMAGES.put(BoardContainer.ContainerType.FILE_CONTAINER, 
                checkImage(Images.class.getResource("/icons/control/containers/fileDrag.png")));
        
        CONTAINER_EDIT_ICON = checkIcon(Images.class.getResource("/icons/control/containers/edit.png"));
        CONTAINER_SAVE_ICON = checkIcon(Images.class.getResource("/icons/control/containers/save.png"));
        CONTAINER_DOWNLOAD_ICON = checkIcon(Images.class.getResource("/icons/control/containers/download.png"));
        
        OPEN_ICON = checkIcon(Images.class.getResource("/icons/control/open.png"));
        SAVE_ICON = checkIcon(Images.class.getResource("/icons/control/save.png"));
        UNDO_ICON = checkIcon(Images.class.getResource("/icons/control/undo.png"));
        REDO_ICON = checkIcon(Images.class.getResource("/icons/control/redo.png"));
        EXTEND_ICON = checkIcon(Images.class.getResource("/icons/control/extend.png"));
        SHRINK_ICON = checkIcon(Images.class.getResource("/icons/control/shrink.png"));
        CLEAR_ICON = checkIcon(Images.class.getResource("/icons/control/clear.png"));
        POINTER_ICON = checkIcon(Images.class.getResource("/icons/control/pointer.png"));
        SETTINGS_ICON = checkIcon(Images.class.getResource("/icons/control/settings.png"));
        SYNC_ICON = checkIcon(Images.class.getResource("/icons/control/sync.png"));
        DELETE_ICON = checkIcon(Images.class.getResource("/icons/control/delete.png"));
    }
    
    /***
     * Checks icon URL
     * @param url icon URL
     * @return icon by its URL or default icon
     */
    private static Icon checkIcon(URL url) {
        Icon icon;
        try {
            icon = new ImageIcon(url);
        }
        catch (NullPointerException e) {
            icon = UIManager.getIcon("OptionPane.errorIcon");
        }
        return icon;
    }
    
    /***
     * Checks image URL
     * @param url image URL
     * @return image by its URL or ?? null
     */
    private static Image checkImage(URL url) {
        Image image;
        try {
            image = ImageIO.read(url);
        }
        catch (IOException | IllegalArgumentException e) {
            image = null;
            //TODO: ???????
        }
        return image;
    }
}
        