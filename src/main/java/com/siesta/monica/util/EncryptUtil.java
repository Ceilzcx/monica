package com.siesta.monica.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptUtil {

    private EncryptUtil() {}

    public static byte[] encode411(byte[] password, byte[] seed) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] pass1 = digest.digest(password);
        digest.reset();
        byte[] pass2 = digest.digest(pass1);
        digest.reset();
        digest.update(seed);
        byte[] pass3 = digest.digest(pass2);
        for (int i = 0; i < pass3.length; i++) {
            pass3[i] = (byte) (pass3[i] ^ pass1[i]);
        }
        return pass3;
    }

}
