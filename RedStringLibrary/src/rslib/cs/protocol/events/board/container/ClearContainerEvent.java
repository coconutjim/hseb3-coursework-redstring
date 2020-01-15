package rslib.cs.protocol.events.board.container;

import rslib.cs.protocol.events.board.common.ComponentEvent;

/***
 * Represents clear container event
 */
public class ClearContainerEvent extends ComponentEvent {

    /** For better parsing */
    public static final long serialVersionUID = 8617432423L;

    /***
     * Constructor
     * @param hash board hash
     * @param id container id
     */
    public ClearContainerEvent(int hash, int id) {
        super(hash, id);
    }

    /***
     * Constructor for externalization
     */
    public ClearContainerEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CLEAR_CONTAINER_E;
    }
}
