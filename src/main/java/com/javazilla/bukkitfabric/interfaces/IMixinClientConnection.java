package com.javazilla.bukkitfabric.interfaces;

import java.net.SocketAddress;
import java.util.UUID;

import com.mojang.authlib.properties.Property;

public interface IMixinClientConnection {

    SocketAddress getRawAddress();

    UUID getSpoofedUUID();

    void setSpoofedUUID(UUID uuid);

    Property[] getSpoofedProfile();

    void setSpoofedProfile(Property[] profile);

}