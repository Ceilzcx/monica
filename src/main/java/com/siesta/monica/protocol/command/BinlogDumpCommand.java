package com.siesta.monica.protocol.command;

import com.siesta.monica.protocol.MySQLCommand;
import io.netty.channel.Channel;

import java.io.IOException;

public class BinlogDumpCommand extends MySQLCommand {
    private static final byte COM_BINLOG_DUMP = 0x12;
    private final int binlogPosition;
    private static final byte FLAG = 0x01;
    private final int serverId;
    private final String binlogFileName;

    public BinlogDumpCommand(Channel channel, int binlogPosition, int serverId, String binlogFileName) {
        super(channel);
        this.binlogPosition = binlogPosition;
        this.serverId = serverId;
        this.binlogFileName = binlogFileName;
    }

    /**
     * <pre>
     * 1            COM_BINLOG_DUMP
     * 4            binlog-pos
     * 2            flags
     * 4            server-id
     * string[EOF]  binlog-filename
     * </pre>
     */
    @Override
    public byte[] getData() {
        try {
            outputStream.writeInt(COM_BINLOG_DUMP);
            outputStream.writeInt(binlogPosition);
            outputStream.writeInt(FLAG, 2);
            outputStream.writeInt(0x00);
            outputStream.writeInt(serverId, 4);
            outputStream.writeEndTerminatedString(binlogFileName);
            return outputStream.toBytes();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }

    @Override
    public byte getSequenceNumber() {
        return 0;
    }
}
