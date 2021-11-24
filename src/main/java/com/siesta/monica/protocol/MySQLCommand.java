package com.siesta.monica.protocol;

import com.siesta.monica.io.ByteArrayOutputStream;
import io.netty.channel.Channel;

// send message to mysql
public abstract class MySQLCommand {
    protected final ByteArrayOutputStream outputStream;
    private final Channel channel;

    protected MySQLCommand(Channel channel) {
        this.outputStream = new ByteArrayOutputStream();
        this.channel = channel;
    }

    public void send() {
        byte[] body = getData();
        byte[] data = new byte[4 + body.length];
        data[0] = (byte)(body.length & 0xFF);
        data[1] = (byte) (body.length >>> 8);
        data[2] = (byte) (body.length >>> 16);
        data[3] = getSequenceNumber();

        System.arraycopy(body, 0, data, 4, body.length);

        channel.writeAndFlush(data);
    }

    public abstract byte[] getData();

    public abstract byte getSequenceNumber();

}
