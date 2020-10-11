package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.mob.MagmaCubeEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;

public class MagmaCubeImpl extends SlimeImpl implements MagmaCube {

    public MagmaCubeImpl(CraftServer server, MagmaCubeEntity entity) {
        super(server, entity);
    }

    @Override
    public MagmaCubeEntity getHandle() {
        return (MagmaCubeEntity) nms;
    }

    @Override
    public String toString() {
        return "MagmaCubeImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.MAGMA_CUBE;
    }

}