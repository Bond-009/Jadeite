package com.jadeite;

import com.jadeite.net.protocol.Protocol;
import com.jadeite.net.protocol.ProtocolThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.SynchronousQueue;

public class Server {
    public void start() throws IOException, InterruptedException {
        final ServerSocket socket = new ServerSocket(25565);
        final SynchronousQueue<Protocol> queue = new SynchronousQueue<>();
        ProtocolThread protocolThread = new ProtocolThread(queue);
        protocolThread.start();
        for (;;) {
            Socket con = socket.accept();
            con.setTcpNoDelay(true);
            Protocol protocol = new Protocol(con);
            queue.put(protocol);
        }
    }
}
