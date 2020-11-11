package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.GhastEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;

public class CardboardGhast extends CardboardFlyingEntity implements Ghast {

    public CardboardGhast(CraftServer server, GhastEntity entity) {
        super(server, entity);
    }

    @Override
    public GhastEntity getHandle() {
        return (GhastEntity) nms;
    }

    @Override
    public String toString() {
        return "Ghast";
    }

    @Override
    public EntityType getType() {
        return EntityType.GHAST;
    }

}