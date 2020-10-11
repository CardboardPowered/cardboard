package com.javazilla.bukkitfabric.impl.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WitherSkeleton;
import net.minecraft.entity.mob.WitherSkeletonEntity;

public class WitherSkeletonImpl extends SkeletonImpl implements WitherSkeleton {

    public WitherSkeletonImpl(CraftServer server, WitherSkeletonEntity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "WitherSkeletonImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.WITHER_SKELETON;
    }

    @SuppressWarnings("deprecation")
    @Override
    public SkeletonType getSkeletonType() {
        return SkeletonType.WITHER;
    }

}