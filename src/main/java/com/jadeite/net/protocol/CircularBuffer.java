package com.jadeite.net.protocol;

public class CircularBuffer {

    /** Size of the buffer */
    private final int size;

    /** The buffer */
    private final byte[] buffer;

    /** Index of the next data to be read from the buffer */
    private int readIndex;

    /** Index of the next data written in the buffer */
    private int writeIndex;

    CircularBuffer(final int size) {
        this.size = size;
        buffer = new byte[size];
    }

    /**
     * Gets the amount of readable bytes.
     */
    public int available() {
        if (readIndex > writeIndex) {
            // Wrap around
            return size - readIndex + writeIndex;
        }

        return writeIndex - readIndex;
    }

    /**
     * Writes a byte to the buffer.
     */
    public void writeByte(final byte value) {
        buffer[writeIndex] = value;
        writeIndex = (writeIndex + 1) % size;
    }

    /**
     * Writes bytes to the buffer.
     */
    public void writeBytes(byte[] bytes) {
        writeBytes(bytes, 0, bytes.length);
    }

    /**
     * Writes bytes to the buffer.
     */
    public void writeBytes(byte[] bytes, int pos, int len) {
        int beforeEnd = size - writeIndex;
        if (len >= beforeEnd)
        {
            System.arraycopy(bytes, pos, buffer, writeIndex, beforeEnd);
            pos += beforeEnd;
            len -= beforeEnd;
            writeIndex = 0;
        }

        System.arraycopy(bytes, pos, buffer, writeIndex, len);
        writeIndex += len;
    }

    /**
     * Reads a byte from the buffer.
     */
    public byte readByte() {
        //if (available() > 0) {
            final byte value = buffer[readIndex];
            readIndex = (readIndex + 1) % size;
            return value;
        //}

        //return -1;
    }

    /**
     * Reads a bytes from the buffer.
     */
    public void readBytes(byte[] dest) {
        readBytes(dest, 0, dest.length);
    }

    /**
     * Reads a bytes from the buffer.
     */
    public void readBytes(byte[] dest, int pos, int len) {
        int beforeEnd = size - readIndex;
        if (len >= beforeEnd)
        {
            System.arraycopy(buffer, readIndex, dest, pos, beforeEnd);
            pos += beforeEnd;
            len -= beforeEnd;
            readIndex = 0;
        }

        System.arraycopy(buffer, readIndex, dest, pos, len);
        readIndex += len;
    }

    public int readVarInt() {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }
}
