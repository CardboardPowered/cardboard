package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.CodEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Cod;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.papermc.paper.entity.SchoolableFish;

public class CardboardFishCod extends CardboardFish implements Cod {

    public CardboardFishCod(CraftServer server, CodEntity entity) {
        super(server, entity);
    }

    @Override
    public CodEntity getHandle() {
        return (CodEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "Cod";
    }

    @Override
    public EntityType getType() {
        return EntityType.COD;
    }

	@Override
	public int getMaxSchoolSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public @Nullable SchoolableFish getSchoolLeader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSchoolSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void startFollowing(@NotNull SchoolableFish arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopFollowing() {
		// TODO Auto-generated method stub
		
	}

}