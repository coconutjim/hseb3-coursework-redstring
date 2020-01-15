package rslib.cs.common;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Holds info about user
 */
public class User implements Externalizable {

    /** User name */
    private String username;

    /** User status */
    private Status status;

    /***
     * Constructor.
     * @param username username;
     * @param status user status
     */
    public User(String username, Status status) {
        if (username == null) {
            throw new NullPointerException("UserConnection: username is null!");
        }
        this.username = username;
        this.status = status;
    }

    /***
     * Constructor for externalization
     */
    public User() {
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(username);
        out.writeObject(status);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        username = in.readUTF();
        status = (Status) in.readObject();
    }
}
