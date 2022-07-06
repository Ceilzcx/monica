package com.siesta.monica.connector;

import com.siesta.monica.common.ConnectStatus;
import com.siesta.monica.protocol.command.AuthCommand;
import com.siesta.monica.protocol.command.AuthSwitchCommand;
import com.siesta.monica.protocol.packet.AuthPacket;
import com.siesta.monica.protocol.packet.HandShakePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.io.IOException;
import java.net.SocketAddress;
import java.security.NoSuchAlgorithmException;

public class MySQLConnector {
    private Channel channel;
    private final SocketAddress socketAddress;
    private final String username;
    private final String password;
    private final String database;
    private ConnectStatus status;
    private HandShakePacket packet;

    public MySQLConnector(SocketAddress socketAddress, String username, String password, String database) {
        this.socketAddress = socketAddress;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public void connect() throws InterruptedException {
        status = ConnectStatus.HAND_SHAKE;
        channel = NettyServer.open(socketAddress);
    }

    public ConnectStatus getStatus() {
        return status;
    }

    public void setStatus(ConnectStatus status) {
        this.status = status;
    }

    public void readProtocol(ByteBuf msg) throws IOException {
        this.packet = new HandShakePacket(msg);
        this.packet.logInfo();

        AuthCommand authCommand = new AuthCommand(channel, packet, username, password, database);
        status = ConnectStatus.AUTH_SWITCH;
        authCommand.send();
    }


    public void readAuthSwitchRequest(ByteBuf msg) throws NoSuchAlgorithmException {
        AuthPacket authPacket = new AuthPacket(msg);
        authPacket.logInfo();
        if (authPacket.getStatus() == AuthPacket.SWITCH_STATUS) {
            sendAuthSwitchResponse(authPacket.getAuthPluginData());
        }
    }

    public void sendAuthSwitchResponse(String authData) throws NoSuchAlgorithmException {
        AuthSwitchCommand response = new AuthSwitchCommand(channel, authData, password, packet.getSequenceNumber());
        channel.writeAndFlush(response.getAuthData());
    }

}