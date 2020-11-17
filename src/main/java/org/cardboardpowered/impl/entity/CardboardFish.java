package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.FishEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Fish;

public class CardboardFish extends CardboardWaterMob implements Fish {

    public CardboardFish(CraftServer server, FishEntity entity) {
        super(server, entity);
    }

    @Override
    public FishEntity getHandle() {
        return (FishEntity) nms;
    }

    @Override
    public String toString() {
        return "CardboardFish";
    }

}