package gui.board;

import gui.board.animation.Animatable;
import gui.board.animation.Animation;
import gui.board.animation.AnimationManager;
import gui.board.animation.Geometry;
import gui.board.animation.PointAnimation;
import gui.board.animation.PointPanel;
import gui.board.animation.Pointable;
import gui.container.ContainerPanel;
import gui.container.file.FileContainerPanel;
import gui.container.image.ImageContainerPanel;
import gui.container.text.TextContainerPanel;
import gui.board_frame.control.CommandFacade;
import gui.parsing.Parsing;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JLayeredPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import rslib.cs.common.Status;
import rslib.cs.protocol.events.board.BoardEvent;
import static rslib.cs.protocol.events.board.BoardEvent.BoardEventType.ADD_CONTAINER_E;
import static rslib.cs.protocol.events.board.BoardEvent.BoardEventType.DELETE_CONTAINER_E;
import rslib.cs.protocol.events.board.PointEvent;
import rslib.cs.protocol.events.board.board.ChangeGeneralColorEvent;
import rslib.cs.protocol.events.board.board.ChangeGeneralFontEvent;
import rslib.cs.protocol.events.board.board.ChangeGeneralOpaqueEvent;
import rslib.cs.protocol.events.board.board.ChangeSyncModeEvent;
import rslib.cs.protocol.events.board.board.SetBoardContentEvent;
import rslib.cs.protocol.events.board.common.ChangeBlockEvent;
import rslib.cs.protocol.events.board.common.ChangeColorEvent;
import rslib.cs.protocol.events.board.common.ChangeFontEvent;
import rslib.cs.protocol.events.board.common.ChangeNameEvent;
import rslib.cs.protocol.events.board.common.ChangeOpaqueEvent;
import rslib.cs.protocol.events.board.common.ChangeOwnerEvent;
import rslib.cs.protocol.events.board.common.ChangeStatusEvent;
import rslib.cs.protocol.events.board.common.MoveEvent;
import rslib.cs.protocol.events.board.common.ResizeEvent;
import rslib.cs.protocol.events.board.container.AddContainerEvent;
import rslib.cs.protocol.events.board.container.ChangeContainerLayerEvent;
import rslib.cs.protocol.events.board.container.ClearContainerEvent;
import rslib.cs.protocol.events.board.container.DeleteContainerEvent;
import rslib.cs.protocol.events.board.container.SetContainerContentEvent;
import rslib.cs.protocol.events.board.container.file.ChangeFileEvent;
import rslib.cs.protocol.events.board.container.image.ChangeImageEvent;
import rslib.cs.protocol.events.board.container.text.ChangeTextEvent;
import rslib.cs.protocol.events.main_client.MainClientEvent;
import rslib.gui.BasicComponent;
import rslib.gui.board.ExternalizableBoard;
import rslib.gui.board.InteractiveBoard;
import rslib.gui.container.BoardContainer;
import rslib.gui.container.ExternalizableContainer;
import rslib.gui.container.file.ExternalizableFileContainer;
import rslib.gui.container.file.FileContainer;
import rslib.gui.container.image.ExternalizableImageContainer;
import rslib.gui.container.image.ImageContainer;
import rslib.gui.container.text.ExternalizableTextContainer;
import rslib.gui.container.text.TextContainer;
import rslib.gui.style.ColorModel;
import rslib.gui.style.FontModel;

/***
 * Represents board panel itself
 */
public class BoardPanel extends JLayeredPane 
        implements InteractiveBoard, Pointable, Animatable {
    
    /** Board owner */
    private String owner;
    
    /** Board status */
    private Status status;
    
    /** Board name */
    private String name;
    
    /** Opaque */
    protected boolean opaque;
    
    /** Command facade */
    private final CommandFacade commandFacade;
    
    /** Minimum width */
    private final int minimumWidth;
    
    /** Maximum width */
    private final int maximumWidth;
    
    /** Minimum height */
    private final int minimumHeight;
    
    /** Maximum height */
    private final int maximumHeight;

    /** General container font */
    private FontModel generalFont;
    
    /** General container opaque */
    private boolean generalOpaque;
    
    /** General container foreground color */
    private ColorModel generalForeground;
    
    /** General container background color */
    private ColorModel generalBackground;
    
    /** All board containers */
    private final List<ContainerPanel> containers;
    
    /** Board blocked */
    private boolean blocked;
    
    /** Block owner */
    private String blockOwner;
    
    /** Board asynchronous */
    private boolean asynchronous;
    
    /** Link to viewport */
    public JViewport viewport;
    
    /** Minimum sizes */
    public static final int MINIMUM_BOARD_WIDTH = 900;
    public static final int MINIMUM_BOARD_HEIGHT = 600;
    
    /** Maximum sizes */
    public static final int MAXIMUM_BOARD_WIDTH = 10000;
    public static final int MAXIMUM_BOARD_HEIGHT = 10000;
    
    /** Point mode */
    private boolean pointMode;
    
    /** Complex container painting mode */
    private boolean complexPainting;
    
    /** Point panel */
    private final PointPanel pointPanel;
    
    /** Container position on the board */
    public final static int CONTAINER_POSITION;

    /** Shadow position on the board */
    public final static int SHADOW_POSITION;
    
    /** Animation manager */
    private final AnimationManager animationManager;
    
    /** Animations */
    private final List<Animation> animations;
    
    static {
        CONTAINER_POSITION = -1;
        SHADOW_POSITION = 0;
    }
    
    /***
     * Constructor
     * @param commandFacade command facade
     * @param eb saved data
     * @param viewport link to component viewport
     */
    public BoardPanel(CommandFacade commandFacade, ExternalizableBoard eb,
            JViewport viewport) {
        //TODO: check null
        this.commandFacade = commandFacade;
        this.viewport = viewport;
        complexPainting = true;
        setPreferredSize(new Dimension(eb.getComponentWidth(), 
                eb.getComponentHeight()));
        containers = new CopyOnWriteArrayList<>();
        List<ExternalizableContainer> eContainers = eb.getContainers();
        for (ExternalizableContainer ec : eContainers) {
            ContainerPanel container = inflate(ec);
            containers.add(container);
        }
        addContainers(containers);
        setOpaque(true);
        owner = eb.getComponentOwner();
        status = eb.getComponentStatus();
        name = eb.getComponentName();
        opaque =  eb.isComponentOpaque();
        setFont(Parsing.createFont(eb.getComponentFont()));
        setForeground(Parsing.createColor(eb.getComponentForeground()));
        setBackground(Parsing.createColor(eb.getComponentBackground()));
        minimumWidth = eb.getComponentMinimumWidth();
        minimumHeight = eb.getComponentMinimumHeight();
        maximumWidth = eb.getComponentMaximumWidth();
        maximumHeight = eb.getComponentMaximumHeight();
        generalFont = eb.getGeneralContainerFont();
        generalOpaque = eb.isGeneralContainerOpaque();
        generalForeground = eb.getGeneralContainerForeground();
        generalBackground = eb.getGeneralContainerBackground();
        blocked = eb.isBlocked();
        blockOwner = eb.getBlockOwner();
        asynchronous = eb.isAsynchronous();
        pointMode = false;
        pointPanel = new PointPanel(this);
        animationManager = new AnimationManager(this);
        new Thread(animationManager).start();
        animations = new CopyOnWriteArrayList<>();
        initFeatures(viewport);
        revalidate();
    }
    
    /***
     * Setting features to board
     * @param viewport viewport to the board
     * @param board board itself
     */
    private void initFeatures(JViewport viewport) {
        // Mouse scrolling
        MouseScrollListener msl = new MouseScrollListener(this, viewport);
        addMouseListener(msl);
        addMouseMotionListener(msl);
        // Container adding
        setTransferHandler(new CreateContainerHandler(this));
        // Pop up menu
        addMouseListener(new BoardPopupMenu(this).createPopUpMenu());
        // Tool tip
        ToolTipManager.sharedInstance().registerComponent(this);
    }
 
    /***
     * Gets container by the current id
     * @param id container id
     * @return corresponding container, null if container was not found
     */
    private ContainerPanel getContainerById(int id) {
        for (ContainerPanel container : containers) {
            if (container.getComponentId() == id) {
                return container;
            }
        }
        return null;
    }
    
    @Override
    public void clearBoard() {
        containers.clear();
        removeAll();
        repaint();
    }

    @Override
    public void setBoardContent(CopyOnWriteArrayList<ExternalizableContainer> 
            list) {
        clearBoard();
        for (ExternalizableContainer ec : list) {
            ContainerPanel container = inflate(ec);
            containers.add(container);         
        }
        addContainers(containers);
    }
    
    /***
     * Sets containers to the board
     * @param containers container list
     */
    private void addContainers(final List<ContainerPanel> containers) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (ContainerPanel container: containers) {
                    setLayer(container, container.getLayer(), CONTAINER_POSITION);
                    add(container);
                    container.validate();
                }
                viewport.scrollRectToVisible(viewport.getBounds());
            }  
        });
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            //TODO: correct
        }
        viewport.scrollRectToVisible(viewport.getBounds());
    }

    @Override
    public void addContainer(BoardContainer bc) {
        final ContainerPanel container = (ContainerPanel) bc;
        int layer = container.getLayer();
        if (layer == BoardContainer.TO_FRONT) {
            layer = containers.size() + 1;
            container.setLayer(layer);
        }
        else {
            for (ContainerPanel cp : containers) {
                int cpLayer = cp.getLayer();
                if (cpLayer >= layer) {
                    ++ cpLayer;
                    cp.setLayer(cpLayer);
                    setLayer(cp, cpLayer);
                }
            }
        }
        final int finalLayer = layer;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                containers.add(container);
                setLayer(container, finalLayer, CONTAINER_POSITION);
                if (pointMode) {
                    setLayer(pointPanel, containers.size() + 1);
                }
                add(container);
                container.revalidate();
            }
        });
    }
    
    @Override
    public void deleteContainer(int i) {
        ContainerPanel container = getContainerById(i);
        remove(container);
        int layer = container.getLayer();
        containers.remove(container);
        for (ContainerPanel cp : containers) {
            int cpLayer = cp.getLayer();
            if (cpLayer > layer) {
                -- cpLayer;
                cp.setLayer(cpLayer);
                setLayer(cp, cpLayer);
            }
        }
        if (pointMode) {
            setLayer(pointPanel, containers.size() + 1);
        }
        repaint();
    }

    @Override
    public ExternalizableBoard toExternalizable() {
        CopyOnWriteArrayList<ExternalizableContainer> eContainers = 
                new CopyOnWriteArrayList<>();
        for (BoardContainer container : containers) {
            eContainers.add(container.toExternalizable());
        }
        return new ExternalizableBoard(getComponentOwner(), getComponentStatus(),
        getComponentLeft(), getComponentTop(), getComponentWidth(),
        getComponentHeight(), getComponentMinimumWidth(), 
        getComponentMinimumHeight(), getComponentMaximumWidth(),
        getComponentMaximumHeight(), getComponentName(), getComponentId(),
        getComponentFont(), isComponentOpaque(),
        getComponentForeground(), getComponentBackground(),
        blocked, blockOwner,
        eContainers, generalFont, generalOpaque, 
        generalForeground, generalBackground, asynchronous);
    }

    @Override
    public int generateId() {
        Random random = new Random();
        int id = random.nextInt(1000000);
        for (ContainerPanel container : containers) {
            if (container.getComponentId() == id) {
                return generateId();
            }
        }
        return id;
   }
    
    @Override
    public BoardContainer findContainer(int i) {
        for (ContainerPanel container : containers) {
            if (container.getComponentId() == i) {
                return container;
            }
        }
        return null;
    }
    
    @Override
    public void setLayerPosition(BoardContainer bc, int i) {
        int oldLayer = bc.getLayer();
        int newLayer;
        switch (i) {
            case BoardContainer.TO_BACKGROUND: {
                for (ContainerPanel container : containers) {
                    int layer = container.getLayer();
                    if (layer < oldLayer) {
                        ++ layer;
                        container.setLayer(layer);
                        setLayer(container, layer, CONTAINER_POSITION);
                    }
                }
                newLayer = BoardContainer.BACKGROUND_LAYER;
                break;
            }
            case BoardContainer.TO_FRONT: {
                int max = 0;
                for (ContainerPanel container : containers) {                 
                    int layer = container.getLayer();
                    if (layer > max) {
                        max = layer;
                    }
                    if (layer > oldLayer) {
                        -- layer;
                        container.setLayer(layer);
                        setLayer(container, layer, CONTAINER_POSITION);
                    }
                }
                newLayer = max;
                break;
            }
            default: {
                if (i > oldLayer) {
                    for (ContainerPanel container : containers) {                 
                        int layer = container.getLayer();
                        if (layer <= i && layer > oldLayer) {
                            -- layer;
                            container.setLayer(layer);
                            setLayer(container, layer, CONTAINER_POSITION);
                        }
                    }
                }
                else {
                    if (i < oldLayer) {
                        for (ContainerPanel container : containers) {                 
                            int layer = container.getLayer();
                            if (layer >= i && layer < oldLayer) {
                                ++ layer;
                                container.setLayer(layer);
                                setLayer(container, layer, CONTAINER_POSITION);
                            }
                        }
                        ContainerPanel container = (ContainerPanel) bc;
                        container.setLayer(i);
                        setLayer(container, i, CONTAINER_POSITION);
                        
                        //TODO wtf here
                    }
                }        
                newLayer = i;
                break;
            }
        }
        ContainerPanel container = (ContainerPanel) bc;
        container.setLayer(newLayer);
        setLayer(container, newLayer, CONTAINER_POSITION);
    }

    @Override
    public int getLayerPosition(BoardContainer bc) {
        return bc.getLayer();
    }

    @Override
    public void setGeneralContainerFont(FontModel fm) {       
        generalFont = fm;
        for (ContainerPanel container : containers) {
            if (! (container.isBlocked() && ! container.getBlockOwner().
                    equals(commandFacade.getUsername()))) {
                container.setFont(Parsing.createFont(fm));
            }
        }
    }

    @Override
    public FontModel getGeneralContainerFont() {
        return generalFont;
    }

    @Override
    public void setGeneralContainerOpaque(boolean bln) {
        generalOpaque = bln;
        generalBackground.setOpaque(bln);
        for (ContainerPanel container : containers) {
            if (! container.isBlocked()) {
                container.setComponentOpaque(bln);
            }
        }
    }

    @Override
    public boolean isGeneralContainerOpaque() {
        return generalOpaque;
    }

    @Override
    public void setGeneralContainerBackground(ColorModel cm) {
        cm.setOpaque(generalOpaque);
        generalBackground = cm;
        for (ContainerPanel container : containers) {
            if (! container.isBlocked()) {
                container.setComponentBackground(cm);
            }
        }
    }

    @Override
    public ColorModel getGeneralContainerBackground() {
        return generalBackground;
    }

    @Override
    public void setGeneralContainerForeground(ColorModel cm) {
        generalForeground = cm;
        for (ContainerPanel container : containers) {
            if (! container.isBlocked()) {
                container.setComponentForeground(cm);
            }
        }
    }

    @Override
    public ColorModel getGeneralContainerForeground() {
        return generalForeground;
    }

    @Override
    public void moveComponent(int i, int i1) {
        //TODO: implement, set scroll position
    }

    @Override
    public int getComponentLeft() {
        return 0;
    }

    @Override
    public int getComponentTop() {
        return 0;
    }

    @Override
    public void resizeComponent(int i, int i1, int i2, int i3) {
        Dimension dimension = calculateShrunkBoard();
        i2 = Math.max(i2, dimension.width);
        i3 = Math.max(i3, dimension.height);
        setPreferredSize(new Dimension(i2, i3));
        pointPanel.setBounds(0, 0, i2, i3);
        revalidate();
    }   

    @Override
    public int getComponentWidth() {
        return getWidth();
    }

    @Override
    public int getComponentHeight() {
        return getHeight();
    }

    @Override
    public int getComponentMinimumWidth() {
        return minimumWidth;
    }

    @Override
    public int getComponentMinimumHeight() {
        return minimumHeight;
   }

    @Override
    public int getComponentMaximumWidth() {
        return maximumWidth;
   }

    @Override
    public int getComponentMaximumHeight() {
        return maximumHeight;
    }

    @Override
    public void setBlocked(boolean bln, String string) {
        blocked = bln;
        if (! bln) {
            blockOwner = null;
        }
        else {
            blockOwner = string;
        }
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }    

    @Override
    public String getBlockOwner() {
        return blockOwner;
    }

    @Override
    public int getComponentId() {
        return InteractiveBoard.BOARD_ID;
    }

    @Override
    public void setComponentName(String string) {
        name = string;
    }

    @Override
    public String getComponentName() {
        return name;
    }

    @Override
    public void setComponentFont(FontModel fm) {
        setFont(Parsing.createFont(fm));
    }

    @Override
    public FontModel getComponentFont() {
        return Parsing.convertToFontModel(getFont());
    }

    @Override
    public void setComponentForeground(ColorModel cm) {
        setForeground(Parsing.createColor(cm));
    }

    @Override
    public ColorModel getComponentForeground() {
        return Parsing.convertToColorModel(getForeground());
    }

    @Override
    public void setComponentBackground(ColorModel cm) {
        cm.setOpaque(opaque);
        setBackground(Parsing.createColor(cm));
    }

    @Override
    public ColorModel getComponentBackground() {
        return Parsing.convertToColorModel(getBackground());
    }
    
    @Override
    public void setComponentOpaque(boolean bln) {
        opaque = bln;
        ColorModel cm = getComponentBackground();
        cm.setOpaque(bln);
        setComponentBackground(cm);
    }

    @Override
    public boolean isComponentOpaque() {
        return opaque;
    }

    @Override
    public void setComponentOwner(String string) {
        owner = string;
    }

    @Override
    public String getComponentOwner() {
        return owner;
    }

    @Override
    public void setComponentStatus(Status status) {
        this.status = status;
    }

    @Override
    public Status getComponentStatus() {
        return status;
    }   

    @Override
    public void setAsynchronous(boolean bln) {
        asynchronous = bln;
    }

    @Override
    public boolean isAsynchronous() {
        return asynchronous;
    }

    @Override
    public BoardContainer inflateContainer(ExternalizableContainer ec) {
        return inflate(ec);
    }
    
    /***
     * Inflates container
     * @param ec saved info
     * @return inflated container
     */
    private ContainerPanel inflate(ExternalizableContainer ec) {
        ContainerPanel container = null;
        switch (ec.getType()) {
            case TEXT_CONTAINER: {
                container = new TextContainerPanel(this, 
                        (ExternalizableTextContainer) ec);
                break;
            }
            case IMAGE_CONTAINER: {
                container = new ImageContainerPanel(this, 
                        (ExternalizableImageContainer) ec);
                break;
            }
            case FILE_CONTAINER: {
                container = new FileContainerPanel(this, 
                        (ExternalizableFileContainer) ec);
                break;
            }
        }
        return container;
    }

    @Override
    public void hear(BoardEvent event) {
        switch(event.getIndex()) {
            case CHANGE_SYNC_MODE_E: {
                ChangeSyncModeEvent changeSyncModeEvent = 
                        (ChangeSyncModeEvent) event;
                setAsynchronous(changeSyncModeEvent.isAsynchronous());
                break;
            }
            case POINT_E: {
                PointEvent pe = (PointEvent) event;
                /*int hash = pe.getUsername().hashCode();
                Color temp = new Color(hash);
                point(animationManager, pe.getX(), pe.getY(), 
                        new Color(temp.getRed(), 
                                temp.getGreen(), temp.getBlue(), 255));*/
                point(animationManager, pe.getX(), pe.getY(), Color.RED);
                break;
            }
            case CLEAR_BOARD_E: {
                clearBoard();
                break;
            }
            case SET_BOARD_CONTENT_E: {
                setBoardContent(((SetBoardContentEvent) event).
                        getSerializableContainers());
                break;
            }
            case ADD_CONTAINER_E: {
                addContainer(inflateContainer(((AddContainerEvent) event).
                        getSerializableContainer()));
                break;
            }
            case DELETE_CONTAINER_E: {
                deleteContainer(((DeleteContainerEvent) event).getId());
                break;
            }
            case CHANGE_BLOCK_E: {
                ChangeBlockEvent cbe = (ChangeBlockEvent) event;
                findContainer(cbe.getId()).setBlocked(cbe.isBlock(),
                        cbe.getUsername());
                break;
            }
            case RESIZE_E: {
                ResizeEvent resizeEvent = (ResizeEvent) event;
                BasicComponent component = resizeEvent.getId() == BOARD_ID?
                         this : findContainer(resizeEvent.getId());
                component.resizeComponent(resizeEvent.getLeft(), resizeEvent.getTop(),
                            resizeEvent.getWidth(), resizeEvent.getHeight());
                if (resizeEvent.isUnblock()) {
                    component.setBlocked(false, null);
                }
                break;
            }
            case MOVE_E: {
                MoveEvent moveEvent = (MoveEvent) event;
                BoardContainer container = findContainer(moveEvent.getId());
                container.moveComponent(moveEvent.getLeft(), moveEvent.getTop());
                if (moveEvent.isUnblock()) {
                    container.setBlocked(false, null);
                }
                break;
            }
            case CHANGE_NAME_E: {
                ChangeNameEvent changeNameEvent = (ChangeNameEvent) event;
                findContainer(changeNameEvent.getId()).setComponentName(changeNameEvent.getName());
                break;
            }
            case CHANGE_OWNER_E: {
                ChangeOwnerEvent changeOwnerEvent = (ChangeOwnerEvent) event;
                if (changeOwnerEvent.getId() == InteractiveBoard.BOARD_ID) {
                    setComponentOwner(changeOwnerEvent.getNewOwner());
                }
                else {
                    findContainer(changeOwnerEvent.getId()).setComponentOwner(
                            changeOwnerEvent.getNewOwner());
                }
                break;
            }
            case CHANGE_STATUS_E: {
                ChangeStatusEvent changeStatusEvent = (ChangeStatusEvent) event;
                if (changeStatusEvent.getId() == InteractiveBoard.BOARD_ID) {
                    setComponentStatus(changeStatusEvent.getNewStatus());
                }
                else {
                    findContainer(changeStatusEvent.getId()).setComponentStatus(
                            changeStatusEvent.getNewStatus());
                }
                break;
            }
            case CHANGE_COLOR_E: {
                ChangeColorEvent changeColorEvent = (ChangeColorEvent) event;
                if (changeColorEvent.getId() == InteractiveBoard.BOARD_ID) {
                    if (changeColorEvent.getForeground() != null) {
                        setComponentForeground(changeColorEvent.getForeground());
                    }
                    if (changeColorEvent.getBackground()!= null) {
                        setComponentBackground(changeColorEvent.getBackground());
                    }
                }
                else {
                    BoardContainer container = findContainer(changeColorEvent.getId());
                    if (changeColorEvent.getForeground() != null) {
                        container.setComponentForeground(changeColorEvent.getForeground());
                    }
                    if (changeColorEvent.getBackground() != null) {
                        container.setComponentBackground(changeColorEvent.getBackground());
                    }
                }
                break;
            }
            case CHANGE_OPAQUE_E: {
                ChangeOpaqueEvent changeOpaqueEvent = (ChangeOpaqueEvent) event;
                findContainer(changeOpaqueEvent.getId()).
                        setComponentOpaque(changeOpaqueEvent.isOpaque());
                break;
            }
            case CHANGE_FONT_E: {
                ChangeFontEvent changeFontEvent = (ChangeFontEvent) event;
                if (changeFontEvent.getId() == InteractiveBoard.BOARD_ID) {
                    setComponentFont(changeFontEvent.getFont());
                }
                else {
                    findContainer(changeFontEvent.getId()).
                            setComponentFont(changeFontEvent.getFont());
                }
                break;
            }
            case CHANGE_GENERAL_COLOR_E: {
                ChangeGeneralColorEvent changeGeneralFontEvent = (ChangeGeneralColorEvent) event;
                if (changeGeneralFontEvent.getForeground() != null) {
                    setGeneralContainerForeground(changeGeneralFontEvent.getForeground());
                }
                if (changeGeneralFontEvent.getBackground() != null) {
                    setGeneralContainerBackground(changeGeneralFontEvent.getBackground());
                }
                break;
            }
            case CHANGE_GENERAL_FONT_E: {
                setGeneralContainerFont(((ChangeGeneralFontEvent) event).getFont());
                break;
            }
            case CHANGE_GENERAL_OPAQUE_E: {
                setGeneralContainerOpaque(((ChangeGeneralOpaqueEvent) event).isOpaque());
                break;
            }
            case CHANGE_CONTAINER_LAYER_E: {
                ChangeContainerLayerEvent changeContainerLayerEvent = (ChangeContainerLayerEvent) event;
                setLayerPosition(findContainer(changeContainerLayerEvent.getId()),
                        changeContainerLayerEvent.getLayer());
                break;
            }
            case CLEAR_CONTAINER_E: {
                findContainer(((ClearContainerEvent) event).getId()).clearContainer();
                break;
            }
            case SET_CONTAINER_CONTENT_E: {
                SetContainerContentEvent scce = (SetContainerContentEvent) event;
                findContainer(scce.getId()).setContent(scce.
                        getSerializableContainer());
                break;
            }
            case CHANGE_TEXT_E: {
                ChangeTextEvent changeTextEvent = (ChangeTextEvent) event;
                ((TextContainer) findContainer(changeTextEvent.getId())).setText(changeTextEvent.getText());
                break;
            }
            case CHANGE_IMAGE_E: {
                ChangeImageEvent changeImageEvent = (ChangeImageEvent) event;
                ((ImageContainer) findContainer(changeImageEvent.getId()))
                        .setImage(changeImageEvent.getImage());
                break;
            }
            case CHANGE_FILE_E: {
                ChangeFileEvent changeFileEvent = (ChangeFileEvent) event;
                ((FileContainer) findContainer(changeFileEvent.getId()))
                        .setFile(changeFileEvent.getFile());
                break;
            }
        }
    }

    @Override
    public void hear(MainClientEvent mce) {
    }
    
    /***
     * Calculates shrunk board dimension. Checks containers
     * @return calculated dimension
     */
    public Dimension calculateShrunkBoard() {
        int newWidth = MINIMUM_BOARD_WIDTH;
        int newHeight = MINIMUM_BOARD_HEIGHT;
        for (ContainerPanel container : containers) {
            int right = container.getComponentLeft() + 
                    container.getComponentWidth() + BORDER_DELTA;
            if (right > newWidth) {
                newWidth = right;
            }
            int bottom = container.getComponentTop() + 
                    container.getComponentHeight() + BORDER_DELTA;
            if (bottom > newHeight) {
                newHeight = bottom;
            }
        }
        if (newWidth > getComponentWidth()) {
            newWidth = getComponentWidth();
        }
        if (newHeight > getComponentHeight()) {
            newHeight = getComponentHeight();
        }
        return new Dimension(newWidth, newHeight);
    }
   
    public CommandFacade getCommandFacade() {
        return commandFacade;
    }
    
    @Override
    public String getToolTipText() {
        return "<html>Interactive board" +
                "<br>" + "Id: " + getComponentId() + 
                "<br>" + "Owner: " + getComponentOwner() + 
                "<br>" + "Minimum user status: " +
                getComponentStatus() + "</html>";
    }

    @Override
    public int hashCode() {
        return toExternalizable().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BoardPanel other = (BoardPanel) obj;
        return hashCode() == other.hashCode();
    }   
    
    public String getUsername() {
        return commandFacade.getUsername();
    }
    
    public BoardContainer getContainer(int i) {
        return containers.get(i);
    }
    
    /***
     * Sets point mode enabled/disabled
     * @param pointMode enabled/disabled 
     */
    public void setPointMode(boolean pointMode) {
        this.pointMode = pointMode;
        if (pointMode) {
            pointPanel.setBounds(0, 0, getWidth(), getHeight());
            setLayer(pointPanel, containers.size() + 1);
            add(pointPanel);
        }
        else {
            remove(pointPanel);
            repaint();
        }
    }

    @Override
    public void point(AnimationManager am, int i, int i1, Color color) {
        // finding corresponding containers
        List<ContainerPanel> corr = new ArrayList<>();
        for (ContainerPanel container : containers) {
            if (Geometry.hasPoint(container, i, i1)) {
                corr.add(container);
            }
        }
        if (! corr.isEmpty()) {
            // finding container on the top
            int maxLayer = corr.get(0).getLayer();
            int index = 0;
            for (int j = 0; j < corr.size(); ++ j) {
                if (corr.get(j).getLayer() > maxLayer) {
                    maxLayer = corr.get(j).getLayer();
                    index = j;
                }
            }
            ContainerPanel target = corr.get(index);
            target.point(am, i - target.getX(), i1 - target.getY(), color);
        }
        else {
            // if no occurrences, point the board itself
            animationManager.addAnimation(
                    new PointAnimation(this, i, i1, color));
        }
    } 
    
    public boolean isComplexPainting() {
        return complexPainting;
    }

    public void setComplexPainting(boolean complexPainting) {
        this.complexPainting = complexPainting;
        for (ContainerPanel container : containers) {
            container.setComplexPainting(complexPainting);
        }
    }

    public AnimationManager getAnimationManager() {
        return animationManager;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        //g2d.scale(0.5, 0.5);
        super.paintComponent(g);
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
