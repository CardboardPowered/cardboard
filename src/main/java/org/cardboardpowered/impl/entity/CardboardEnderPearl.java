package org.cardboardpowered.impl.entity;

import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;

public class CardboardEnderPearl extends ThrowableProjectileImpl implements EnderPearl {

    public CardboardEnderPearl(CraftServer server, EnderPearlEntity entity) {
        super(server, entity);
    }

    @Override
    public EnderPearlEntity getHandle() {
        return (EnderPearlEntity) nms;
    }

    @Override
    public String toString() {
        return "Enderpearl";
    }

    @Override
    public EntityType getType() {
        return EntityType.ENDER_PEARL;
    }

}