package rslib.cs.protocol.events.admin;

import java.io.*;
import java.util.ArrayList;

/***
 * Represents log file event
 */
public class LogFileEvent extends AdminEvent {

    /** For better parsing */
    public static final long serialVersionUID = 8266555276253432L;

    /** Log message */
    private ArrayList<String> content;

    /***
     * Constructor
     * @param content file content
     * @throws IOException if problems with reading occurred
     */
    public LogFileEvent(ArrayList<String> content) throws IOException {
        if (content == null) {
            throw new IllegalArgumentException("LogFileEvent: content is null!");
        }
        this.content = content;
    }

    /***
     * Constructor for externalization
     */
    public LogFileEvent() {
    }

    public ArrayList<String> getContent() {
        return content;
    }

    @Override
    public AdminEvent.AdminEventType getIndex() {
        return AdminEventType.LOG_FILE_E;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(content);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        content = (ArrayList<String>) in.readObject();
    }
}
