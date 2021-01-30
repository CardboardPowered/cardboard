package org.cardboardpowered.impl.entity;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import net.minecraft.entity.mob.DrownedEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Drowned;

public class CardboardDrowned extends ZombieImpl implements Drowned {

    public CardboardDrowned(CraftServer server, DrownedEntity entity) {
        super(server, entity);
    }

    @Override
    public DrownedEntity getHandle() {
        return (DrownedEntity) nms;
    }

    @Override
    public String toString() {
        return "Drowned";
    }

    @Override
    public EntityType getType() {
        return EntityType.DROWNED;
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