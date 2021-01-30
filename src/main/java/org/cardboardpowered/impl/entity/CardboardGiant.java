package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.GiantEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;

public class CardboardGiant extends MonsterImpl implements Giant {

    public CardboardGiant(CraftServer server, GiantEntity entity) {
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