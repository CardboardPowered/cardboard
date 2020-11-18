package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.BlazeEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;

public class CardboardBlaze extends MonsterImpl implements Blaze {

    public CardboardBlaze(CraftServer server, BlazeEntity entity) {
        super(server, entity);
    }

    @Override
    public BlazeEntity getHandle() {
        return (BlazeEntity) nms;
    }

    @Override
    public String toString() {
        return "Blaze";
    }

    @Override
    public EntityType getType() {
        return EntityType.BLAZE;
    }

}