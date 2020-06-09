package com.fungus_soft.bukkitfabric.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.server.ServerNetworkIo;
import com.fungus_soft.bukkitfabric.interfaces.IMixinNetworkIo;

import io.netty.channel.ChannelFuture;

@Mixin(ServerNetworkIo.class)
public class ServerNetworkIoMixin implements IMixinNetworkIo {

    @Shadow
    @Final
    public List<ChannelFuture> channels;

    @Override
    public void acceptConnections() {
        synchronized (channels) {
            for (ChannelFuture future : channels)
                future.channel().config().setAutoRead(true);
        }
    }

}