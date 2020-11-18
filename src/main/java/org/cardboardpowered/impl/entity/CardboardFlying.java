package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.FlyingEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Flying;

public class CardboardFlying extends MobImpl implements Flying {

    public CardboardFlying(CraftServer server, FlyingEntity entity) {
        super(server, entity);
    }

    @Override
    public FlyingEntity getHandle() {
        return (FlyingEntity) nms;
    }

    @Override
    public String toString() {
        return "FlyingEntity";
    }

}