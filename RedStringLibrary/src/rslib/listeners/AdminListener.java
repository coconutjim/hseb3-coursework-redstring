package rslib.listeners;

import rslib.cs.protocol.events.admin.AdminEvent;

/***
 * Represents a listener that listen admin events
 */
public interface AdminListener {

    /***
     * Does actions depending on the event
     * @param event hired event
     */
    public void hear(AdminEvent event);
}
