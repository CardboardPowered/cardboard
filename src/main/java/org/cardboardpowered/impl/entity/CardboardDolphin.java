package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.DolphinEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.EntityType;

public class CardboardDolphin extends CardboardWaterMob implements Dolphin {

    public CardboardDolphin(CraftServer server, DolphinEntity entity) {
        super(server, entity);
    }

    @Override
    public DolphinEntity getHandle() {
        return (DolphinEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "Dolphin";
    }

    @Override
    public EntityType getType() {
        return EntityType.DOLPHIN;
    }

}