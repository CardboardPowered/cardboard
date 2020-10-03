package org.bukkit.craftbukkit.entity;

import net.minecraft.entity.mob.PathAwareEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Creature;

public class CraftCreature extends CraftMob implements Creature {

    public CraftCreature(CraftServer server, PathAwareEntity entity) {
        super(server, entity);
    }

    @Override
    public PathAwareEntity getHandle() {
        return (PathAwareEntity) nms;
    }

    @Override
    public String toString() {
        return "CraftCreature";
    }

}