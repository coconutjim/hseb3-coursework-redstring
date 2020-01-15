package org.lev.redstringplugin;

import gui.NewLobbyFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import rslib.cs.protocol.events.message.ShowMessageEvent;
import rslib.listeners.MessageListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Team",
        id = "org.lev.redstringplugin.RedStringAction"
)
@ActionRegistration(
        iconBase = "org/lev/redstringplugin/chat.png",
        displayName = "#CTL_RedStringAction"
)
@ActionReferences({
    @ActionReference(path = "Toolbars/File", position = 0),
    @ActionReference(path = "Shortcuts", name = "D-R D-S")
})
@Messages("CTL_RedStringAction=RS")
public final class RedStringAction implements ActionListener,
        MessageListener {
    
    /** Message types */
    public static final Map<ShowMessageEvent.MessageType, Integer> messageTypes;
    
    static {
        messageTypes = new HashMap<>();
        messageTypes.put(ShowMessageEvent.MessageType.INFO, 
                JOptionPane.INFORMATION_MESSAGE);
        messageTypes.put(ShowMessageEvent.MessageType.ERROR, 
                JOptionPane.ERROR_MESSAGE);
    }

    /** Frame with lobby choosing */
    private final NewLobbyFrame newLobbyFrame;
    
    public RedStringAction() {
        newLobbyFrame = new NewLobbyFrame(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        newLobbyFrame.showFrame();
    }

    @Override
    public void hear(ShowMessageEvent sme) {
        JOptionPane.showMessageDialog(null, sme.getMessage(),
                    "Information", messageTypes.get(sme.getType())); 
    }
}
