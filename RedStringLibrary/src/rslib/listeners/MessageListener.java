package rslib.listeners;

import rslib.cs.protocol.events.message.ShowMessageEvent;

/***
 * Represents a message listener
 */
public interface MessageListener {

    /***
     * Does actions depending on the event
     * @param event hired event
     */
    public void hear(ShowMessageEvent event);
}
