package com.siesta.monica.protocol;

import com.siesta.monica.io.ByteArrayOutputStream;
import io.netty.channel.Channel;

import java.io.IOException;

// send message to mysql
public abstract class MySQLCommand {
    protected final ByteArrayOutputStream outputStream;
    private final Channel channel;

    protected MySQLCommand(Channel channel) {
        this.outputStream = new ByteArrayOutputStream();
        this.channel = channel;
    }

    public void send() throws IOException {
        byte[] body = getData();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.writeInt(body.length, 3);
        buffer.writeInt(getSequenceNumber(), 1);
        buffer.writeBytes(body);

        channel.writeAndFlush(buffer.toBytes());
    }

    public abstract byte[] getData();

    public abstract byte getSequenceNumber();

}
