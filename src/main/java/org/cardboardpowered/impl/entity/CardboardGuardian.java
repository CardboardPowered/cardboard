package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.GuardianEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;

public class CardboardGuardian extends MonsterImpl implements Guardian {

    public CardboardGuardian(CraftServer server, GuardianEntity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "Guardian";
    }

    @Override
    public EntityType getType() {
        return EntityType.GUARDIAN;
    }

    @Override
    public boolean isElder() {
        return false;
    }

    @Override
    public void setElder(boolean shouldBeElder) {
        throw new UnsupportedOperationException("Not supported.");
    }

}