package com.jadeite;

import com.jadeite.net.protocol.Protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public void start() throws IOException {
        final ServerSocket socket = new ServerSocket(25565);
        for (;;) {
            Socket con = socket.accept();
            con.setTcpNoDelay(true);
            Protocol protocol = new Protocol(con);
            for (;;) {
                protocol.processData();
            }
        }
    }
}
