package com.javazilla.bukkitfabric.mixin.network;

import java.net.SocketAddress;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinClientConnection;

import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;

@Mixin(ClientConnection.class)
public class MixinClientConnection implements IMixinClientConnection {

    @Shadow
    public Channel channel;

    @Override
    public SocketAddress getRawAddress() {
        return channel.remoteAddress();
    }

}
