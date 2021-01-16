package com.jadeite.net.protocol;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.concurrent.SynchronousQueue;

public class ProtocolThread extends Thread {
    SynchronousQueue<Protocol> protocolQueue;
    ArrayList<Protocol> protocols = new ArrayList<Protocol>();

    public ProtocolThread(SynchronousQueue<Protocol> queue) {
        protocolQueue = queue;
    }

    public void run() {
        for (;;) {
            protocolQueue.drainTo(protocols);

            for (int i = 0; i < protocols.size(); i++) {
                try {
                    protocols.get(i).processData();
                }
                catch (IOException ex) {
                    protocols.remove(i--);
                }
                catch (BufferUnderflowException ex) {
                    protocols.remove(i--);
                }
            }

            try {
                Thread.sleep(1000 / 20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
