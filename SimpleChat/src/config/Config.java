package config;

import util.DataManagement;

import java.io.*;

/***
 * Represents config creating
 */
public class Config {

    /***
     * Creates config file
     * @param strings file content
     * @param filename creating config filename
     */
    private static void createConfig(String[] strings, String filename) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(filename, "UTF-8");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return;
        }
        for (String string : strings) {
            writer.println(string);
        }
        writer.close();
    }

    public static void main(String[] args) {

        String userServerPort = "UserServerPort: 8080";
        String adminServerPort = "AdminServerPort: 8081";
        String serverHost = "ServerHost: localhost";

        // Creating user config

        String[] strings1 = { serverHost, userServerPort };
        createConfig(strings1, "user.properties");

        // Creating admin config

        String[] strings2 = { serverHost, adminServerPort };
        createConfig(strings2, "admin.properties");

        // Creating server config
        String adminServerPassword = "qweqweqwep";
        byte[] pass = DataManagement.toHashMD5(adminServerPassword);
        adminServerPassword = "AdminPassword: ";
        for (byte b : pass) {
            adminServerPassword += Byte.toString(b);
        }

        String[] strings3 = { userServerPort, adminServerPort, adminServerPassword };
        createConfig(strings3, "server.properties");

    }
}
