package com.siesta.monica.io;

import com.siesta.monica.common.MySQLConstants;

import java.io.IOException;

public class ByteArrayOutputStream {
    private final java.io.ByteArrayOutputStream outputStream;

    public ByteArrayOutputStream() {
        outputStream = new java.io.ByteArrayOutputStream();
    }

    public void writeByte(int b) {
        outputStream.write(b);
    }

    public void writeBytes(byte[] bytes) throws IOException {
        outputStream.write(bytes);
    }

    public void writeInt(int data, int len) {
        for (int i = 0; i < len; i++) {
            // int类型在ByteArrayOutputStream转化为byte，作为一个字节写入
            // len<<3：一个字节8位
            outputStream.write(0xFF & (data >>> (i << 3)));
        }
    }

    public void writeInt(int data) {
        writeInt(data, 4);
    }

    public void writeLong(long data, int len) {
        for (int i = 0; i < len; i++) {
            outputStream.write((int) (0xFF & (data >>> (i << 3))));
        }
    }

    public void writeLong(long data) {
        writeLong(data, 8);
    }

    public void writeString(String data) throws IOException {
        outputStream.write(data.getBytes());
    }

    public void writeEndTerminatedString(String data) throws IOException {
        writeString(data);
        outputStream.write(MySQLConstants.NULL_TERMINATED_STRING_DELIMITER);
    }

    public void fillBytes(byte data, int len) {
        for (int i = 0; i < len; i++) {
            outputStream.write(data);
        }
    }

    public byte[] toBytes() {
        return outputStream.toByteArray();
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    public void close() throws IOException {
        outputStream.close();
    }

}
