package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.mob.StrayEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Stray;

public class StrayImpl extends SkeletonImpl implements Stray {

    public StrayImpl(CraftServer server, StrayEntity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "StrayImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.STRAY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public SkeletonType getSkeletonType() {
        return SkeletonType.STRAY;
    }

}