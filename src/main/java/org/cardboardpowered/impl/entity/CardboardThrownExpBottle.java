package org.cardboardpowered.impl.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ThrownExpBottle;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;

public class CardboardThrownExpBottle extends ThrowableProjectileImpl implements ThrownExpBottle {

    public CardboardThrownExpBottle(CraftServer server, ProjectileEntity entity) {
        super(server, entity);
    }

    @Override
    public ExperienceBottleEntity getHandle() {
        return (ExperienceBottleEntity) nms;
    }

    @Override
    public String toString() {
        return "EntityThrownExpBottle";
    }

    @Override
    public EntityType getType() {
        return EntityType.THROWN_EXP_BOTTLE;
    }

}