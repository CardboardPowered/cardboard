package org.cardboardpowered.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Iterator;

import org.bukkit.Server;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.cardboardpowered.impl.util.IconCacheImpl;

import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class CardboardServerListPingEvent extends ServerListPingEvent {
    
    public final Object[] players;
    public IconCacheImpl icon;
    
    //      public ServerListPingEvent(@NotNull InetAddress address, @NotNull String motd, boolean shouldSendChatPreviews, int numPlayers, int maxPlayers) {

    public CardboardServerListPingEvent(ClientConnection connection, MinecraftServer server) {
        super(((InetSocketAddress) connection.getAddress()).getAddress(), server.getServerMotd(), false, server.getPlayerManager().getCurrentPlayerCount(), server.getPlayerManager().getMaxPlayerCount());
        this.players = server.getPlayerManager().players.toArray();
        this.icon = CraftServer.INSTANCE.getServerIcon();
    }

    @Override
    public void setServerIcon(org.bukkit.util.CachedServerIcon icon) {
        if (!(icon instanceof IconCacheImpl)) throw new IllegalArgumentException(icon + " was not created by Bukkit");
    }

    public Iterator<Player> iterator() throws UnsupportedOperationException {
        return new Iterator<Player>() {
            int i;
            int ret = Integer.MIN_VALUE;
            ServerPlayerEntity player;

            @Override
            public boolean hasNext() {
                if (player != null) {
                    return true;
                }
                final Object[] currentPlayers = players;
                for (int length = currentPlayers.length, i = this.i; i < length; i++) {
                    final ServerPlayerEntity player = (ServerPlayerEntity) currentPlayers[i];
                    if (player != null) {
                        this.i = i + 1;
                        this.player = player;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Player next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                final ServerPlayerEntity player = this.player;
                this.player = null;
                this.ret = this.i - 1;
                return (Player) ((IMixinServerEntityPlayer)player).getBukkitEntity();
            }

            @Override
            public void remove() {
                final Object[] currentPlayers = players;
                final int i = this.ret;
                if (i < 0 || currentPlayers[i] == null) {
                    throw new IllegalStateException();
                }
                currentPlayers[i] = null;
            }
        };
    }

}