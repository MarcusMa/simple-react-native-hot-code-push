package com.marcus.lib.codepush.util;

/**
 * Created by marcus on 17/4/11.
 */

public class HexUtils {

    public static byte[] hexStr2Bytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (char2Byte(hexChars[pos]) << 4 | char2Byte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte char2Byte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

}
