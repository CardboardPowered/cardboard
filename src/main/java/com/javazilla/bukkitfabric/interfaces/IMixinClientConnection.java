package com.javazilla.bukkitfabric.interfaces;

import java.net.SocketAddress;
import java.util.UUID;

import com.mojang.authlib.properties.Property;

public interface IMixinClientConnection {

    public SocketAddress getRawAddress();

    public UUID getSpoofedUUID();

    public void setSpoofedUUID(UUID uuid);

    public Property[] getSpoofedProfile();

    public void setSpoofedProfile(Property[] profile);

}