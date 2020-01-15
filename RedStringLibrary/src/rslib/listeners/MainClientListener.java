package rslib.listeners;

import rslib.cs.protocol.events.main_client.MainClientEvent;

/***
 * Represents a listener that listen main client events
 */
public interface MainClientListener {

    /***
     * Does actions depending on the event
     * @param event hired event
     */
    public void hear(MainClientEvent event);
}
