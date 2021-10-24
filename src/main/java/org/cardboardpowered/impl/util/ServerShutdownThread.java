package org.cardboardpowered.impl.util;

public class ServerShutdownThread extends Thread {

    @Override
    public void run() {
        System.out.println("Server Closed! ");
    }

}