package com.javazilla.bukkitfabric.mixin.network;

import java.net.InetSocketAddress;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.impl.IconCacheImpl;

import net.minecraft.SharedConstants;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ServerQueryPacketListener;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.text.Text;

@Mixin(ServerQueryNetworkHandler.class)
public class MixinServerQueryNetworkHandler implements ServerQueryPacketListener {

    @Shadow @Final private static Text REQUEST_HANDLED;
    @Shadow @Final private ClientConnection connection;
    @Shadow private boolean responseSent;

    /**
     * @author BukkitFabric
     * @reason ServerListPingEvent
     */
    @Overwrite
    public void onRequest(QueryRequestC2SPacket packetstatusinstart) {
        if (this.responseSent) {
            this.connection.disconnect(REQUEST_HANDLED);
        } else {
            this.responseSent = true;
            MinecraftServer server = CraftServer.server;

            class ServerListPingEvent extends org.bukkit.event.server.ServerListPingEvent {

                ServerListPingEvent() {
                    super(((InetSocketAddress) connection.getAddress()).getAddress(), server.getServerMotd(), server.getPlayerManager().getCurrentPlayerCount(), server.getPlayerManager().getMaxPlayerCount());
                }

                @Override
                public void setServerIcon(org.bukkit.util.CachedServerIcon icon) {
                    if (!(icon instanceof IconCacheImpl))
                        throw new IllegalArgumentException(icon + " was not created by Bukkit");
                }

            }

            ServerListPingEvent event = new ServerListPingEvent ();
            CraftServer.INSTANCE.getPluginManager().callEvent(event);

            ServerMetadata ping = new ServerMetadata();
            ping.setDescription(CraftChatMessage.fromString(event.getMotd(), true)[0]);

            try {
                ping.setFavicon(server.getServerMetadata().favicon);
            } catch (Exception e) {}
            ping.setPlayers(server.getServerMetadata().players);
            int version = SharedConstants.getGameVersion().getProtocolVersion();
            ping.setVersion(new ServerMetadata.Version("BukkitModded " + server.getVersion(), version));

            this.connection.send(new QueryResponseS2CPacket(ping));
        }
    }

    @Shadow public ClientConnection getConnection() {return null;}
    @Shadow
    public void onDisconnected(Text reason) {}
    @Shadow public void onPing(QueryPingC2SPacket packet) {}

}