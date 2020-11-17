package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.CodEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Cod;
import org.bukkit.entity.EntityType;

public class CardboardFishCod extends CardboardFish implements Cod {

    public CardboardFishCod(CraftServer server, CodEntity entity) {
        super(server, entity);
    }

    @Override
    public CodEntity getHandle() {
        return (CodEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "Cod";
    }

    @Override
    public EntityType getType() {
        return EntityType.COD;
    }

}