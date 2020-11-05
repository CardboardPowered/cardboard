package com.javazilla.bukkitfabric.mixin.network;

import java.net.SocketAddress;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinClientConnection;
import com.mojang.authlib.properties.Property;

import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;

@Mixin(ClientConnection.class)
public class MixinClientConnection implements IMixinClientConnection {

    public UUID spoofedUUID;
    public Property[] spoofedProfile;
    public boolean preparing = true;

    @Override
    public SocketAddress getRawAddress() {
        return ((ClientConnection)(Object)this).channel.remoteAddress();
    }

    @Override
    public UUID getSpoofedUUID() {
        return spoofedUUID;
    }

    @Override
    public void setSpoofedUUID(UUID uuid) {
        this.spoofedUUID = uuid;
    }

    @Override
    public Property[] getSpoofedProfile() {
        return spoofedProfile;
    }

    @Override
    public void setSpoofedProfile(Property[] profile) {
        this.spoofedProfile = profile;
    }

}
