package rslib.cs.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * Provides methods for data checking
 */
public class DataChecking {

    /** Constants */
    public static final String USERNAME_RULES = "Username can consists only of English letters, numbers and underscores.\n" +
            "It can not be longer than 12 symbols and shorter than 5 symbols!";
    public static final String LOBBY_NAME_RULES = "Lobby name can consists only of English letters, numbers and underscores!\n" +
            "It can not be longer than 12 symbols and shorter than 5 symbols!";
    public static final String LOBBY_PASSWORD_RULES = "Lobby password can consists only of English letters, numbers and underscores!\n" +
            "It can not be longer than 12 symbols!";

    /***
     * Checks if username is valid
     * @param username requested username
     * @return true if valid, false otherwise
     */
    public static boolean isUsernameValid(String username) {
        Pattern pattern = Pattern.compile("^\\w{5,12}$");
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    /***
     * Checks if user status is valid
     * @param status requested user status
     * @return true if valid, false otherwise
     */
    public static boolean isUserStatusValid(Status status) {
        return status.ordinal() >= Status.READONLY.ordinal() &&
                status.ordinal() <= Status.ADMINISTRATOR.ordinal();
    }

    /***
     * Checks if username is valid
     * @param lobbyName requested lobby name
     * @return true if valid, false otherwise
     */
    public static boolean isLobbyNameValid(String lobbyName) {
        Pattern pattern = Pattern.compile("^\\w{5,12}$");
        Matcher matcher = pattern.matcher(lobbyName);
        return matcher.matches();
    }

    /***
     * Checks if username is valid
     * @param lobbyPassword requested lobby password
     * @return true if valid, false otherwise
     */
    public static boolean isLobbyPasswordValid(String lobbyPassword) {
        if (lobbyPassword == null) {
            return true;
        }
        Pattern pattern = Pattern.compile("^\\w{0,12}$");
        Matcher matcher = pattern.matcher(lobbyPassword);
        return matcher.matches();
    }
}
