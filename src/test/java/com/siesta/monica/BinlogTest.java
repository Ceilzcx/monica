package com.siesta.monica;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import org.junit.Test;

import java.io.IOException;

public class BinlogTest {

    @Test
    public void test() throws IOException {
        BinaryLogClient client = new BinaryLogClient("localhost", 3306, "test", "username", "");
        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setCompatibilityMode(
                EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
        );
        client.setEventDeserializer(eventDeserializer);
        client.registerEventListener(event -> {
        });
        client.connect();
    }

}
