package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.SalmonEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Salmon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.papermc.paper.entity.SchoolableFish;

public class CardboardFishSalmon extends CardboardFish implements Salmon {

    public CardboardFishSalmon(CraftServer server, SalmonEntity entity) {
        super(server, entity);
    }

    @Override
    public SalmonEntity getHandle() {
        return (SalmonEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "Salmon";
    }

    @Override
    public EntityType getType() {
        return EntityType.SALMON;
    }
    
    // TODO: import io.papermc.paper.entity.PaperSchoolableFish;

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