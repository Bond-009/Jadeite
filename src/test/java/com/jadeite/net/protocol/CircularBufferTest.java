package com.jadeite.net.protocol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CircularBufferTest {
    @Test
    void writeBytes_large_wrapAround() {
        final CircularBuffer buf = new CircularBuffer(4);
        final byte[] b = new byte[] { 0, 1, 2, 3, 4};
        buf.writeBytes(b);
        Assertions.assertEquals(4, buf.readByte());
    }
}
