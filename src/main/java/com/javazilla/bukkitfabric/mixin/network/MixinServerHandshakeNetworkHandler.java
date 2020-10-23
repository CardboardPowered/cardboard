package com.javazilla.bukkitfabric.mixin.network;

import java.net.InetAddress;
import java.util.HashMap;

import org.bukkit.craftbukkit.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinClientConnection;
import com.javazilla.bukkitfabric.interfaces.IMixinHandshakeC2SPacket;
import com.javazilla.bukkitfabric.interfaces.IMixinServerLoginNetworkHandler;

import net.minecraft.SharedConstants;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Mixin(ServerHandshakeNetworkHandler.class)
public class MixinServerHandshakeNetworkHandler {

    // CraftBukkit start - add fields
    private static final com.google.gson.Gson gson = new com.google.gson.Gson(); // Spigot
    //private static final HashMap<InetAddress, Long> throttleTracker = new HashMap<InetAddress, Long>();
    //private static int throttleCounter = 0;
    // CraftBukkit end

    @Shadow
    public ClientConnection connection;

    @Shadow
    public ClientConnection getConnection() {
        return null;
    }

    @Shadow
    public void onDisconnected(Text reason) {
    }

    @Overwrite
    public void onHandshake(HandshakeC2SPacket packethandshakinginsetprotocol) {
        switch (packethandshakinginsetprotocol.getIntendedState()) {
            case LOGIN:
                this.connection.setState(NetworkState.LOGIN);
                TranslatableText chatmessage;

                if (packethandshakinginsetprotocol.getProtocolVersion() > SharedConstants.getGameVersion().getProtocolVersion()) {
                    chatmessage = new TranslatableText( java.text.MessageFormat.format( org.spigotmc.SpigotConfig.outdatedServerMessage.replaceAll("'", "''"), SharedConstants.getGameVersion().getName() ) ); // Spigot
                    this.connection.send(new LoginDisconnectS2CPacket(chatmessage));
                    this.connection.disconnect(chatmessage);
                } else if (packethandshakinginsetprotocol.getProtocolVersion() < SharedConstants.getGameVersion().getProtocolVersion()) {
                    chatmessage = new TranslatableText( java.text.MessageFormat.format( org.spigotmc.SpigotConfig.outdatedClientMessage.replaceAll("'", "''"), SharedConstants.getGameVersion().getName() ) ); // Spigot
                    this.connection.send(new LoginDisconnectS2CPacket(chatmessage));
                    this.connection.disconnect(chatmessage);
                } else {
                    this.connection.setPacketListener(new ServerLoginNetworkHandler(CraftServer.server, this.connection));
                    // Spigot Start
                    if (org.spigotmc.SpigotConfig.bungee) {
                        String[] split = packethandshakinginsetprotocol.address.split("\00");
                        if ( split.length == 3 || split.length == 4 ) {
                            packethandshakinginsetprotocol.address = split[0];
                            connection.address = new java.net.InetSocketAddress(split[1], ((java.net.InetSocketAddress) connection.getAddress()).getPort());
                            ((IMixinClientConnection)connection).setSpoofedUUID(com.mojang.util.UUIDTypeAdapter.fromString( split[2] ));
                        } else {
                            chatmessage = new TranslatableText("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!");
                            this.connection.send(new LoginDisconnectS2CPacket(chatmessage));
                            this.connection.disconnect(chatmessage);
                            return;
                        }
                        if ( split.length == 4 ) ((IMixinClientConnection)connection).setSpoofedProfile(gson.fromJson(split[3], com.mojang.authlib.properties.Property[].class));
                    }
                    // Spigot End
                    ((IMixinServerLoginNetworkHandler)((ServerLoginNetworkHandler) this.connection.getPacketListener())).setHostname(packethandshakinginsetprotocol.address + ":" + ((IMixinHandshakeC2SPacket)packethandshakinginsetprotocol).getPortBF()); // Bukkit - set hostname
                }
                break;
            case STATUS:
                if (CraftServer.server.acceptsStatusQuery()) {
                    this.connection.setState(NetworkState.STATUS);
                    this.connection.setPacketListener(new ServerQueryNetworkHandler(CraftServer.INSTANCE.getHandle(), this.connection));
                } else this.connection.disconnect(new LiteralText("Ignoring status request"));
                break;
            default:
                throw new UnsupportedOperationException("Invalid intention " + packethandshakinginsetprotocol.getIntendedState());
        }

    }

}