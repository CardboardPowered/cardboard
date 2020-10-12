package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.mob.SlimeEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;

import com.javazilla.bukkitfabric.interfaces.IMixinSlimeEntity;

public class SlimeImpl extends MobImpl implements Slime {

    public SlimeImpl(CraftServer server, SlimeEntity entity) {
        super(server, entity);
    }

    @Override
    public int getSize() {
        return getHandle().getSize();
    }

    @Override
    public void setSize(int size) {
        ((IMixinSlimeEntity)getHandle()).setSizeBF(size, true);
    }

    @Override
    public SlimeEntity getHandle() {
        return (SlimeEntity) nms;
    }

    @Override
    public String toString() {
        return "SlimeImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.SLIME;
    }

}