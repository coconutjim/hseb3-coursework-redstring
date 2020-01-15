package rslib.cs.protocol.requests.to_server.admin;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/***
 * Represents logging in to server API adapter
 */
public class LoginAdminRequest implements AdminServerRequest {

    /** For better parsing */
    public static final long serialVersionUID = 83767236774845L;

    /** User name */
    private String username;

    /** Password */
    private byte[] password;

    /***
     * Constructor
     * @param username username
     * @param password password
     */
    public LoginAdminRequest(String username, byte[] password) {
        if (username == null) {
            throw new IllegalArgumentException("LoginToAPIRequest: username is null!");
        }
        if (password == null) {
            throw new IllegalArgumentException("LoginToAPIRequest: password is null!");
        }
        this.username = username;
        this.password = password;
    }

    /***
     * Constructor for externalization
     */
    public LoginAdminRequest() {
    }

    @Override
    public AdminServerRequestType getIndex() {
        return AdminServerRequestType.LOGIN_R;
    }

    public String getUsername() {
        return username;
    }

    public byte[] getPassword() {
        return password;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        //TODO: optimize
        out.writeUTF(username);
        out.writeObject(password);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        username = in.readUTF();
        password = (byte[]) in.readObject();
    }
}
