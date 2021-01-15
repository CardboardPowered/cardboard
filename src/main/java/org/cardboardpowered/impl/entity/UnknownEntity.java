package org.cardboardpowered.impl.entity;

import org.bukkit.Chunk;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import net.minecraft.entity.Entity;

public class UnknownEntity extends CraftEntity {

    public UnknownEntity(Entity entity) {
        super(entity);
    }

    @Override
    public EntityType getType() {
        return EntityType.UNKNOWN;
    }

    @Override
    public boolean fromMobSpawner() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Chunk getChunk() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpawnReason getEntitySpawnReason() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isInBubbleColumn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInWaterOrBubbleColumn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInWaterOrRain() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInWaterOrRainOrBubbleColumn() {
        // TODO Auto-generated method stub
        return false;
    }

}
