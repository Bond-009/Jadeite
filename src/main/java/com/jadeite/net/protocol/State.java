package com.jadeite.net.protocol;

public class State {
    public static final int HandShaking = 0x00;
    public static final int Status = 0x01;
    public static final int Login = 0x02;
    public static final int Play = 0x03;
    public static final int Disconnected = 0xFF;
}
