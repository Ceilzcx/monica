package com.siesta.monica.protocol;

import com.siesta.monica.CapabilityFlags;
import com.siesta.monica.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class HandShake {
    private static final Logger log = LoggerFactory.getLogger(HandShake.class);

    private byte packetSequenceNumber;
    private int protocolVersion;
    private String serverVersion;
    private long connectionId;
    private String authDataPart;
    private byte characterSet;
    private byte[] statusFlag;
    private int capabilities;
    private String authDataPart2;
    private String authPluginName;


    // HandShakeV10
    // https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol

    /**
     * <pre>
     *     Bytes            Name
     *      1              [0a] protocol version
     *      string[NUL]    server version
     *      4              connection id
     *      string[8]      auth-plugin-data-part-1
     *      1              [00] filler
     *      2              capability flags (lower 2 bytes)
     *    if more data in the packet:
     *      1              character set
     *      2              status flags
     *      2              capability flags (upper 2 bytes)
     *    if capabilities & CLIENT_PLUGIN_AUTH {
     *      1              length of auth-plugin-data
     *    } else {
     *      1              [00]
     *    }
     *      string[10]     reserved (all [00])
     *   if capabilities & CLIENT_SECURE_CONNECTION {
     *      string[$len]   auth-plugin-data-part-2 ($len=MAX(13, length of auth-plugin-data - 8))
     *   if capabilities & CLIENT_PLUGIN_AUTH {
     *      string[NUL]    auth-plugin name
     *   }
     * </pre>
     */
    public void readBytes(byte[] body) {
        int index = 0;
        protocolVersion = body[index++];

        byte[] versionBytes = ByteUtil.getBytes(body, index, (byte) 0x00);
        index += versionBytes.length + 1;
        serverVersion = new String(versionBytes, StandardCharsets.UTF_8);

        byte[] connectionBytes = ByteUtil.splitBytes(body, index, 4);
        connectionId = ((connectionBytes[0] & 0xFF) | ((connectionBytes[1] & 0xFF) << 8) |
                (connectionBytes[2] & 0xFF) << 16 | (connectionBytes[3] & 0xFF));
        index += 4;

        // 服务器发送的随机字符串，需要作为密码的加密条件
        authDataPart = new String(ByteUtil.splitBytes(body, index, 8), StandardCharsets.UTF_8);
        index += 8;

        byte filler = body[index];
        if (filler != 0) log.warn("filler != 0, filler = {}", filler);
        index++;

        int capabilityFlags = ByteUtil.toShortValue(body, index);
        index += 2;

        if (body.length > index) {
            characterSet = body[index++];

            statusFlag = ByteUtil.splitBytes(body, index, 2);
            index += 2;

            int capabilityFlags2 = ByteUtil.toShortValue(body, index);
            index += 2;

            // two capability flag
            capabilities = (capabilityFlags2 << 16) | capabilityFlags;

            // capabilities & CLIENT_PLUGIN_AUTH -> length of auth-plugin-data | 0
            byte authPluginDataLen = 0;
            if ((capabilities & CapabilityFlags.CLIENT_PLUGIN_AUTH) != 0) {
                authPluginDataLen = body[index];
            }
            index++;

            // reserved(all [00])
            ByteUtil.printBytes(body, index, 10);
            index += 10;

            if ((capabilities & CapabilityFlags.CLIENT_SECURE_CONNECTION) != 0) {
                // auth-plugin-data-part-2
                int pluginDataPart2Len = Math.max(13, authPluginDataLen - 8);
                // 最后一个字节为0，不包含在scramble中
                pluginDataPart2Len--;
                authDataPart2 = new String(ByteUtil.splitBytes(body, index, pluginDataPart2Len), StandardCharsets.UTF_8);
                index += pluginDataPart2Len;
            }

            if ((capabilities & CapabilityFlags.CLIENT_PLUGIN_AUTH) != 0) {
                authPluginName = new String(ByteUtil.getBytes(body, index, (byte) 0x00), StandardCharsets.UTF_8);
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

    public byte getCharacterSet() {
        return characterSet;
    }

    // seed
    public String getAuthDataPart() {
        return authDataPart;
    }
    // scramble
    public String getAuthDataPart2() {
        return authDataPart2;
    }

    public byte getPacketSequenceNumber() {
        return packetSequenceNumber;
    }

    public String getAuthPluginName() {
        return authPluginName;
    }

    public void setPacketSequenceNumber(byte packetSequenceNumber) {
        this.packetSequenceNumber = packetSequenceNumber;
    }

}
