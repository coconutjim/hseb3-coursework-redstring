package rslib.listeners;

import rslib.cs.protocol.events.setup.SetupEvent;

/***
 * Represents a listener that listen to setup events
 */
public interface SetupListener {

    /***
     * Does actions depending on the event
     * @param event hired event
     */
    public void hear(SetupEvent event);
}
