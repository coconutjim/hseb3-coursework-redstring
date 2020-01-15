package client_server.server.util;

import client_server.protocol.ChatInfo;
import client_server.protocol.ProtocolConstants;
import util.DataManagement;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * Holds constants and static methods connected with networking
 */
public final class ServerUtil {

    /***
     * Writes message to client (usually short one, because writing is represented in blocking mode)
     * @param channel associated connection
     * @param command command
     * @throws IOException if something went wrong
     */
    public static void writeMessageToClient(SocketChannel channel, ChatInfo command) throws IOException {
        byte[] serialized = DataManagement.serialize(command);
        int length = serialized.length;
        ByteBuffer buffer = ByteBuffer.allocate(ProtocolConstants.COMMAND_LENGTH + length);
        buffer.putInt(length);
        buffer.put(serialized);
        buffer.flip();
        //TODO: see block
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    /***
     * Checks requested name for collisions appearance
     * @param requested requested name
     * @param names list of existing names
     * @return requested name if no collisions appear, new name with collision index otherwise
     */
    public static String checkNameCollisions(String requested, ArrayList<String> names) {
        //TODO: more pre conditions, more robust
        boolean collision = false;
        for (String name : names) {
            if (requested.equals(name)) {
                collision = true;
                break;
            }
        }
        if (! collision) {
            return requested; // if no collision appears, return requested name
        }
        else {
            Pattern p = Pattern.compile(requested + "\\((\\d+)\\)"); // create regexp that defines collisions
            final int INDEX_GROUP = 1; // place in regexp that defines the collision index
            int maximumIndex = 0;
            int index;
            for (String name : names) {
                Matcher m = p.matcher(name);
                if (m.matches()) { // if it is a collision, get index
                    index = Integer.parseInt(m.group(INDEX_GROUP));
                    if (index > maximumIndex) {
                        maximumIndex = index;
                    }
                }
            }
            //TODO: if somebody disconnects?????
            ++ maximumIndex; // increment index for new collision
            return requested + "(" + maximumIndex + ")"; // return new name with collision index
        }
    }
}
