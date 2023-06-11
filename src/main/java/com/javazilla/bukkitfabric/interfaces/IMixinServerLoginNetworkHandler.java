package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.network.ClientConnection;

public interface IMixinServerLoginNetworkHandler {

    String getHostname();

    void setHostname(String hostname);

	ClientConnection cb_get_connection();

}