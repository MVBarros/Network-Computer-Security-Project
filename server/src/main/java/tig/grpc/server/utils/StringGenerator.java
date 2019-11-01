package tig.grpc.server.utils;

import java.security.SecureRandom;
import java.util.Random;

public class StringGenerator {
    private static final String symbols = "ABCDEFGJKLMNPRSTUVWXYZ" +
            "0123456789" +
            "abcdefghijklmnopqrstuvwxyz" +
            "-_&~^*+-/()[]{}=?%$#!<>";

    private static final Random random = new SecureRandom();

    public static String RandomString(int length) {
        char[] buf = new char[length];

        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols.charAt(random.nextInt(symbols.length()));
        return new String(buf);
    }

}
