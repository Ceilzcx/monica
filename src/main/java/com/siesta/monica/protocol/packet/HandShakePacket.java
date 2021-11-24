package com.siesta.monica.protocol.packet;

import com.siesta.monica.common.CapabilityFlags;
import com.siesta.monica.protocol.MySQLPacket;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HandShakePacket extends MySQLPacket {
    private static final Logger log = LoggerFactory.getLogger(HandShakePacket.class);

    private int protocolVersion;
    private String serverVersion;
    private long connectionId;
    private byte[] authDataPart;
    private int characterSet;
    private int statusFlag;
    private int capabilities;
    private byte[] authDataPart2;
    private String authPluginName;

    public HandShakePacket(ByteBuf byteBuf) {
        super(byteBuf);
    }

    /**
     * <pre>
     * Bytes            Name
     * 1                [0a] protocol version
     * string[NUL]      server version
     * 4                connection id
     * string[8]        auth-plugin-data-part-1
     * 1                [00] filler
     * 2                capability flags (lower 2 bytes)
     * if more data in the packet:
     * 1                character set
     * 2                status flags
     * 2                capability flags (upper 2 bytes)
     * if capabilities & CLIENT_PLUGIN_AUTH {
     * 1                length of auth-plugin-data
     * } else {
     * 1                [00]
     * }
     * string[10]       reserved (all [00])
     * if capabilities & CLIENT_SECURE_CONNECTION {
     * $len=MAX(13, length of auth-plugin-data - 8)
     * string[$len]     auth-plugin-data-part-2
     * if capabilities & CLIENT_PLUGIN_AUTH {
     * string[NUL]      auth-plugin name
     * }
     * </pre>
     */
    public void init() {
        try {
            this.protocolVersion = inputStream.readInt(1);
            this.serverVersion = inputStream.readEndTerminatedString();
            this.connectionId = inputStream.readInt(4);
            this.authDataPart = inputStream.readBytes(8);

            int filler = inputStream.readInt(1);
            if (filler != 0) log.warn("filler != 0, filler = {}", filler);

            int capabilityFlag = inputStream.readInt(2);

            if (inputStream.hasMoreData()) {
                this.characterSet = inputStream.readInt(1);
                this.statusFlag = inputStream.readInt(2);

                int capabilityFlag2 = inputStream.readInt(2);
                // two capability flag
                this.capabilities = (capabilityFlag2 << 16) | capabilityFlag;

                // capabilities & CLIENT_PLUGIN_AUTH -> length of auth-plugin-data | 0
                int authPluginDataLen = 0;
                if ((this.capabilities & CapabilityFlags.CLIENT_PLUGIN_AUTH) != 0) {
                    authPluginDataLen = inputStream.readInt(1);
                }
                // reserved(all [00])
                inputStream.skipData(10);

                if ((capabilities & CapabilityFlags.CLIENT_SECURE_CONNECTION) != 0) {
                    // auth-plugin-data-part-2
                    int pluginDataPart2Len = Math.max(13, authPluginDataLen - 8);
                    // 最后一个字节为0，不包含在scramble中
                    authDataPart2 = inputStream.readBytes(pluginDataPart2Len);
                }

                if ((capabilities & CapabilityFlags.CLIENT_PLUGIN_AUTH) != 0) {
                    authPluginName = inputStream.readEndTerminatedString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void logInfo() {
        log.info("protocol version: {}", protocolVersion);
        log.info("server version: {}", serverVersion);
        log.info("connectionId: {}", connectionId);
        log.info("auth-plugin-data-part: {}", authDataPart);
        log.info("character set: {}", characterSet);
        log.info("capabilities: {}", capabilities);
        log.info("status flag: {}", statusFlag);
        log.info("plugin data part2: {}", authDataPart2);
        log.info("auth plugin name: {}", authPluginName);
    }

    public int getCharacterSet() {
        return characterSet;
    }

    public byte[] getAuthDataPart() {
        return authDataPart;
    }

    public byte[] getAuthDataPart2() {
        return authDataPart2;
    }

    public String getAuthPluginName() {
        return authPluginName;
    }
}
