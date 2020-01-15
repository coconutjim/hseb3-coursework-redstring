package server;

import rslib.cs.server.admin.AdminServer;
import java.io.IOException;

/***
 * Represents a remote server executor
 */
public class RemoteServer {
    public static void main(String[] args) {
        AdminServer adminServer = null;
        try {
            adminServer = new AdminServer();
            adminServer.start();
        }
        catch (IOException e) {
            if (adminServer != null) {
                adminServer.foldLog("Unable to start server: " + e.getMessage());
            }
        }
    }
}
