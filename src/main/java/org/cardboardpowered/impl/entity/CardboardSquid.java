package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.SquidEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Squid;

public class CardboardSquid extends CardboardWaterMob implements Squid {

    public CardboardSquid(CraftServer server, SquidEntity entity) {
        super(server, entity);
    }

    @Override
    public SquidEntity getHandle() {
        return (SquidEntity) nms;
    }

    @Override
    public String toString() {
        return "Squid";
    }

    @Override
    public EntityType getType() {
        return EntityType.SQUID;
    }

}