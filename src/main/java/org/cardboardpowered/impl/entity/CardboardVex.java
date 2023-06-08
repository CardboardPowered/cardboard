package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.VexEntity;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Vex;
import org.jetbrains.annotations.Nullable;

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

	@Override
	public @Nullable Location getBound() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLifeTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getLimitedLifetimeTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasLimitedLife() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasLimitedLifetime() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setBound(@Nullable Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLifeTicks(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLimitedLifetime(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLimitedLifetimeTicks(int arg0) {
		// TODO Auto-generated method stub
		
	}

}