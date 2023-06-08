package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.WitchEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Witch;
import org.bukkit.inventory.ItemStack;

public class CardboardWitch extends CardboardRaider implements Witch {

    public CardboardWitch(CraftServer server, WitchEntity entity) {
        super(server, entity);
    }

    @Override
    public WitchEntity getHandle() {
        return (WitchEntity) nms;
    }

    @Override
    public String toString() {
        return "Witch";
    }

    @Override
    public EntityType getType() {
        return EntityType.WITCH;
    }

    @Override
    public void rangedAttack(LivingEntity arg0, float arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setChargingAttack(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ItemStack getDrinkingPotion() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getPotionUseTimeLeft() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isDrinkingPotion() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setDrinkingPotion(ItemStack arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPotionUseTimeLeft(int arg0) {
        // TODO Auto-generated method stub
        
    }

	@Override
	public boolean isCelebrating() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCelebrating(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

}