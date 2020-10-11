package com.javazilla.bukkitfabric.impl.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;

import net.minecraft.entity.passive.ChickenEntity;

public class ChickenImpl extends AnimalsImpl implements Chicken {

    public ChickenImpl(CraftServer server, ChickenEntity entity) {
        super(server, entity);
    }

    @Override
    public ChickenEntity getHandle() {
        return (ChickenEntity) nms;
    }

    @Override
    public String toString() {
        return "Chicken";
    }

    @Override
    public EntityType getType() {
        return EntityType.CHICKEN;
    }

}