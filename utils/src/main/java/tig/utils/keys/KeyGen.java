package tig.utils.keys;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class KeyGen {

    public static Key generateSessionKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            SecretKey secretKey = keyGenerator.generateKey();
            return secretKey;
        } catch (NoSuchAlgorithmException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }
}
