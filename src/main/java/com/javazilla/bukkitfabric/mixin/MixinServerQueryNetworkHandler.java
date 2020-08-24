package com.javazilla.bukkitfabric.mixin;

import java.net.InetSocketAddress;
import java.util.Iterator;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftIconCache;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.mojang.authlib.GameProfile;

import net.minecraft.SharedConstants;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ServerQueryPacketListener;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.text.Text;

@Mixin(ServerQueryNetworkHandler.class)
public class MixinServerQueryNetworkHandler implements ServerQueryPacketListener {

    @Shadow @Final private static Text REQUEST_HANDLED;
    @Shadow @Final private ClientConnection connection;
    @Shadow private boolean responseSent;

    @Overwrite
    public void onRequest(QueryRequestC2SPacket packetstatusinstart) {
        if (this.responseSent) {
            this.connection.disconnect(REQUEST_HANDLED);
        } else {
            this.responseSent = true;
            // CraftBukkit start
            // this.networkManager.sendPacket(new PacketStatusOutServerInfo(this.minecraftServer.getServerPing()));
            MinecraftServer server = CraftServer.server;
            final Object[] players = server.getPlayerManager().players.toArray();
            class ServerListPingEvent extends org.bukkit.event.server.ServerListPingEvent {

                CraftIconCache icon = CraftServer.INSTANCE.getServerIcon();

                ServerListPingEvent() {
                    super(((InetSocketAddress) connection.getAddress()).getAddress(), server.getServerMotd(), server.getPlayerManager().getMaxPlayerCount());
                }

                @Override
                public void setServerIcon(org.bukkit.util.CachedServerIcon icon) {
                    if (!(icon instanceof CraftIconCache)) {
                        throw new IllegalArgumentException(icon + " was not created by " + org.bukkit.craftbukkit.CraftServer.class);
                    }
                    this.icon = (CraftIconCache) icon;
                }

                @Override
                public Iterator<Player> iterator() throws UnsupportedOperationException {
                    return new Iterator<Player>() {
                        int i;
                        int ret = Integer.MIN_VALUE;
                        ServerPlayerEntity player;

                        @Override
                        public boolean hasNext() {
                            if (player != null)
                                return true;

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
                            if (!hasNext())
                                throw new java.util.NoSuchElementException();
                            final ServerPlayerEntity player = this.player;
                            this.player = null;
                            this.ret = this.i - 1;
                            return (Player) ((IMixinServerEntityPlayer)player).getBukkitEntity();
                        }

                        @Override
                        public void remove() {
                            final Object[] currentPlayers = players;
                            final int i = this.ret;
                            if (i < 0 || currentPlayers[i] == null)
                                throw new IllegalStateException();
                            currentPlayers[i] = null;
                        }
                    };
                }
            }

            ServerListPingEvent event = new ServerListPingEvent();
            CraftServer.INSTANCE.getPluginManager().callEvent(event);

            java.util.List<GameProfile> profiles = new java.util.ArrayList<GameProfile>(players.length);
            for (Object player : players)
                if (player != null)
                    profiles.add(((ServerPlayerEntity) player).getGameProfile());

            ServerMetadata.Players playerSample = new ServerMetadata.Players(event.getMaxPlayers(), profiles.size());
            // Spigot Start
            if ( !profiles.isEmpty() ) {
                java.util.Collections.shuffle( profiles ); // This sucks, its inefficient but we have no simple way of doing it differently
                profiles = profiles.subList( 0, Math.min( profiles.size(), /*org.spigotmc.SpigotConfig.playerSample*/4 )); // Cap the sample to n (or less) displayed players, ie: Vanilla behaviour
            }
            // Spigot End
            playerSample.setSample(profiles.toArray(new GameProfile[profiles.size()]));

            ServerMetadata ping = new ServerMetadata();
            ping.setFavicon(event.icon.value);
            ping.setDescription(CraftChatMessage.fromString(event.getMotd(), true)[0]);
            ping.setPlayers(playerSample);
            int version = SharedConstants.getGameVersion().getProtocolVersion();
            ping.setVersion(new ServerMetadata.Version(server.getServerModName() + " " + server.getVersion(), version));

            this.connection.send(new QueryResponseS2CPacket(ping));
        }
        // CraftBukkit end
    }

    @Shadow public ClientConnection getConnection() {return null;}
    @Shadow
    public void onDisconnected(Text reason) {}
    @Shadow public void onPing(QueryPingC2SPacket packet) {}

}