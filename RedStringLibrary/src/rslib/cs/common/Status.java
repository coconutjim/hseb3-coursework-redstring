package rslib.cs.common;

/***
 * Holds user statuses
 */
public enum Status {

    READONLY("readonly user"),
    COMMON("common user"),
    MODERATOR("moderator"),
    LOBBY_ROOT("lobby root"),
    ADMINISTRATOR("administrator");

    /** String representation */
    private final String string;

    /***
     * Constructor
     * @param string string representation
     */
    private Status(String string) {
        this.string = string;
    }

    public String toString() {
        return string;

    }
}
