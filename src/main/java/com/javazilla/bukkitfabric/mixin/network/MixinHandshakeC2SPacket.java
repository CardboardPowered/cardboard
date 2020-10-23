package com.javazilla.bukkitfabric.mixin.network;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinHandshakeC2SPacket;

import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;

@Mixin(HandshakeC2SPacket.class)
public class MixinHandshakeC2SPacket implements IMixinHandshakeC2SPacket {

    @Shadow public int protocolVersion;
    @Shadow public String address;
    @Shadow public int port;
    @Shadow public NetworkState intendedState;

    @Override
    public int getPortBF() {
        return port;
    }

    /**
     * @author BukkitFabric
     * @reason 256 -> Short.MAX_VALUE
     */
    @Overwrite
    public void read(PacketByteBuf buf) throws IOException {
        this.protocolVersion = buf.readVarInt();
        this.address = buf.readString(Short.MAX_VALUE);
        this.port = buf.readUnsignedShort();
        this.intendedState = NetworkState.byId(buf.readVarInt());
    }


}