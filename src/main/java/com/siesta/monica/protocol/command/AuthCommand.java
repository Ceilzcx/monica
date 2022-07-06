package com.siesta.monica.protocol.command;

import com.siesta.monica.common.CapabilityFlags;
import com.siesta.monica.common.MySQLConstants;
import com.siesta.monica.protocol.MySQLCommand;
import com.siesta.monica.protocol.packet.HandShakePacket;
import com.siesta.monica.util.EncryptUtil;
import io.netty.channel.Channel;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class AuthCommand extends MySQLCommand {
    private final HandShakePacket handShakePacket;
    private final String username;
    private final String password;
    private final String schema;

    public AuthCommand(Channel channel, HandShakePacket handShakePacket, String username, String password, String schema) {
        super(channel);

        this.handShakePacket = handShakePacket;
        this.username = username;
        this.password = password;
        this.schema = schema;
    }

    /**
     * <pre>
     * Bytes            Name
     * 4                capability flags, CLIENT_PROTOCOL_41 always set
     * 4                max-packet size
     * 1                character set
     * string[23]       reserved (all [0])
     * string[NUL]      username
     * if capabilities & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA {
     * lenenc-int       length of auth-response
     * string[n]        auth-response
     * } else if capabilities & CLIENT_SECURE_CONNECTION {
     * 1                length of auth-response
     * string[n]        auth-response
     * } else {
     * string[NUL]      auth-response
     * }
     * if capabilities & CLIENT_CONNECT_WITH_DB {
     * string[NUL]      database
     * }
     * if capabilities & CLIENT_PLUGIN_AUTH {
     * string[NUL]      auth plugin name
     * }
     * if capabilities & CLIENT_CONNECT_ATTRS {
     * lenenc-int       length of all key-values
     * lenenc-str       key
     * lenenc-str       value
     * }
     * </pre>
     */
    @Override
    public byte[] getData() {
        try {
            outputStream.writeInt(CapabilityFlags.CLIENT_LONG_FLAG | CapabilityFlags.CLIENT_PROTOCOL_41 | CapabilityFlags.CLIENT_SECURE_CONNECTION | CapabilityFlags.CLIENT_CONNECT_WITH_DB);
            outputStream.writeInt(0);
            outputStream.writeInt(handShakePacket.getCharacterSet(), 1);

            outputStream.fillBytes(MySQLConstants.NULL_TERMINATED_STRING_DELIMITER, 23);

            outputStream.writeEndTerminatedString(username);
            byte[] encryptPassword = encryptPwd();
            outputStream.writeInt(encryptPassword.length, 1);
            outputStream.writeBytes(encryptPassword);
            outputStream.writeEndTerminatedString(schema);

//            outputStream.writeEndTerminatedString(handShakePacket.getAuthPluginName());

            return outputStream.toBytes();
        } catch (IOException | NoSuchAlgorithmException e) {
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
        return handShakePacket.getSequenceNumber();
    }

    private byte[] encryptPwd() throws NoSuchAlgorithmException {
        String scramble = handShakePacket.getAuthDataPart() + handShakePacket.getAuthDataPart2();
        return EncryptUtil.passwordCompatibleWithMySQL411(password, scramble);
    }

}
