package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.SilverfishEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Silverfish;

public class CardboardSilverfish extends MonsterImpl implements Silverfish {

    public CardboardSilverfish(CraftServer server, SilverfishEntity entity) {
        super(server, entity);
    }

    @Override
    public SilverfishEntity getHandle() {
        return (SilverfishEntity) nms;
    }

    @Override
    public String toString() {
        return "CardboardSilverfish";
    }

    @Override
    public EntityType getType() {
        return EntityType.SILVERFISH;
    }

}