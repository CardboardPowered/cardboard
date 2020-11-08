package org.cardboardpowered.impl.entity;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.EntityType;

import net.minecraft.entity.Entity;

public class UnknownEntity extends CraftEntity {

    public UnknownEntity(Entity entity) {
        super(entity);
    }

    @Override
    public EntityType getType() {
        return EntityType.UNKNOWN;
    }

}
