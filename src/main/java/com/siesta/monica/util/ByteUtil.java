package com.siesta.monica.util;

import java.io.ByteArrayOutputStream;

public class ByteUtil {

    private ByteUtil() {}

    public static byte[] splitBytes(byte[] bytes, int offset, int len) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = offset; i < Math.min(offset + len, bytes.length); i++) {
            bos.write(bytes[i]);
        }
        return bos.toByteArray();
    }

    public static byte[] getBytes(byte[] bytes, int offset, byte end) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = offset; i < bytes.length; i++) {
            if (bytes[i] == end) {
                break;
            }
            bos.write(bytes[i]);
        }
        return bos.toByteArray();
    }

    public static int toShortValue(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8);
    }

    public static void printBytes(byte[] bytes) {
        printBytes(bytes, 0, bytes.length);
    }

    public static void printBytes(byte[] bytes, int offset, int len) {
        for (int i = offset; i < len; i++) {
            System.out.print(bytes[i] + " ");
        }
        System.out.println();
    }

}
