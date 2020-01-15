package client_server.server.util;

import java.util.HashMap;
import java.util.Map;

/***
 * Holds constants connected with user status
 */
public final class Status {

    /** User connection status */
    public static final byte USER_STATUS_READONLY = 50;
    public static final byte USER_STATUS_COMMON_USER = 51;
    public static final byte USER_STATUS_MODERATOR = 52;
    public static final byte USER_STATUS_ROUTE = 53;
    public static final byte USER_STATUS_ADMINISTRATOR = 54;

    public static final Map<Byte, String> STATUS_STRINGS;

    static {
        STATUS_STRINGS = new HashMap<Byte, String>();
        STATUS_STRINGS.put(USER_STATUS_READONLY, "read only user");
        STATUS_STRINGS.put(USER_STATUS_COMMON_USER, "common user");
        STATUS_STRINGS.put(USER_STATUS_MODERATOR, "moderator");
        STATUS_STRINGS.put(USER_STATUS_ROUTE, "lobby route");
        STATUS_STRINGS.put(USER_STATUS_ADMINISTRATOR, "administrator");
    }
}
