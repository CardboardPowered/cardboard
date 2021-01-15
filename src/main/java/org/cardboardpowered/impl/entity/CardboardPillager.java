package org.cardboardpowered.impl.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pillager;
import org.bukkit.inventory.Inventory;

import net.minecraft.entity.mob.PillagerEntity;

public class CardboardPillager extends CardboardIllager implements Pillager {

    public CardboardPillager(CraftServer server, PillagerEntity entity) {
        super(server, entity);
    }

    @Override
    public PillagerEntity getHandle() {
        return (PillagerEntity) super.getHandle();
    }

    @Override
    public EntityType getType() {
        return EntityType.PILLAGER;
    }

    @Override
    public String toString() {
        return "Pillager";
    }

    @Override
    public Inventory getInventory() {
        return null; // TODO  inventory is not visible.
    }

    @Override
    public void rangedAttack(LivingEntity arg0, float arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setChargingAttack(boolean arg0) {
        // TODO Auto-generated method stub
    }

}