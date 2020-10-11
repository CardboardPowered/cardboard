package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.passive.CowEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;

public class CowImpl extends AnimalsImpl implements Cow {

    public CowImpl(CraftServer server, CowEntity entity) {
        super(server, entity);
    }

    @Override
    public CowEntity getHandle() {
        return (CowEntity) nms;
    }

    @Override
    public String toString() {
        return "CraftCow";
    }

    @Override
    public EntityType getType() {
        return EntityType.COW;
    }

}