package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

public class TntImpl extends CraftEntity implements TNTPrimed {

    public TntImpl(CraftServer server, TntEntity entity) {
        super(entity);
    }

    @Override
    public float getYield() {
        return 0f; // TODO return getHandle().yield;
    }

    @Override
    public boolean isIncendiary() {
        return false; // TODO return getHandle().isIncendiary;
    }

    @Override
    public void setIsIncendiary(boolean isIncendiary) {
        // TODO getHandle().isIncendiary = isIncendiary;
    }

    @Override
    public void setYield(float yield) {
        // TODO getHandle().yield = yield;
    }

    @Override
    public int getFuseTicks() {
        return getHandle().getFuseTimer();
    }

    @Override
    public void setFuseTicks(int fuseTicks) {
        getHandle().setFuse(fuseTicks);
    }

    @Override
    public TntEntity getHandle() {
        return (TntEntity) nms;
    }

    @Override
    public String toString() {
        return "TNT";
    }

    @Override
    public EntityType getType() {
        return EntityType.PRIMED_TNT;
    }

    @Override
    public Entity getSource() {
        LivingEntity source = getHandle().getCausingEntity();
        return (source != null) ? ((IMixinEntity)source).getBukkitEntity() : null;
    }

}
