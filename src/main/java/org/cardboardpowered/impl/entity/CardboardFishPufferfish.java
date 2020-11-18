package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.PufferfishEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PufferFish;

public class CardboardFishPufferfish extends CardboardFish implements PufferFish {

    public CardboardFishPufferfish(CraftServer server, PufferfishEntity entity) {
        super(server, entity);
    }

    @Override
    public PufferfishEntity getHandle() {
        return (PufferfishEntity) super.getHandle();
    }

    @Override
    public int getPuffState() {
        return getHandle().getPuffState();
    }

    @Override
    public void setPuffState(int state) {
        getHandle().setPuffState(state);
    }

    @Override
    public String toString() {
        return "PufferFish";
    }

    @Override
    public EntityType getType() {
        return EntityType.PUFFERFISH;
    }

}