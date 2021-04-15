package com.javazilla.bukkitfabric.mixin.network;

import java.net.InetAddress;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinNetworkIo;

import io.netty.channel.ChannelFuture;
import net.minecraft.server.ServerNetworkIo;

@Mixin(ServerNetworkIo.class)
public class MixinServerNetworkIo implements IMixinNetworkIo {

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

    @Inject(at = @At("TAIL"), method = "bind")
    public void cardboard_setAutoreadFalse(InetAddress ina, int i, CallbackInfo ci) {
        synchronized (channels) {
            for (ChannelFuture future : channels)
                future.channel().config().setAutoRead(false);
        }
    }

}