package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.WitchEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Witch;

public class CardboardWitch extends CardboardRaider implements Witch {

    public CardboardWitch(CraftServer server, WitchEntity entity) {
        super(server, entity);
    }

    @Override
    public WitchEntity getHandle() {
        return (WitchEntity) nms;
    }

    @Override
    public String toString() {
        return "Witch";
    }

    @Override
    public EntityType getType() {
        return EntityType.WITCH;
    }

}