package rslib.util;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/***
 * Represents working with files
 */
public class FileWorking {

    /***
     * Appends log message to log file
     * @param filename log file name
     * @param message log message
     */
    public static void logToFile(String filename, String message) {
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            writer.println(dateFormat.format(date) + ": " + message);
            writer.close();
        }
        catch (IOException e) {
            System.out.println("Error when writing log to file : " + e.getMessage());
        }
    }
}
