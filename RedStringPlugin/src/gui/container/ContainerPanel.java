package gui.container;

import gui.board.BoardPanel;
import gui.board.animation.AftermovingAnimation;
import gui.board.animation.Animatable;
import gui.board.animation.Animation;
import gui.board.animation.Geometry;
import gui.board.animation.Line;
import gui.board.animation.Pointable;
import gui.parsing.Parsing;
import gui.util.ColorScheme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import rslib.cs.common.Status;
import rslib.gui.container.BoardContainer;
import rslib.gui.container.ExternalizableContainer;
import rslib.gui.style.ColorModel;
import rslib.gui.style.FontModel;

/***
 * Represents board container in swing
 */
public abstract class ContainerPanel extends JPanel implements 
        BoardContainer, Pointable, Animatable {
    
    /** Container owner */
    private String owner;
    
    /** Container status */
    private Status status;
    
    /** Container id */
    protected int id;
    
    /** Container name */
    protected String name;
    
    /** Container opaque */
    protected boolean opaque;
    
    /** Link to board */
    protected BoardPanel board;
    
    /** Container type */
    protected ContainerType type;
    
    /** Container layer */
    private int layer;
     
    /** Minimum width */
    private int minimumWidth;
    
    /** Maximum width */
    private int maximumWidth;
    
    /** Minimum height */
    private int minimumHeight;
    
    /** Maximum height */
    private int maximumHeight;
    
    /** Container blocked */
    private boolean blocked;
    
    /** Block owner */
    private String blockOwner;
    
    /** Block label */
    protected StableLabel blockLabel;
    
    /** Name label */
    protected StableLabel nameLabel;
    
    /** Minimum container dimensions */
    public static final Map<ContainerType, Dimension> MINIMUM_DIMENSIONS;
    
    /** Minimum container dimensions */
    public static final Map<ContainerType, Dimension> MAXIMUM_DIMENSIONS;
    
    /** For gaps */
    public static int horizontal_gap;
    public static int vertical_gap;
    
    /** Complex container painting mode */
    private boolean complexPainting;
    
    /** Link to listener to change shadow */
    private MoveResizeListener mrl;
   
    /** Stroke */
    protected static int stroke;
    
    /** Arc diameter */
    protected static int arc;
    
    /** Shadow color */
    protected static Color shadowColor;
    
    /** Shadow size */
    protected static int shadowSize;
    
    /** Shadow offset  */
    protected static int shadowOffset;
    
    private List<Animation> animations;

    static {
        horizontal_gap = 10;
        vertical_gap = 10;
        stroke = 1;
        arc = 20;
        shadowColor = new Color(0, 0, 0, 150);
        shadowSize = 5;
        shadowOffset = 4;
        MINIMUM_DIMENSIONS = new HashMap<>();
        MINIMUM_DIMENSIONS.put(ContainerType.TEXT_CONTAINER, 
                new Dimension(200, 250));
        MAXIMUM_DIMENSIONS = new HashMap<>();
        MAXIMUM_DIMENSIONS.put(ContainerType.TEXT_CONTAINER, 
                new Dimension(1000, 1000));
        MINIMUM_DIMENSIONS.put(ContainerType.IMAGE_CONTAINER, 
                new Dimension(300, 350));
        MAXIMUM_DIMENSIONS.put(ContainerType.IMAGE_CONTAINER, 
                new Dimension(1000, 1000));
        MINIMUM_DIMENSIONS.put(ContainerType.FILE_CONTAINER, 
                new Dimension(300, 200));
        MAXIMUM_DIMENSIONS.put(ContainerType.FILE_CONTAINER, 
                new Dimension(1000, 1000));
    }
    
    /***
     * Constructor
     * @param type container type
     * @param owner container owner
     * @param id container id
     * @param board link to board
     * @param origin origin point
     */
    public ContainerPanel(ContainerType type, 
            String owner, int id, BoardPanel board,
            Point origin) {
        if (board == null) {
            throw new IllegalArgumentException("ContainerPanel: "
                    + "board is null!");
        }
        if (origin == null) {
            throw new IllegalArgumentException("ContainerPanel: "
                    + "center is null!");    
        }
        complexPainting = board.isComplexPainting();
        this.owner = owner;
        this.type = type;
        status = Status.COMMON;
        this.id = id;
        this.board = board;
        name = "";
        opaque = true;
        layer = BoardContainer.TO_FRONT;
        blocked = false;
        blockOwner = null;
        Dimension minDim = MINIMUM_DIMENSIONS.get(type);
        minimumWidth = minDim.width;
        minimumHeight = minDim.height;
        Dimension maxDim = MAXIMUM_DIMENSIONS.get(type);
        maximumWidth = maxDim.width;
        maximumHeight = maxDim.height;
        setBounds(origin.x, origin.y,
                minimumWidth, minimumHeight);
        setOpaque(! complexPainting);
        setFont(Parsing.createFont(board.getGeneralContainerFont()));
        String schemeName = type.toString() + " #1";
        ColorScheme scheme = ColorScheme.COLOR_SCHEMES.get(schemeName);
        Color fgColor;
        Color bgColor;       
        if (scheme != null) {
            fgColor = scheme.getForeground();
            bgColor = scheme.getBackground();
        }
        else {
            fgColor = Color.BLACK;
            bgColor = Color.BLUE;
        }
        setForeground(fgColor);
        setBackground(bgColor);
        initComponents();
    }
    
    /***
     * Constructor
     * @param board link to board
     * @param ec saved data
     */
    public ContainerPanel(BoardPanel board, ExternalizableContainer ec) {
        if (board == null) {
            throw new IllegalArgumentException("ContainerPanel: "
                    + "board is null!");
        }
        if (ec == null) {
            throw new IllegalArgumentException("ContainerPanel: "
                    + "serializableContainerPanel is null!");
        }
        complexPainting = board.isComplexPainting();
        owner = ec.getComponentOwner();
        status = ec.getComponentStatus();
        this.board = board;
        this.id = ec.getComponentId();
        setBounds(ec.getComponentLeft(), ec.getComponentTop(), 
                ec.getComponentWidth(), ec.getComponentHeight());
        layer = ec.getLayer();
        name = ec.getComponentName();
        opaque = ec.isComponentOpaque();
        blocked = ec.isBlocked();
        blockOwner = ec.getBlockOwner();      
        type = ec.getType();
        minimumWidth = ec.getComponentMinimumWidth();
        minimumHeight = ec.getComponentMinimumHeight();
        maximumWidth = ec.getComponentMaximumWidth();
        maximumHeight = ec.getComponentMaximumHeight();
        setOpaque(! complexPainting);
        setFont(Parsing.createFont(ec.getComponentFont()));
        setForeground(Parsing.createColor(ec.getComponentForeground()));
        setBackground(Parsing.createColor(ec.getComponentBackground()));  
        initComponents();
    }
    
    /***
     * Initializes GUI components and features
     */
    private void initComponents() {
        animations = new CopyOnWriteArrayList<>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        if (! complexPainting) {
            setBorder(isComponentOpaque()? 
                BorderFactory.createLineBorder(getForeground(), 1) : null);
        }
        add(Box.createVerticalStrut(vertical_gap));
        blockLabel = new StableLabel();
        blockLabel.setOpaque(true);
        blockLabel.setForeground(Color.BLACK);
        blockLabel.setText(blockOwner);
        blockLabel.setFont(new Font("Calibri", Font.BOLD, 20));
        blockLabel.setAlignmentX(CENTER_ALIGNMENT);
        if (blocked) {
            blockLabel.setText(blockOwner);
            blockLabel.setBackground(blockOwner.equals(board.getUsername())? 
                    Color.GREEN : Color.RED);
            blockLabel.setBorder(BorderFactory.
                    createLineBorder(Color.BLACK, stroke));
        }
        else {
            blockLabel.setBackground(getBackground());
        }
        add(blockLabel, "wrap");
        add(Box.createVerticalStrut(vertical_gap));
        nameLabel = new StableLabel();
        nameLabel.setText(name);
        nameLabel.setForeground(getForeground());
        nameLabel.setFont(getFont());
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(nameLabel, "wrap");
        add(Box.createVerticalStrut(vertical_gap));
        initFeatures();
    }
    
    /***
     * Sets general container features
     */
    private void initFeatures() {
        // Move and resize
        mrl = new MoveResizeListener(this, board, 
                board.getCommandFacade());
        addMouseListener(mrl);
        addMouseMotionListener(mrl);
        // Pop up menu
        addMouseListener(new ContainerPopupMenu(board, this).createPopUpMenu());
        // Tool tip
        ToolTipManager.sharedInstance().registerComponent(this);
    }
    
    /***
     * Gets free width of the container to fit the scroll
     * @return free width
     */
    public int getFreeWidth() {
        return getWidth() - 4 * horizontal_gap;
    }
    
    /***
     * Gets free height of the container to fit the scroll
     * @return free height
     */
    public int getFreeHeight() {
        return getHeight() - nameLabel.getHeight() - blockLabel.getHeight() - 
                3 * vertical_gap;
    }

    @Override
    public void moveComponent(int i, int i1) {
        Rectangle oldBounds = getBounds();
        setLocation(new Point(i, i1));
        Rectangle newBounds = getBounds();
        Line minLine = Geometry.defineContainersDistance(oldBounds, newBounds);
        board.getAnimationManager().addAnimation(
            new AftermovingAnimation(board, minLine, board.getForeground()));
    }

    @Override
    public int getComponentLeft() {
        return getX();
    }

    @Override
    public int getComponentTop() {
        return getY();
    }

    @Override
    public void resizeComponent(int i, int i1, int i2, int i3) {
        setBounds(i, i1, i2, i3);
        setPreferredSize(new Dimension(i2, i3));
        board.repaint();
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
    public int getComponentId() {
        return id;
    }

    @Override
    public void setComponentName(String string) {
        name = string;
        nameLabel.setText(name);
    }

    @Override
    public String getComponentName() {
        return name;
    }

    @Override
    public void setComponentFont(FontModel fm) {
        Font font = Parsing.createFont(fm);
        setFont(Parsing.createFont(fm));
        nameLabel.setFont(font);
    }

    @Override
    public FontModel getComponentFont() {
        return Parsing.convertToFontModel(getFont());
    }

    @Override
    public void setComponentForeground(ColorModel cm) {
        Color color = Parsing.createColor(cm);
        setForeground(color);
        nameLabel.setForeground(color);
        if (! complexPainting) {
            setBorder(opaque ? 
                BorderFactory.createLineBorder(getForeground(), stroke) : null);
        }
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
        if (! complexPainting) {
            setOpaque(bln);
            setBorder(bln? BorderFactory.createLineBorder(
                    getForeground(), stroke) : null);
        }
    }

    @Override
    public boolean isComponentOpaque() {
        return opaque;
    }

    @Override
    public void setLayer(int i) {
        layer = i;
    }

    @Override
    public int getLayer() {
        return layer;
    }

    @Override
    public ContainerType getType() {
        return type;
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
    public void setBlocked(boolean bln, String string) {
        blocked = bln;
        if (bln) {
            blockOwner = string;
            blockLabel.setText(blockOwner);
            blockLabel.setBackground(blockOwner.equals(board.getUsername())? 
                    Color.GREEN : Color.RED);
            blockLabel.setBorder(BorderFactory.
                    createLineBorder(Color.BLACK, stroke));
        }
        else {
            blockOwner = null;
            blockLabel.setText(blockOwner);
            blockLabel.setBackground(getBackground());
            blockLabel.setBorder(null);
        }
    }

    @Override
    public String getBlockOwner() {
        return blockOwner;
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }

    @Override
    public String getToolTipText() {
        return "<html>" + getType().toString() + 
                "<br>" + "Id: " + getComponentId()+ 
                "<br>" + "Owner: " + getComponentOwner() + 
                "<br>" + "Minimum user status: " + 
                getComponentStatus().toString() + "</html>";
    }   

    @Override
    public final void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height); 
        revalidate();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (complexPainting && isComponentOpaque()) {
            drawComponent(g2d);
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			RenderingHints.VALUE_ANTIALIAS_ON);
        for (Animation animation : animations) {
            animation.draw(g2d);
        }
    }
    
    /***
     * Draw complex container style
     * @param graphics link to graphics
     */
    private void drawComponent(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Shadow
        graphics.setColor(shadowColor);
            graphics.fillRoundRect(shadowOffset,// X position
                    shadowOffset,
                    width - stroke - shadowOffset, 
                    height - stroke - shadowOffset,
                    arc, arc);

        // Panel
        graphics.setColor(getBackground());
        graphics.fillRoundRect(0, 0, width - shadowSize, 
		height - shadowSize, arc, arc);
        
        graphics.setColor(getForeground());
        graphics.setStroke(new BasicStroke(stroke));
        graphics.drawRoundRect(0, 0, width - shadowSize, 
		height - shadowSize, arc, arc);

        // opt
        graphics.setStroke(new BasicStroke());
    }

    public void setComplexPainting(boolean complexPainting) {
        this.complexPainting = complexPainting;
        setOpaque(opaque && ! complexPainting);
        if (! complexPainting) {
            setBorder(opaque? 
                BorderFactory.createLineBorder(getForeground(), 1) : null);
        }
        else {
            setBorder(null);
        }
        mrl.setComplexPainting(complexPainting);
        repaint();
    }

    public boolean isComplexPainting() {
        return complexPainting;
    }

    public static int getArc() {
        return arc;
    }

    public static Color getShadowColor() {
        return shadowColor;
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
