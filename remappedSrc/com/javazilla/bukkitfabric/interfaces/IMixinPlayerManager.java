package com.javazilla.bukkitfabric.interfaces;

import org.bukkit.Location;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public interface IMixinPlayerManager {

    public ServerPlayerEntity moveToWorld(ServerPlayerEntity player, ServerWorld world, boolean flag, Location location, boolean avoidSuffocation);

}