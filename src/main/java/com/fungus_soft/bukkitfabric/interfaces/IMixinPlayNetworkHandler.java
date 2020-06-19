package com.fungus_soft.bukkitfabric.interfaces;

import org.bukkit.Location;

public interface IMixinPlayNetworkHandler {

    public void chat(String message, boolean notDeprecated);

    public void teleport(Location location);

    public boolean isDisconnected();

}