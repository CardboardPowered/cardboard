package org.cardboardpowered.impl.entity;

import net.kyori.adventure.text.Component;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowball;
import org.jetbrains.annotations.Nullable;

public class SnowballImpl extends ThrowableProjectileImpl implements Snowball {

    public SnowballImpl(CraftServer server, SnowballEntity entity) {
        super(server, entity);
    }

    @Override
    public SnowballEntity getHandle() {
        return (SnowballEntity) nms;
    }

    @Override
    public String toString() {
        return "SnowballImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.SNOWBALL;
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