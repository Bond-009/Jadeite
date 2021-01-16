package com.jadeite.net.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

        do {
            int read = inputStream.read(processDataBuffer, 0, Math.min(available, MAX_ENC_SIZE));
            available -= read;
            // TODO: decrypt

            readBuffer.writeBytes(processDataBuffer, 0, read);
        } while (available > 0);

        handlePackets();
    }

    private void handlePackets() throws IOException {
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

    private void handlePacket(ByteBuffer packet) throws IOException {
        int id = ByteBufferHelper.readVarInt(packet);
        switch (state) {
            case State.HandShaking:
                switch (id) {
                    case 0x00:
                        handleHandshake(packet);
                        return;
                    default:
                        System.out.println("unknown packet: " + id  + ", state: " + state);
                        return;
                }
            case State.Status:
                switch (id) {
                    case 0x00:
                        handleRequest(packet);
                        return;
                    case 0x01:
                        handlePing(packet);
                        return;
                    default:
                        System.out.println("unknown packet: " + id  + ", state: " + state);
                        return;
                }
            case State.Disconnected:
                return;
            default:
                System.out.println("unknown state: " + state);
                return;
        }
    }

    private void writePacket(ByteBuffer packet) throws IOException {
        int len = packet.position();
        // TODO: encryption + compression
        OutputStream stream = socket.getOutputStream();
        OutputStreamHelper.writeVarInt(stream, len);
        stream.write(packet.array(), packet.arrayOffset(), len);
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

    // Status packets

    private void handleRequest(ByteBuffer rbuf) throws IOException {
        JSONObject obj = new JSONObject();

        JSONObject version = new JSONObject();
        version.put("name", "1.8.9");
        version.put("protocol", new Integer(47));
        obj.put("version", version);

        JSONObject players = new JSONObject();
        // TODO: make configurable
        players.put("max", new Integer(2147483647));
        players.put("online", new Integer(42069));
        JSONArray sample = new JSONArray();
        players.put("sample", sample);
        obj.put("players", players);

        JSONObject description = new JSONObject();
        description.put("text", "bite my shiny metal ass");
        obj.put("description", description);

        String str = obj.toJSONString();

        System.out.println(str);

        ByteBuffer wbuf = ByteBuffer.allocate(str.length() + 5);
        ByteBufferHelper.writeVarInt(wbuf, 0x00);
        ByteBufferHelper.writeString(wbuf, str);

        writePacket(wbuf);
    }

    private void handlePing(ByteBuffer rbuf) throws IOException {
        ByteBuffer wbuf = ByteBuffer.allocate(16);
        ByteBufferHelper.writeVarInt(wbuf, 0x01);
        long payload = rbuf.getLong();
        wbuf.putLong(payload);

        System.out.println(payload);

        writePacket(rbuf);
    }
}
