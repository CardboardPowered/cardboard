package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.SalmonEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Salmon;

public class CardboardFishSalmon extends CardboardFish implements Salmon {

    public CardboardFishSalmon(CraftServer server, SalmonEntity entity) {
        super(server, entity);
    }

    @Override
    public SalmonEntity getHandle() {
        return (SalmonEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "Salmon";
    }

    @Override
    public EntityType getType() {
        return EntityType.SALMON;
    }

}