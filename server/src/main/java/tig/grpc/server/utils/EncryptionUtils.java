package tig.grpc.server.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtils {

    public static SecretKey generateAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // for example
            SecretKey secretKey = keyGen.generateKey();
            return secretKey;
        } catch (NoSuchAlgorithmException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }

    public static SecretKeySpec getAesKey(byte[] encoding) {
        return new SecretKeySpec(encoding, 0, 16, "AES");
    }

    public static byte[] encryptFile(byte[] originalFile, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(originalFile);
        } catch (Exception e) {
            //Should never happen
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] decryptFile(byte[] encryptedFile, SecretKeySpec key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptedFile);
        } catch (Exception e) {
            //Should never happen
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
