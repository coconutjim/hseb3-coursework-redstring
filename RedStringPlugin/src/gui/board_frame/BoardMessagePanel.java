package gui.board_frame;

import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;
import rslib.listeners.BoardMessageListener;

/***
 * Represents a panel that shows board messages
 */
public class BoardMessagePanel extends JPanel implements BoardMessageListener {
    
    /** Text area */
    private final JTextArea textArea;

    /***
     * Constructor
     * @param width panel width
     */
    public BoardMessagePanel(int width) {
        textArea = new JTextArea();
        initComponents(width);
    }
    
    /***
     * Initializes GUI components
     * @param width panel width
     */
    private void initComponents(int width) {
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("Calibri", Font.BOLD, 10));
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        setLayout(new MigLayout("", "right", "center"));
        JScrollPane scroll = new JScrollPane(textArea);
        add(scroll, new CC().width("" + width + ":" + width + ":" + width)
                .height("70:70:70"));
    }

    @Override
    public void hearMessage(String string) {
        if (textArea.getLineCount() > 50) {
            try {
                textArea.getDocument().remove(0, 
                        textArea.getText().indexOf("\n") + 1);
            }
            catch (BadLocationException e) {
                textArea.setText("");
            }
        }
        textArea.append(string + "\n");
    }
}
