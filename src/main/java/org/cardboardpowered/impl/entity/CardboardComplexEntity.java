package org.cardboardpowered.impl.entity;

import net.minecraft.entity.LivingEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.ComplexLivingEntity;

public abstract class CardboardComplexEntity extends LivingEntityImpl implements ComplexLivingEntity {

    public CardboardComplexEntity(CraftServer server, LivingEntity entity) {
        super(server, entity);
    }

    @Override
    public LivingEntity getHandle() {
        return (LivingEntity) nms;
    }

}