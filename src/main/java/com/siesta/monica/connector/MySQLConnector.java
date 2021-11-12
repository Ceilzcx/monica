package com.siesta.monica.connector;

import com.siesta.monica.CapabilityFlags;
import com.siesta.monica.ConnectStatus;
import com.siesta.monica.MySQLConstants;
import com.siesta.monica.protocol.HandShake;
import com.siesta.monica.util.EncryptUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class MySQLConnector {
    private static final Logger log = LoggerFactory.getLogger(MySQLConnector.class);

    private Channel channel;
    private final SocketAddress socketAddress;
    private final String username;
    private final String password;
    private final String database;
    private ConnectStatus status;

    public MySQLConnector(SocketAddress socketAddress, String username, String password, String database) {
        this.socketAddress = socketAddress;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public void connect() throws InterruptedException {
        status = ConnectStatus.CONNECT;
        channel = NettyServer.open(socketAddress);
    }

    public ConnectStatus getStatus() {
        return status;
    }

    public void setStatus(ConnectStatus status) {
        this.status = status;
    }

    public static void main(String[] args) throws InterruptedException {
        MySQLConnector sqlConnector = MySQLConnectFactory.createMySQLConnector("localhost", 3306, "root", "zcx88372565", "test");
        sqlConnector.connect();
    }

    public void readProtocol(ByteBuf msg) throws IOException, NoSuchAlgorithmException {
        byte[] header = new byte[4];
        msg.readBytes(header);

        int len = (header[0] & 0xFF) | ((header[1] & 0xFF) << 8) | ((header[2] & 0xFF) << 16);
        byte sequenceNumber = header[3];
        log.info("len = {}, sequenceNumber = {}", len, sequenceNumber);

        byte[] body = new byte[len];
        msg.readBytes(body);

        HandShake handShake = new HandShake();
        handShake.setPacketSequenceNumber(sequenceNumber);
        handShake.readBytes(body);
        handShake.logInfo();
        sendAuthPacket(handShake);
    }

    /**
     * <pre>
     *      4              capability flags, CLIENT_PROTOCOL_41 always set
     *      4              max-packet size
     *      1              character set
     *      string[23]     reserved (all [0])
     *      string[NUL]    username
     *   if capabilities & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA {
     *      lenenc-int     length of auth-response
     *      string[n]      auth-response
     *   } else if capabilities & CLIENT_SECURE_CONNECTION {
     *      1              length of auth-response
     *      string[n]      auth-response
     *   } else {
     *      string[NUL]    auth-response
     *   }
     *   if capabilities & CLIENT_CONNECT_WITH_DB {
     *      string[NUL]    database
     *   }
     *   if capabilities & CLIENT_PLUGIN_AUTH {
     *      string[NUL]    auth plugin name
     *   }
     *   if capabilities & CLIENT_CONNECT_ATTRS {
     *      lenenc-int     length of all key-values
     *      lenenc-str     key
     *      lenenc-str     value
     *   }
     * </pre>
     */
    public void sendAuthPacket(HandShake handShake) throws IOException, NoSuchAlgorithmException {
        setStatus(ConnectStatus.AUTH_PACKAGE);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(CapabilityFlags.CLIENT_PROTOCOL_41);
        bos.write(MySQLConstants.MAX_PACKET_LENGTH);
        bos.write(handShake.getCharacterSet());
        // reserved (all [0]) length = 23
        bos.write(new byte[23]);
        // username string[NUL]
        bos.write(username.getBytes(StandardCharsets.UTF_8));
        // password should encode with SHA1
        byte[] scrambleBuff = new byte[handShake.getAuthDataPart().length() + handShake.getAuthDataPart2().length()];
        System.arraycopy(handShake.getAuthDataPart().getBytes(StandardCharsets.UTF_8),
                0,
                scrambleBuff,
                0,
                handShake.getAuthDataPart().length());
        System.arraycopy(handShake.getAuthDataPart2().getBytes(StandardCharsets.UTF_8),
                0,
                scrambleBuff,
                handShake.getAuthDataPart().length(),
                handShake.getAuthDataPart2().length());
        byte[] encryptedPassword = EncryptUtil.encode411(password.getBytes(StandardCharsets.UTF_8), scrambleBuff);
        bos.write(encryptedPassword);
        // database string[NUL]
        bos.write(database.getBytes(StandardCharsets.UTF_8));
        bos.write(handShake.getAuthPluginName().getBytes(StandardCharsets.UTF_8));

        byte[] body = bos.toByteArray();
        byte[] data = new byte[4 + body.length];
        data[0] = (byte)(body.length & 0xFF);
        data[1] = (byte) (body.length >>> 8);
        data[2] = (byte) (body.length >>> 16);
//        data[3] = handShake.getPacketSequenceNumber();
        data[3] = (byte) 33;
        System.arraycopy(body, 0, data, 4, body.length);

        channel.writeAndFlush(data);
    }

}