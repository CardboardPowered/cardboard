package org.cardboardpowered.impl.entity;

import net.minecraft.entity.passive.FishEntity;

import org.bukkit.Sound;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Fish;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CardboardFish extends CardboardWaterMob implements Fish {

    public CardboardFish(CraftServer server, FishEntity entity) {
        super(server, entity);
    }

    @Override
    public FishEntity getHandle() {
        return (FishEntity) nms;
    }

    @Override
    public String toString() {
        return "CardboardFish";
    }

	@Override
	public @NotNull ItemStack getBaseBucketItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull Sound getPickupSound() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFromBucket() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFromBucket(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

}