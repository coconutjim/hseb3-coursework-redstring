package rslib.cs.protocol.events.board.board;

import rslib.cs.protocol.events.board.BoardEvent;

/***
 * Represents send board hash event for validation
 */
public class SendHashEvent extends BoardEvent {

    /***
     * Constructor
     * @param hash board hash
     */
    public SendHashEvent(int hash) {
        super(hash);
    }

    /***
     * Constructor for externalization
     */
    public SendHashEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.SEND_HASH_E;
    }
}
