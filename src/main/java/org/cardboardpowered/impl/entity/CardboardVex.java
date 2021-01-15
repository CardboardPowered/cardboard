package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.VexEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Vex;

public class CardboardVex extends MonsterImpl implements Vex {

    public CardboardVex(CraftServer server, VexEntity entity) {
        super(server, entity);
    }

    @Override
    public VexEntity getHandle() {
        return (VexEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "Vex";
    }

    @Override
    public EntityType getType() {
        return EntityType.VEX;
    }

    @Override
    public boolean isCharging() {
        return getHandle().isCharging();
    }

    @Override
    public void setCharging(boolean charging) {
        getHandle().setCharging(charging);
    }

    @Override
    public Mob getSummoner() {
        return null;
    }

    @Override
    public void setSummoner(Mob arg0) {
    }

}