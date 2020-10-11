package com.javazilla.bukkitfabric.impl.entity;

import org.bukkit.entity.EntityType;

import net.minecraft.entity.mob.DrownedEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Drowned;

public class DrownedImpl extends ZombieImpl implements Drowned {

    public DrownedImpl(CraftServer server, DrownedEntity entity) {
        super(server, entity);
    }

    @Override
    public DrownedEntity getHandle() {
        return (DrownedEntity) nms;
    }

    @Override
    public String toString() {
        return "Drowned";
    }

    @Override
    public EntityType getType() {
        return EntityType.DROWNED;
    }

}