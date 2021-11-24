package com.siesta.monica.protocol;

import com.siesta.monica.io.ByteArrayInputStream;
import io.netty.buffer.ByteBuf;

// read packet from mysql
public abstract class MySQLPacket {
    private static final int HEADER_LENGTH = 4;

    private final byte sequenceNumber;
    private final int length;
    protected final ByteArrayInputStream inputStream;

    // parse the header of packet (length: 3byte, sequenceNumber: 1byte, body: others)
    protected MySQLPacket(ByteBuf byteBuf) {
        byte[] header = new byte[HEADER_LENGTH];
        byteBuf.readBytes(header);
        this.length = (header[0] & 0xFF) | ((header[1] & 0xFF) << 8) | ((header[2] & 0xFF) << 16);
        this.sequenceNumber = header[3];

        byte[] body = new byte[length];
        byteBuf.readBytes(body);
        this.inputStream = new ByteArrayInputStream(body);

        this.init();
    }

    public int getBodyLength() {
        return length;
    }

    public byte getSequenceNumber() {
        return sequenceNumber;
    }

    // parse the body
    public abstract void init();

    public abstract void logInfo();

}