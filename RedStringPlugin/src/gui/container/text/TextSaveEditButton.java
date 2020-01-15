package gui.container.text;

import gui.board.BoardPanel;
import gui.container.ContainerPanel;
import gui.container.SaveEditButton;
import javax.swing.JTextArea;
import rslib.commands.container.text.ChangeTextCommand;

/***
 * Represents a text save edit button
 */
public class TextSaveEditButton extends SaveEditButton {
    
    /** Link to text area */
    private JTextArea textArea;
    
    /** Old text */
    private String oldText;

    /***
     * Constructor
     * @param textArea text area
     * @param board link to board
     * @param container link to container
     */
    public TextSaveEditButton(JTextArea textArea, BoardPanel board, 
            ContainerPanel container) {
        super(board, container);
        if (textArea == null) {
            throw new IllegalArgumentException("TextSaveEditJButton: "
                    + "textArea is null!");
        }
        this.textArea = textArea;
        oldText = "";
    }

    @Override
    protected void editActions() {
        super.editActions();
        oldText = textArea.getText();
        textArea.setEditable(true);
    }

    @Override
    protected void saveActions() {
        super.saveActions();
        String newText = textArea.getText();
        if (! newText.equals(oldText)) { 
            textArea.setText(oldText);
            commandFacade.doCommand(new ChangeTextCommand(board, 
                    (TextContainerPanel)container, newText), true);
        }
        textArea.setEditable(false);
        oldText = "";
    } 
}
