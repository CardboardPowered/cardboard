package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.AmbientEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.EntityType;

public class CardboardAmbient extends MobImpl implements Ambient {

    public CardboardAmbient(CraftServer server, AmbientEntity entity) {
        super(server, entity);
    }

    @Override
    public AmbientEntity getHandle() {
        return (AmbientEntity) nms;
    }

    @Override
    public String toString() {
        return "Ambient";
    }

    @Override
    public EntityType getType() {
        return EntityType.UNKNOWN;
    }

}