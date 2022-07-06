package com.siesta.monica.protocol.packet;

import com.siesta.monica.protocol.MySQLPacket;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthPacket extends MySQLPacket {
    public static final int SWITCH_STATUS = -2;
    public static final int MORE_STATUS = 1;
    private static final Logger log = LoggerFactory.getLogger(AuthPacket.class);
    private int status;
    private String pluginName;
    // caching_sha2_password | mysql_native_password
    private String authPluginData;

    public AuthPacket(ByteBuf byteBuf) {
        super(byteBuf);
    }

    @Override
    public void init() {
        /*
         *     type             byte[0]
         *     OkPacket         0x00
         *     ErrorPacket      0xFF
         *     FieldPacket      0x01-0xFA
         *     Row Data Packet  0x01-0xFA
         *     EOF Packet       0xFE
         */
        this.status = inputStream.readInt(1);
        log.info("auth response status: {}", this.status);
        if (status == SWITCH_STATUS) {
            pluginName = inputStream.readEndTerminatedString();
        } else if (status != MORE_STATUS) {
            String msg = inputStream.readEndEOFString();
            log.warn("auth connect error, error info: {}", msg);
            return;
        }
        authPluginData = inputStream.readEndEOFString();
    }

    public void logInfo() {
        log.info("plugin name: {}", pluginName);
        log.info("auth plugin data: {}", authPluginData);
    }

    public String getAuthPluginData() {
        return authPluginData;
    }

    public int getStatus() {
        return status;
    }
}
