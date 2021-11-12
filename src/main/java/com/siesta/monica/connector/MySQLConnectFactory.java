package com.siesta.monica.connector;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MySQLConnectFactory {
    private static final Map<SocketAddress, MySQLConnector> MYSQL_CONNECTOR_MAP = new ConcurrentHashMap<>();

    private MySQLConnectFactory() {}

    public static MySQLConnector createMySQLConnector(String host, int port, String username, String password, String database) {
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        return MYSQL_CONNECTOR_MAP.computeIfAbsent(socketAddress, address -> new MySQLConnector(address, username, password, database));
    }

    public static MySQLConnector getDefaultMySQLConnector(String host, int port) {
        return MYSQL_CONNECTOR_MAP.get(new InetSocketAddress(host, port));
    }

}
