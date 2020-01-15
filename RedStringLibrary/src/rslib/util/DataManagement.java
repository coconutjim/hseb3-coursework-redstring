package rslib.util;

import rslib.cs.protocol.RedStringInfo;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/***
 * Represents some methods connected to the data management
 */
public class DataManagement {

    /***
     * Hashes string (MD5)
     * @param string string
     * @return hash
     */
    public static byte[] digest(String string) {
        if (string == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return md.digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            // nothing to do here
        }
        return null;
    }

    /***
     * Serializes data
     * @param serializable data
     * @return serialized data
     * @throws java.io.IOException if something went wrong
     */
    public static byte[] serialize(RedStringInfo serializable) throws IOException {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(arrayOutputStream);
        objectOutputStream.writeObject(serializable);
        byte[] objData = arrayOutputStream.toByteArray();
        arrayOutputStream.close();
        objectOutputStream.close();
        return objData;
    }

    /***
     * Inflates data
     * @param buffer data
     * @return inflated data data
     * @throws java.io.IOException if something went wrong
     * @throws ClassNotFoundException if data is wrong
     */
    public static Object inflate(ByteBuffer buffer) throws IOException, ClassNotFoundException {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(buffer.array());
        ObjectInputStream objectInputStream = new ObjectInputStream(arrayInputStream);
        Object object = objectInputStream.readObject();
        arrayInputStream.close();
        objectInputStream.close();
        return object;
    }
}
