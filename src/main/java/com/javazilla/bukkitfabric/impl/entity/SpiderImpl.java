package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.mob.SpiderEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Spider;

public class SpiderImpl extends MonsterImpl implements Spider {

    public SpiderImpl(CraftServer server, SpiderEntity entity) {
        super(server, entity);
    }

    @Override
    public SpiderEntity getHandle() {
        return (SpiderEntity) nms;
    }

    @Override
    public String toString() {
        return "SpiderImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.SPIDER;
    }

}