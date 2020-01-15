package client_server.client.util;

/***
 * Represents admin client configuration, filled from config file
 */
public final class AdminClientConfiguration {

    /** Server host */
    private static String host;

    /** Server port */
    private static int port;

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        AdminClientConfiguration.host = host;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        AdminClientConfiguration.port = port;
    }
}
