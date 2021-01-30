package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.HuskEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Husk;

public class CardboardHusk extends ZombieImpl implements Husk {

    public CardboardHusk(CraftServer server, HuskEntity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftHusk";
    }

    @Override
    public EntityType getType() {
        return EntityType.HUSK;
    }

}