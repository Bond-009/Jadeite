package com.jadeite.net.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Protocol {
    private final int MAX_ENC_SIZE = 512;

    private final Socket socket;
    private final byte[] processDataBuffer = new byte[MAX_ENC_SIZE];
    private final CircularBuffer readBuffer = new CircularBuffer(32 * 1024);

    private int state = State.HandShaking;
    private boolean encrypted = false;
    private boolean compressed = false;

    public Protocol(Socket sock) {
        socket = sock;
    }

    public void processData() throws IOException {
        InputStream inputStream = socket.getInputStream();
        int available = inputStream.available();
        if (available <= 0) {
            // No data to read
            return;
        }

        while (available > 0) {
            int read = inputStream.read(processDataBuffer, 0, Math.min(available, MAX_ENC_SIZE));
            available -= read;
            // TODO: decrypt

            readBuffer.writeBytes(processDataBuffer, 0, read);
        }

        handlePackets();
    }

    private void handlePackets() {
        for (;;) {
            if (readBuffer.available() <= 0) {
                // No data to read
                return;
            }

            int len = readBuffer.readVarInt();
            assert readBuffer.available() < len;

            // TODO: decompress

            // TODO: maybe remove allocation
            byte[] packet = new byte[len];
            readBuffer.readBytes(packet);
            handlePacket(ByteBuffer.wrap(packet));
        }
    }

    private void handlePacket(ByteBuffer packet) {
        int id = ByteBufferHelper.readVarInt(packet);
        switch (state) {
            case State.HandShaking:
                switch (id) {
                    case 0x00:
                        handleHandshake(packet);
                        return;
                    default:
                        System.out.println("unknown packet with id: " + id);
                        return;
                }
            case State.Disconnected:
                return;
            default:
                System.out.println("unknown state: " + state);
                return;
        }
    }

    // HandShaking packets

    private void handleHandshake(ByteBuffer rbuf) {
        int version = ByteBufferHelper.readVarInt(rbuf);
        assert version == 47;
        String _address = ByteBufferHelper.readString(rbuf);
        short _port = rbuf.getShort();
        int nextState = ByteBufferHelper.readVarInt(rbuf);
        // TODO: validate state?
        state = nextState;
        System.out.println(state);
    }
}
