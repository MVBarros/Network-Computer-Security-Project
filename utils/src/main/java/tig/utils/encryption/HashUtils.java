package tig.utils.encryption;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HashUtils {

    public static byte[] hashBytes(byte[] bytes) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            //should never happen
            throw new RuntimeException();
        }
    }

    public static boolean verifyMessageSignature(byte[] message, byte[] signature) {
        byte[] digest = hashBytes(message);
        return Arrays.equals(digest, signature);
    }
}
