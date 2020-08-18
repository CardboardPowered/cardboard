package com.javazilla.bukkitfabric.interfaces;

import org.bukkit.Location;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;

public interface IMixinPlayerManager {

    public ServerPlayerEntity moveToWorld(ServerPlayerEntity entityplayer, DimensionType dimensionmanager, boolean flag, Location location, boolean avoidSuffocation);

}