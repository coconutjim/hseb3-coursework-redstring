package rslib.cs.server.util;

import rslib.cs.common.Status;
import rslib.cs.common.UserConnection;

import java.util.ArrayList;
import java.util.List;

/***
 * Holds info about sending command and its receivers
 */
public class CommandReceiver {

    /** Minimum user status (if to all) */
    private Status minimumUserStatus;

    /** Specified receivers */
    private List<UserConnection> receivers;

    /***
     * Constructor
     * @param receiver current receiver
     */
    public CommandReceiver(UserConnection receiver) {
        if (receiver == null) {
            throw new IllegalArgumentException("Command receiver: receiver is null!");
        }
        receivers = new ArrayList<>();
        receivers.add(receiver);
        minimumUserStatus = Status.READONLY;
    }

    /***
     * Constructor
     * @param receivers specified receivers
     */
    public CommandReceiver(List<UserConnection> receivers, Status minimumUserStatus) {
        if (receivers == null) {
            throw new IllegalArgumentException("Command receiver: receivers is null!");
        }
        if (minimumUserStatus.ordinal() < Status.READONLY.ordinal()
                || minimumUserStatus.ordinal() > Status.ADMINISTRATOR.ordinal()) {
            throw new IllegalArgumentException("CommandReceiver: illegal minimum status!");
        }
        this.minimumUserStatus = minimumUserStatus;
        this.receivers = new ArrayList<>();
        this.receivers.addAll(receivers);
    }

    /***
     * Adds additional receiver
     * @param receiver current receiver
     */
    public void addReceiver(UserConnection receiver) {
        if (receiver == null) {
            throw new IllegalArgumentException("Command receiver: receiver is null!");
        }
        if (receivers == null) {
            receivers = new ArrayList<>();
        }
        receivers.add(receiver);
    }

    /***
     * Removes receiver
     * @param receiver current receiver
     */
    public void removeReceiver(UserConnection receiver) {
        if (receiver == null) {
            throw new IllegalArgumentException("Command receiver: receiver is null!");
        }
        if (receivers != null) {
            receivers.remove(receiver);
        }
    }

    public Status getMinimumUserStatus() {
        return minimumUserStatus;
    }

    public List<UserConnection> getReceivers() {
        return receivers;
    }
}
