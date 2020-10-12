package com.javazilla.bukkitfabric.impl.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Monster;

import net.minecraft.entity.mob.HostileEntity;

public class MonsterImpl extends CreatureImpl implements Monster {

    public MonsterImpl(CraftServer server, HostileEntity entity) {
        super(server, entity);
    }

    @Override
    public HostileEntity getHandle() {
        return (HostileEntity) nms;
    }

    @Override
    public String toString() {
        return "CraftMonster";
    }

}