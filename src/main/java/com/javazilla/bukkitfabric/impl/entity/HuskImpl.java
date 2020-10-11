package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.mob.HuskEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Husk;

public class HuskImpl extends ZombieImpl implements Husk {

    public HuskImpl(CraftServer server, HuskEntity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftHusk";
    }

    @Override
    public EntityType getType() {
        return EntityType.HUSK;
    }

}