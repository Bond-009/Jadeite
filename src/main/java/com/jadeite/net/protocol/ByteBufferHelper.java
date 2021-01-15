package com.jadeite.net.protocol;

import java.nio.ByteBuffer;

public class ByteBufferHelper {
    public static int readVarInt(ByteBuffer buf) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = buf.get();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public static String readString(ByteBuffer buf) {
        int len = readVarInt(buf);

        if (len == 0) {
            return "";
        }

        byte[] bytes = new byte[len];
        buf.get(bytes);
        return new String(bytes);
    }
}
