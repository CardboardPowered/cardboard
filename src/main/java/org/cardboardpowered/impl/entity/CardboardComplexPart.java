package org.cardboardpowered.impl.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.kyori.adventure.text.Component;
import net.minecraft.entity.boss.dragon.EnderDragonPart;

public class CardboardComplexPart extends CraftEntity implements ComplexEntityPart {

    public CardboardComplexPart(CraftServer server, EnderDragonPart entity) {
        super(entity);
    }

    @Override
    public ComplexLivingEntity getParent() {
        return (ComplexLivingEntity) ((IMixinEntity) getHandle().owner).getBukkitEntity();
    }

    @Override
    public void setLastDamageCause(EntityDamageEvent cause) {
        getParent().setLastDamageCause(cause);
    }

    @Override
    public EntityDamageEvent getLastDamageCause() {
        return getParent().getLastDamageCause();
    }

    @Override
    public boolean isValid() {
        return getParent().isValid();
    }

    @Override
    public EnderDragonPart getHandle() {
        return (EnderDragonPart) nms;
    }

    @Override
    public EntityType getType() {
        return EntityType.UNKNOWN;
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

}