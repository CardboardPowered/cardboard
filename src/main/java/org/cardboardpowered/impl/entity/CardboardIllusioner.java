package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.IllusionerEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.LivingEntity;

public class CardboardIllusioner extends CardboardSpellcaster implements Illusioner {

    public CardboardIllusioner(CraftServer server, IllusionerEntity entity) {
        super(server, entity);
    }

    @Override
    public IllusionerEntity getHandle() {
        return (IllusionerEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "Illusioner";
    }

    @Override
    public EntityType getType() {
        return EntityType.ILLUSIONER;
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