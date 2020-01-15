package client_server;

import client_server.protocol.ChatInfo;
import util.DataManagement;

import java.util.Arrays;

/***
 * Holds info about lobby
 */
public class LobbyInfo implements ChatInfo {

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
            this.password = DataManagement.toHashMD5(password);
        }
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

}
