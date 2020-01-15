package rslib.cs.common;

import rslib.util.DataManagement;

import java.io.*;
import java.util.Arrays;

/***
 * Holds info about lobby
 */
public class LobbyInfo implements Externalizable {

    /** Lobby name */
    private String lobbyName;

    /** If secured */
    private boolean secured;

    /** Server password (hashed) */
    private byte[] password;

    /***
     * General constructor
     * @param lobbyName lobby Name
     * @param secured if password exists
     * @param password lobby hashed password
     */
    public LobbyInfo(String lobbyName, boolean secured, String password) {
        if (lobbyName == null) {
            throw new NullPointerException("ServerInfo: lobbyName is null!");
        }
        this.lobbyName = lobbyName;
        this.secured = secured;
        if (secured) {
            if (password == null) {
                throw new NullPointerException("LobbyInfo: null password in secured lobby");
            }
            this.password = DataManagement.digest(password);
        }
    }

    /***
     * Constructor for externalization
     */
    public LobbyInfo() {
    }

    /***
     * Copy constructor, gets info without password itself
     * @param info full server info
     */
    private LobbyInfo(LobbyInfo info) {
        if (info.getLobbyName() == null) {
            throw new NullPointerException("ServerInfo: lobbyName is null!");
        }
        this.lobbyName = info.getLobbyName();
        this.secured = info.isSecured();
    }

    /***
     * Returns server info for user (without password)
     * @return server info without password
     */
    public LobbyInfo getInfoForUser() {
        return new LobbyInfo(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LobbyInfo that = (LobbyInfo) o;

        return lobbyName != null && that.lobbyName != null  && lobbyName.equals(that.lobbyName) &&
                secured == that.secured && Arrays.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        int result = lobbyName != null ? lobbyName.hashCode() : 0;
        result = 31 * result + (secured ? 1 : 0);
        result = 31 * result + (password != null ? Arrays.hashCode(password) : 0);
        return result;
    }

    public void setLobbyName(String lobbyName) {
        this.lobbyName = lobbyName;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public byte[] getPassword() {
        return password;
    }

    public boolean isSecured() {
        return secured;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(lobbyName);
        out.writeBoolean(secured);
        out.writeObject(password);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        lobbyName = in.readUTF();
        secured = in.readBoolean();
        password = (byte[]) in.readObject();
    }
}
