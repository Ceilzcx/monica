package com.siesta.monica.protocol;

import com.siesta.monica.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class AuthSwitchRequest {
    private static final Logger log = LoggerFactory.getLogger(AuthSwitchRequest.class);
    private String pluginName;
    // caching_sha2_password | mysql_native_password
    private String authPluginData;

    public void readBytes(byte[] bytes) {
        int index = 0;
        byte status = bytes[0];
        if (status != 0xfe) {
            log.warn("auth switch request status error, status: {}", status);
            return;
        }
        index++;
        byte[] pluginBytes = ByteUtil.getBytes(bytes, index, (byte) 0x00);
        pluginName = new String(pluginBytes, StandardCharsets.UTF_8);
        index += pluginBytes.length;
        authPluginData = new String(ByteUtil.splitBytes(bytes, index, bytes.length - index), StandardCharsets.UTF_8);
    }

    public void logInfo() {
        log.info("plugin name: {}", pluginName);
        log.info("auth plugin data: {}", authPluginData);
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getAuthPluginData() {
        return authPluginData;
    }
}
