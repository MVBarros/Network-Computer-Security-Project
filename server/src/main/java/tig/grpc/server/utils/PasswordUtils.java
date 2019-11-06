package tig.grpc.server.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class PasswordUtils {

    public static final int iterations = 1000;
    private static final int saltSize = 1000;

    public static byte[] generateRandomSalt() {
        //Beter to let JVM decide which SecureRandom it wants to use
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[saltSize];
        sr.nextBytes(salt);
        return salt;
    }

    /**
     * Generates a Password Hash for a given password
     *
     * @param password the password to hash
     * @return Hash String of password
     */
    public static byte[] generateStrongPasswordHash(String password, byte[] salt, int iterations){
        try {

            char[] chars = password.toCharArray();
            PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA2");
            return skf.generateSecret(spec).getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            //Should never Happen
            throw new RuntimeException();
        }

    }


    public static boolean validatePassword(byte[] originalPassword, byte[] hashedPassword) {
        if (originalPassword.length != hashedPassword.length) {
            return false;
        }
        for (int i = 0; i < originalPassword.length; ++i) {
            if (originalPassword[i] != hashedPassword[i]) {
                return false;
            }
        }
        return true;

    }

}
