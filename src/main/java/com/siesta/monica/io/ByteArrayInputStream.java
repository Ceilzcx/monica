package com.siesta.monica.io;

import com.siesta.monica.common.MySQLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ByteArrayInputStream {
    private static final Logger log = LoggerFactory.getLogger(ByteArrayInputStream.class);

    private final java.io.ByteArrayInputStream inputStream;

    public ByteArrayInputStream(byte[] bytes) {
        this.inputStream = new java.io.ByteArrayInputStream(bytes);
    }

    public int readInt(int len) {
        int res = 0;
        for (int i = 0; i < len; i++) {
            res |= (inputStream.read() << (i << 3));
        }
        return res;
    }

    public long readLong(int len) {
        long res = 0;
        for (int i = 0; i < len; i++) {
            res |= ((long) inputStream.read() << (i << 3));
        }
        return res;
    }

    public String readString(int len) throws IOException {
        byte[] bytes = readBytes(len);
        return new String(bytes);
    }

    public String readEndString(int endSymbol) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int b; (b = inputStream.read()) != endSymbol;) {
            outputStream.writeInt(b, 1);
        }
        byte[] bytes = outputStream.toBytes();
        if (bytes == null || bytes.length == 0) {
            log.warn("read empty data");
            return null;
        }
        return new String(bytes);
    }

    public String readEndTerminatedString() {
        return readEndString(MySQLConstants.NULL_TERMINATED_STRING_DELIMITER);
    }

    public String readEndEOFString() {
        return readEndString(-1);
    }

    public byte[] readBytes(int len) throws IOException {
        byte[] bytes = new byte[len];
        int readLen = inputStream.read(bytes);
        if (readLen != len) {
            log.warn("inputStream has not enough data");
        }
        return bytes;
    }

    public boolean hasMoreData() {
        return inputStream.available() > 0;
    }

    public void skipData(int len) {
        long skip = inputStream.skip(len);
        if (skip != len) {
            log.warn("msg data is not enough: " + skip);
        }
    }

    public void close() throws IOException {
        inputStream.close();
    }

}
