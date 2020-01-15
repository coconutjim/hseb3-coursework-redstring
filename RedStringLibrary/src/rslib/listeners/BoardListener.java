package rslib.listeners;

import rslib.cs.protocol.events.board.BoardEvent;
/***
 * Represents a listener that listen board events
 */
public interface BoardListener {

    /***
     * Does actions depending on the event
     * @param event hired event
     */
    public void hear(BoardEvent event);
}
