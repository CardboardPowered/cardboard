package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.mob.AbstractSkeletonEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftMonster;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;

@SuppressWarnings("deprecation")
public class SkeletonImpl extends CraftMonster implements Skeleton {

    public SkeletonImpl(CraftServer server, AbstractSkeletonEntity entity) {
        super(server, entity);
    }

    @Override
    public AbstractSkeletonEntity getHandle() {
        return (AbstractSkeletonEntity) nms;
    }

    @Override
    public String toString() {
        return "SkeletonImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.SKELETON;
    }

    @Override
    public SkeletonType getSkeletonType() {
       return SkeletonType.NORMAL;
    }

    @Override
    public void setSkeletonType(SkeletonType type) {
        throw new UnsupportedOperationException("Not supported.");
    }

}