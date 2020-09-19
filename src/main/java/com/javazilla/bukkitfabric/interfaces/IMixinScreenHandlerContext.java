package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMixinScreenHandlerContext {

    public org.bukkit.Location getLocation();

    public World getWorld();

    public BlockPos getPosition();

}