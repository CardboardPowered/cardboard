package org.cardboardpowered.impl.entity;

import net.kyori.adventure.text.Component;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CardboardFishHook extends ProjectileImpl implements FishHook {

    private double biteChance = -1;

    public CardboardFishHook(CraftServer server, FishingBobberEntity entity) {
        super(server, entity);
    }

    @Override
    public FishingBobberEntity getHandle() {
        return (FishingBobberEntity) nms;
    }

    @Override
    public String toString() {
        return "CardboardFishingHook";
    }

    @Override
    public EntityType getType() {
        return EntityType.FISHING_HOOK;
    }

    @Override
    public double getBiteChance() {
        FishingBobberEntity hook = getHandle();
        if (this.biteChance == -1) {
            if (hook.world.hasRain(new BlockPos(MathHelper.floor(hook.getX()), MathHelper.floor(hook.getY()) + 1, MathHelper.floor(hook.getZ()))))
                return 1 / 300.0;
            return 1 / 500.0;
        }
        return this.biteChance;
    }

    @Override
    public void setBiteChance(double chance) {
        Validate.isTrue(chance >= 0 && chance <= 1, "The bite chance must be between 0 and 1.");
        this.biteChance = chance;
    }

    @Override
    public boolean getApplyLure() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getMaxWaitTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getMinWaitTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setApplyLure(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMaxWaitTime(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMinWaitTime(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public @Nullable Component customName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void customName(@Nullable Component arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public @Nullable Entity getHookedEntity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NotNull HookState getState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isInOpenWater() {
        // TODO Auto-generated method stub
        return nms.isTouchingWater();
    }

    @Override
    public boolean pullHookedEntity() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setHookedEntity(@Nullable Entity arg0) {
        // TODO Auto-generated method stub
        
    }

	@Override
	public int getWaitTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setWaitTime(int arg0) {
		// TODO Auto-generated method stub
		
	}

}
