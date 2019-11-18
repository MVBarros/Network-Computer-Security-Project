package tig.utils;

import java.security.SecureRandom;
import java.util.Random;

public class StringGenerator {
    private static final String symbols = "ABCDEFGJKLMNPRSTUVWXYZ" +
            "0123456789" +
            "abcdefghijklmnopqrstuvwxyz" +
            "-_&~^*+-/()[]{}=?%$#!<>.:,;";

    private static final String noMetaSymbols = "ABCDEFGJKLMNPRSTUVWXYZ" +
            "0123456789" +
            "abcdefghijklmnopqrstuvwxyz";

    private static final Random random = new SecureRandom();

    public static String randomString(int length) {
        char[] buf = new char[length];

        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols.charAt(random.nextInt(symbols.length()));
        return new String(buf);
    }

    public static String randomStringNoMetacharacters(int length) {
        char[] buf = new char[length];

        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = noMetaSymbols.charAt(random.nextInt(noMetaSymbols.length()));
        return new String(buf);
    }
}
