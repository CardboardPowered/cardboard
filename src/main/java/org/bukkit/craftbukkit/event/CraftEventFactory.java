package org.bukkit.craftbukkit.event;

import java.net.InetAddress;

import org.bukkit.Server;
import org.bukkit.event.server.ServerListPingEvent;

public class CraftEventFactory {

    public static ServerListPingEvent callServerListPingEvent(Server craftServer, InetAddress address, String motd, int numPlayers, int maxPlayers) {
        ServerListPingEvent event = new ServerListPingEvent(address, motd, numPlayers, maxPlayers);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

}