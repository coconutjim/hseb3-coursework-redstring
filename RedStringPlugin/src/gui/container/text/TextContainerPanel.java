package gui.container.text;

import gui.container.ContentScroll;
import gui.board.BoardPanel;
import gui.board.animation.AnimationManager;
import gui.board.animation.Geometry;
import gui.board.animation.PointAnimation;
import gui.container.ContainerPanel;
import static gui.container.ContainerPanel.vertical_gap;
import gui.parsing.Parsing;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.Box;
import rslib.commands.container.text.ChangeTextCommand;
import rslib.gui.container.ExternalizableContainer;
import rslib.gui.container.text.ExternalizableTextContainer;
import rslib.gui.container.text.TextContainer;
import rslib.gui.style.ColorModel;
import rslib.gui.style.FontModel;

/***
 * Represents a container that can hold text
 */
public class TextContainerPanel extends ContainerPanel implements TextContainer {
    
    /** Text content */
    private final ContainerTextArea textArea;
    
    /** Text scroll */
    private ContentScroll scroll;
    
    /** Edit save button */
    private TextSaveEditButton saveEditButton;
    
    /***
     * Constructor
     * @param type container type
     * @param owner container owner
     * @param textId text container id
     * @param id container id
     * @param board link to board
     * @param origin origin point
     * @param text text
     */
    public TextContainerPanel(ContainerType type, 
            String owner, int id, BoardPanel board,
            Point origin, String text) {
        this(type, owner, id, board, origin);
        textArea.setText(text);
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
    public TextContainerPanel(ContainerType type, 
            String owner, int id, BoardPanel board,
            Point origin) {
        super(type, owner, id, board, origin);
        textArea = new ContainerTextArea();
        initComponents();
    }
    
    /***
     * Constructor
     * @param board link to board
     * @param etc saved data
     */
    public TextContainerPanel(BoardPanel board, ExternalizableTextContainer
            etc) {
        super(board, etc);
        textArea = new ContainerTextArea();
        textArea.setText(etc.getText());
        initComponents();
    }
    
    /***
     * Initializes GUI components and features
     */
    private void initComponents() {     
        scroll = new ContentScroll(this, textArea);
        scroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                textArea.repaint();
            }
        });
        scroll.changeAppearance();
        textArea.setBackground(getBackground());
        textArea.setForeground(getForeground());
        textArea.setFont(getFont());
        scroll.setAlignmentX(CENTER_ALIGNMENT);
        add(scroll, "wrap");
        add(Box.createVerticalStrut(vertical_gap));
        
        saveEditButton = new TextSaveEditButton(textArea, board, this);
        saveEditButton.setAlignmentX(CENTER_ALIGNMENT);
        saveEditButton.setEnabled(opaque);
        add(saveEditButton, "wrap");
        //saveEditButton.setVisible(opaque);
        add(Box.createVerticalStrut(vertical_gap));
        initFeatures();
    }
    
    @Override
    public int getFreeHeight() {
        return super.getFreeHeight() - saveEditButton.getHeight() 
                - 2 * vertical_gap;
    }
    
    /***
     * Sets text container features
     */
    private void initFeatures() {
        textArea.setTransferHandler(new TextTransferHandler(this));
        final Clipboard clipboard = Toolkit.
                getDefaultToolkit().getSystemClipboard();
        textArea.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if ((e.getKeyCode() == KeyEvent.VK_C) && 
                        ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
                    if (textArea.getSelectedText() != null) {
                        StringSelection selection = new StringSelection(
                                textArea.getSelectedText());
                        clipboard.setContents(selection, selection);
                    }
                }
                if ((e.getKeyCode() == KeyEvent.VK_V) && 
                        ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
                    if (textArea.isEditable()) {
                        Transferable contents = clipboard.getContents(null);
                        if (contents != null &&
                            contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                            try {
                                String text = (String) contents.
                                        getTransferData(DataFlavor.stringFlavor);
                                textArea.insert(text, 
                                        textArea.getCaretPosition());
                            }
                            catch (UnsupportedFlavorException | IOException ex) {
                                // ???
                            }
                        }
                    }
                }
            }
        });
    }
    
    /***
     * Handles text from DND file
     * @param text text from file
     */
    public void handleTextFromFile(String text) {
        board.getCommandFacade().doCommand(new 
            ChangeTextCommand(board, this, text), true);
    }

    @Override
    public void setComponentBackground(ColorModel cm) {
        super.setComponentBackground(cm); 
        Color color = Parsing.createColor(cm);
        textArea.setBackground(color);
        scroll.changeAppearance();
    }

    @Override
    public void setComponentOpaque(boolean bln) {
        super.setComponentOpaque(bln);
        scroll.changeAppearance();
        saveEditButton.setEnabled(bln);
        saveEditButton.createIcon(getForeground(), bln);
        repaint();
    }

    @Override
    public void setComponentForeground(ColorModel cm) {
        super.setComponentForeground(cm); 
        Color color = Parsing.createColor(cm);
        textArea.setForeground(color);
        scroll.changeAppearance();
        saveEditButton.createIcon(color, isComponentOpaque());
    }

    @Override
    public void setComponentFont(FontModel fm) {
        super.setComponentFont(fm); 
        Font font = Parsing.createFont(fm);
        textArea.setFont(font);
        revalidate();
    }
    
    @Override
    public void setText(String string) {
        textArea.setText(string);
    }

    @Override
    public void appendText(String string) {
        textArea.append(string);
    }

    @Override
    public String getText() {
        return textArea.getText();
    } 

    @Override
    public void clearContainer() {
        textArea.setText("");
    }

    @Override
    public void setContent(ExternalizableContainer ec) {
        textArea.setText(((ExternalizableTextContainer) ec).getText());
    }
    
    @Override
    public ExternalizableContainer toExternalizable() {
        return new ExternalizableTextContainer(getComponentOwner(), 
                getComponentStatus(), getComponentLeft(), 
                getComponentTop(), getComponentWidth(), getComponentHeight(),
                getComponentMinimumWidth(), getComponentMinimumHeight(),
                getComponentMaximumWidth(), getComponentMaximumHeight(),
                getComponentName(), getComponentId(),
                getComponentFont(), isComponentOpaque(), 
                getComponentForeground(), getComponentBackground(),
                getLayer(), getType(), isBlocked(), getBlockOwner(), getText());
    }   
    
    @Override
    public String getToolTipText() {
        String text = super.getToolTipText();
        text += "<html><br> Correct file extensiions:";
        for (String extension : TextTransferHandler.TEXT_FILE_EXTENSIONS) {
            text += "<br>" + extension;
        }
        text += "</html>";
        return text;
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
            am.addAnimation(
                    new PointAnimation(textArea, i - scroll.getX() + bounds.x, 
                    i1 - scroll.getY() + bounds.y, color));
            return;
        }
        if (Geometry.hasPoint(saveEditButton, i, i1)) {
            am.addAnimation(new PointAnimation(
                    saveEditButton, i - saveEditButton.getX(), 
                    i1 - saveEditButton.getY(), color));
            return;
        }
        am.addAnimation(new PointAnimation(this, i, i1, color));
    }
}
