package rslib.cs.protocol.events.board.board;

import rslib.cs.protocol.events.board.BoardEvent;

/***
 * Represents clear board event
 */
public class ClearBoardEvent extends BoardEvent {

    /** For better parsing */
    public static final long serialVersionUID = 276487628756234L;

    /***
     * Constructor
     * @param hash board hash
     */
    public ClearBoardEvent(int hash) {
        super(hash);
    }

    /***
     * Constructor for externalization
     */
    public ClearBoardEvent() {
    }

    @Override
    public BoardEventType getIndex() {
        return BoardEventType.CLEAR_BOARD_E;
    }
}
