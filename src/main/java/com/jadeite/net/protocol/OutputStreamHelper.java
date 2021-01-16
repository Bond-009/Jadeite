package com.jadeite.net.protocol;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamHelper {
    public static void writeVarInt(OutputStream stream, int value) throws IOException {
        do {
            byte temp = (byte)(value & 0b01111111);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }

            stream.write((int)temp);
        } while (value != 0);
    }
}
