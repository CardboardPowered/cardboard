package org.cardboardpowered.impl.entity;

import net.kyori.adventure.sound.Sound.Source;
import net.minecraft.entity.passive.SnowGolemEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowman;
import org.jetbrains.annotations.NotNull;

public class CardboardSnowman extends CardboardGolem implements Snowman {

    public CardboardSnowman(CraftServer server, SnowGolemEntity entity) {
        super(server, entity);
    }

    @Override
    public boolean isDerp() {
        return !getHandle().hasPumpkin();
    }

    @Override
    public void setDerp(boolean derpMode) {
        getHandle().setHasPumpkin(!derpMode);
    }

    @Override
    public SnowGolemEntity getHandle() {
        return (SnowGolemEntity) nms;
    }

    @Override
    public String toString() {
        return "Snowman";
    }

    @Override
    public EntityType getType() {
        return EntityType.SNOWMAN;
    }

    @Override
    public void rangedAttack(LivingEntity arg0, float arg1) {
    }

    @Override
    public void setChargingAttack(boolean arg0) {
    }
    
    // 1.19.4:

	@Override
	public boolean readyToBeSheared() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void shear(@NotNull Source arg0) {
		// TODO Auto-generated method stub
		
	}

}