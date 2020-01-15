package rslib.listeners;

import rslib.cs.protocol.events.chat.ChatEvent;

/***
 * Represents a listener that listen chat events
 */
public interface ChatListener {

    /***
     * Does actions depending on the event
     * @param event hired event
     */
    public void hear(ChatEvent event);
}
