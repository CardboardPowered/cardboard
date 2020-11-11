package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.WaterCreatureEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.WaterMob;

public class CardboardWaterMob extends CreatureImpl implements WaterMob {

    public CardboardWaterMob(CraftServer server, WaterCreatureEntity entity) {
        super(server, entity);
    }

    @Override
    public WaterCreatureEntity getHandle() {
        return (WaterCreatureEntity) nms;
    }

    @Override
    public String toString() {
        return "WaterMob";
    }

}