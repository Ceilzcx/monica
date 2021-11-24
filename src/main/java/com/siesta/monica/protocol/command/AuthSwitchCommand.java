package com.siesta.monica.protocol.command;

import com.siesta.monica.protocol.MySQLCommand;
import com.siesta.monica.util.EncryptUtil;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class AuthSwitchCommand extends MySQLCommand {
    public final byte[] authData;
    private final byte sequenceNumber;

    public AuthSwitchCommand(Channel channel, String authData, String password, byte sequenceNumber) throws NoSuchAlgorithmException {
        super(channel);
        this.authData = EncryptUtil.encode411(password.getBytes(StandardCharsets.UTF_8), authData.getBytes(StandardCharsets.UTF_8));
        this.sequenceNumber = sequenceNumber;
    }

    public byte[] getAuthData() {
        return authData;
    }

    @Override
    public byte[] getData() {
        return authData;
    }

    @Override
    public byte getSequenceNumber() {
        return sequenceNumber;
    }
}
