package rslib.cs.protocol.events.board.container;

import rslib.cs.protocol.events.board.common.ComponentEvent;

/***
 * Represents delete container command
 */
public class DeleteContainerEvent extends ComponentEvent {

    /** For better parsing */
    public static final long serialVersionUID = 827654541212L;

    /***
     * Constructor
     * @param hash board hash
     * @param id component id
     */
    public DeleteContainerEvent(int hash, int id) {
        super(hash, id);
    }

    /***
     * Constructor for externalization
     */
    public DeleteContainerEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.DELETE_CONTAINER_E;
    }
}
