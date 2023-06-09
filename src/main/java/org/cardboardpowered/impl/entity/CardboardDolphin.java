package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.DolphinEntity;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

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

	@Override
	public int getMoistness() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public @NotNull Location getTreasureLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasFish() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHasFish(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMoistness(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTreasureLocation(@NotNull Location arg0) {
		// TODO Auto-generated method stub
		
	}

}