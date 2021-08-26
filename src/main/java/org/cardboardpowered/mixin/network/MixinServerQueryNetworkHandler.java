package org.cardboardpowered.mixin.network;

import java.util.ArrayList;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.cardboardpowered.impl.CardboardServerListPingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

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
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Mixin(ServerQueryNetworkHandler.class)
public class MixinServerQueryNetworkHandler implements ServerQueryPacketListener {

    @Shadow private static Text REQUEST_HANDLED;
    @Shadow private ClientConnection connection;
    @Shadow private boolean responseSent;

    /**
     * @author Cardboard
     * @reason ServerListPingEvent
     */
    @Overwrite
    public void onRequest(QueryRequestC2SPacket packetstatusinstart) {
        if (this.responseSent) {
            this.connection.disconnect(new LiteralText("BF: Response sent!"));
        } else {
            this.responseSent = true;
            MinecraftServer server = CraftServer.server;

            CardboardServerListPingEvent event = new CardboardServerListPingEvent(connection, server);
            CraftServer.INSTANCE.getPluginManager().callEvent(event);

            ArrayList<GameProfile> profiles = new ArrayList<GameProfile>(event.players.length);
            for (Object player : event.players) {
                if (player != null)
                    profiles.add(((ServerPlayerEntity) player).getGameProfile());
            }

            ServerMetadata.Players samp = new ServerMetadata.Players(event.getMaxPlayers(), profiles.size());
            samp.setSample(profiles.toArray(new GameProfile[profiles.size()]));

            ServerMetadata ping = new ServerMetadata();
            ping.setDescription(CraftChatMessage.fromString(event.getMotd(), true)[0]);

            ping.setFavicon(event.icon.value);
            ping.setPlayers(samp);
            ping.setVersion(new ServerMetadata.Version("Cardboard " + server.getVersion(), SharedConstants.getGameVersion().getProtocolVersion()));

            this.connection.send(new QueryResponseS2CPacket(ping));
        }
    }

    @Shadow public ClientConnection getConnection() {return null;}
    @Shadow public void onDisconnected(Text reason) {}
    @Shadow public void onPing(QueryPingC2SPacket packet) {}

}