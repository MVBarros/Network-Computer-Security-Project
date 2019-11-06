package tig.grpc.server.utils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtils {

    public SecretKey generateAESKey() {
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

    public SecretKeySpec getAesKey(byte[] encoding) {
        return new SecretKeySpec(encoding, 0, 16, "AES");
    }

    public byte[] encryptFile(byte[] originalFile, SecretKey key) {
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
}
