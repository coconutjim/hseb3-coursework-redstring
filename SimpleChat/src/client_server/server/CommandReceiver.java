package client_server.server;

import client_server.server.util.Status;

import java.nio.channels.SelectionKey;
import java.util.ArrayList;

/***
 * Holds info about sending command and its receivers
 */
public class CommandReceiver {

    /** If the command is to send to all receivers */
    private boolean toAll;

    /** Minimum user status (if to all) */
    private byte minimumUserStatus;

    /** Specified receivers */
    private ArrayList<SelectionKey> receivers;

    /***
     * Constructor
     * @param minimumUserStatus minimum user status for sending command
     */
    public CommandReceiver(byte minimumUserStatus) {
        toAll = true;
        if (minimumUserStatus < Status.USER_STATUS_READONLY ||
                minimumUserStatus > Status.USER_STATUS_ADMINISTRATOR) {
            throw new IllegalArgumentException("CommandReceiver: illegal status!");
        }
        this.minimumUserStatus = minimumUserStatus;
    }

    /***
     * Constructor
     * @param receiver current receiver
     */
    public CommandReceiver(SelectionKey receiver) {
        toAll = false;
        if (receiver == null) {
            throw new NullPointerException("Command receiver: receiver is null!");
        }
        receivers = new ArrayList<SelectionKey>();
        receivers.add(receiver);
    }

    /***
     * Adds additional receiver
     * @param receiver current receiver
     */
    public void addReceiver(SelectionKey receiver) {
        if (receiver == null) {
            throw new NullPointerException("Command receiver: receiver is null!");
        }
        if (receivers == null) {
            receivers = new ArrayList<SelectionKey>();
        }
        receivers.add(receiver);
    }

    public boolean isToAll() {
        return toAll;
    }

    public byte getMinimumUserStatus() {
        return minimumUserStatus;
    }

    public ArrayList<SelectionKey> getReceivers() {
        return receivers;
    }
}
