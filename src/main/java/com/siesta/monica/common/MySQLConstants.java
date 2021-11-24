package com.siesta.monica.common;

public class MySQLConstants {

    private MySQLConstants() {}

    public static final int MAX_PACKET_LENGTH = (1 << 24);
    // String 字符串需要一个空字符作为结束符
    public static final byte NULL_TERMINATED_STRING_DELIMITER   = 0x00;

}
