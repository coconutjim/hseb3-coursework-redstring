package rslib.listeners;

/***
 * Represents a listener that can listen to board message events
 */
public interface BoardMessageListener {

    /***
     * Hears board message
     * @param message message
     */
    public void hearMessage(String message);
}
