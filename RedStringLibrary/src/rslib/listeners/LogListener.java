package rslib.listeners;


/***
 * Represents a listener that listen main client events
 */
public interface LogListener {

    /***
     * Does log actions
     * @param log log message
     */
    public void hearLog(String log);
}
