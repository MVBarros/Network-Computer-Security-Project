package tig.utils.encryption;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

public class EncryptionUtils {

    public static SecretKey generateAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
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

    public static EncryptedFile encryptFile(byte[] originalFile, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return new EncryptedFile(cipher.doFinal(originalFile), cipher.getIV());
        } catch (Exception e) {
            //Should never happen
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] decryptFile(EncryptedFile encryptedFile, SecretKeySpec key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(encryptedFile.getIv()));
            return cipher.doFinal(encryptedFile.getContent());
        } catch (Exception e) {
            //Should never happen
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] decryptbytesAES(byte[] encryptedBytes, SecretKeySpec key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptedBytes);
        } catch (Exception e) {
            //Should never happen
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] encryptBytesAES(byte[] bytes, SecretKeySpec key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            //Should never happen
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] decryptbytesRSAPub(byte[] encryptedBytes, PublicKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptedBytes);
        } catch (Exception e) {
            //Should never happen
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] encryptBytesRSAPub(byte[] bytes, PublicKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            //Should never happen
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] decryptbytesRSAPriv(byte[] encryptedBytes, PrivateKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptedBytes);
        } catch (Exception e) {
            //Should never happen
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] encryptBytesRSAPriv(byte[] bytes, PrivateKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            //Should never happen
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] generateIv() {
        // Generating IV.
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }
}

