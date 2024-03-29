package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.BatEntity;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public class CardboardBat extends CardboardAmbient implements Bat {

    public CardboardBat(CraftServer server, BatEntity entity) {
        super(server, entity);
    }

    @Override
    public BatEntity getHandle() {
        return (BatEntity) nms;
    }

    @Override
    public String toString() {
        return "Batman";
    }

    @Override
    public EntityType getType() {
        return EntityType.BAT;
    }

    @Override
    public boolean isAwake() {
        return !getHandle().isRoosting();
    }

    @Override
    public void setAwake(boolean state) {
        getHandle().setRoosting(!state);
    }

	@Override
	public @Nullable Location getTargetLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTargetLocation(@Nullable Location arg0) {
		// TODO Auto-generated method stub
		
	}

}