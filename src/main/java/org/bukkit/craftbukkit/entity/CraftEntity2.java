package org.bukkit.craftbukkit.entity;

import org.bukkit.entity.EntityType;

import net.minecraft.entity.Entity;

public class CraftEntity2 extends CraftEntity {

    public CraftEntity2(Entity entity) {
        super(entity);
    }

    @Override
    public EntityType getType() {
        return EntityType.UNKNOWN;
    }

}
