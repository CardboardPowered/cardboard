package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.GiantEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;

public class GiantImpl extends MonsterImpl implements Giant {

    public GiantImpl(CraftServer server, GiantEntity entity) {
        super(server, entity);
    }

    @Override
    public GiantEntity getHandle() {
        return (GiantEntity) nms;
    }

    @Override
    public String toString() {
        return "Giant";
    }

    @Override
    public EntityType getType() {
        return EntityType.GIANT;
    }

}