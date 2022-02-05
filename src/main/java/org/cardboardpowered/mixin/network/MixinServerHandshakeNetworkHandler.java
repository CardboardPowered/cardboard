package org.cardboardpowered.mixin.network;

import org.cardboardpowered.interfaces.IHandshakeC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinClientConnection;
import com.javazilla.bukkitfabric.interfaces.IMixinServerLoginNetworkHandler;

import me.isaiah.common.GameVersion;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import net.minecraft.server.network.ServerLoginNetworkHandler;

@Mixin(ServerHandshakeNetworkHandler.class)
public class MixinServerHandshakeNetworkHandler {

    private static final com.google.gson.Gson gson = new com.google.gson.Gson(); // Spigot

    @Shadow
    public ClientConnection connection;

    @Inject(at = @At("TAIL"), method = "onHandshake")
    public void onHandshake_Bungee(HandshakeC2SPacket packethandshakinginsetprotocol, CallbackInfo ci) {
        if (packethandshakinginsetprotocol.getIntendedState() == NetworkState.LOGIN) {
            GameVersion ver = GameVersion.INSTANCE;

            if (packethandshakinginsetprotocol.getProtocolVersion() > ver.getProtocolVersion()) {
            } else if (packethandshakinginsetprotocol.getProtocolVersion() < ver.getProtocolVersion()) {
            } else {
                if (org.spigotmc.SpigotConfig.bungee) {
                    String[] split = packethandshakinginsetprotocol.address.split("\00");
                    if ( split.length == 3 || split.length == 4 ) {
                       // TODO 1.17ify packethandshakinginsetprotocol.address = split[0];
                        connection.address = new java.net.InetSocketAddress(split[1], ((java.net.InetSocketAddress) connection.getAddress()).getPort());
                        ((IMixinClientConnection)connection).setSpoofedUUID(com.mojang.util.UUIDTypeAdapter.fromString( split[2] ));
                    } else {
                        return;
                    }
                    if ( split.length == 4 ) ((IMixinClientConnection)connection).setSpoofedProfile(gson.fromJson(split[3], com.mojang.authlib.properties.Property[].class));
                }
                ((IMixinServerLoginNetworkHandler)((ServerLoginNetworkHandler) this.connection.getPacketListener())).setHostname(packethandshakinginsetprotocol.address + ":" + ((IHandshakeC2SPacket)packethandshakinginsetprotocol).getPortBF()); // Bukkit - set hostname
            }
        }
    }

}