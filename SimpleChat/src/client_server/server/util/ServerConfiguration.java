package client_server.server.util;

/***
 * Represents server configuration, filled from config file
 */
public final class ServerConfiguration {

    /** Port for admin server */
    private static int adminPort;

    /** Port for user server */
    private static int userPort;

    /** Admin password */
    private static String adminPassword;

    public static int getAdminPort() {
        return adminPort;
    }

    public static void setAdminPort(int adminPort) {
        ServerConfiguration.adminPort = adminPort;
    }

    public static int getUserPort() {
        return userPort;
    }

    public static void setUserPort(int userPort) {
        ServerConfiguration.userPort = userPort;
    }

    public static String getAdminPassword() {
        return adminPassword;
    }

    public static void setAdminPassword(String adminPassword) {
        ServerConfiguration.adminPassword = adminPassword;
    }
}
