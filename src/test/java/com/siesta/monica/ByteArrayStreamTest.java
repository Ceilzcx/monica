package com.siesta.monica;

import com.siesta.monica.io.ByteArrayInputStream;
import com.siesta.monica.io.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ByteArrayStreamTest {

    @Test
    public void test() throws IOException {
        int a = 10;
        long b = 20L;
        String msg1 = "hello";
        String msg2 = "world";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.writeInt(a);
        outputStream.writeLong(b);
        outputStream.writeEndTerminatedString(msg1);
        outputStream.writeEndTerminatedString(msg2);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toBytes());
        Assert.assertEquals(inputStream.readInt(4), a);
        Assert.assertEquals(inputStream.readLong(8), b);
        Assert.assertEquals(inputStream.readEndTerminatedString(), msg1);
        Assert.assertEquals(inputStream.readString(msg2.getBytes(StandardCharsets.UTF_8).length), msg2);
    }

}
