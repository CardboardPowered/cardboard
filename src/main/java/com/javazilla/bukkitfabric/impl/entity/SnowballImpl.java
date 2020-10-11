package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.projectile.thrown.SnowballEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowball;

public class SnowballImpl extends ThrowableProjectileImpl implements Snowball {

    public SnowballImpl(CraftServer server, SnowballEntity entity) {
        super(server, entity);
    }

    @Override
    public SnowballEntity getHandle() {
        return (SnowballEntity) nms;
    }

    @Override
    public String toString() {
        return "SnowballImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.SNOWBALL;
    }

}