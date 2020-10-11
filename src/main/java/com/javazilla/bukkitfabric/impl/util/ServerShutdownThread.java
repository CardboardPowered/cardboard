package com.javazilla.bukkitfabric.impl.util;

public class ServerShutdownThread extends Thread {

    @Override
    public void run() {
        try {
            System.out.println("Server Closed! ");
        } finally {
        }
    }

}