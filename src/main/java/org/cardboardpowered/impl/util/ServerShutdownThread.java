package org.cardboardpowered.impl.util;

import org.bukkit.craftbukkit.CraftServer;

public class ServerShutdownThread extends Thread {

    @Override
    public void run() {
        System.out.println("Server Closed! ");
    }

}