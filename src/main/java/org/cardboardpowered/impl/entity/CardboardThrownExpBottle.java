package org.cardboardpowered.impl.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ThrownExpBottle;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;
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

    @Override
    public @Nullable Component customName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void customName(@Nullable Component arg0) {
        // TODO Auto-generated method stub
        
    }

}