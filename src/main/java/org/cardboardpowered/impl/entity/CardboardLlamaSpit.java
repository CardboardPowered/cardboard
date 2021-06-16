package org.cardboardpowered.impl.entity;

import net.kyori.adventure.text.Component;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.Nullable;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

public class CardboardLlamaSpit extends AbstractProjectile implements LlamaSpit {

    public CardboardLlamaSpit(CraftServer server, LlamaSpitEntity entity) {
        super(server, entity);
    }

    @Override
    public LlamaSpitEntity getHandle() {
        return (LlamaSpitEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "LlamaSpit";
    }

    @Override
    public EntityType getType() {
        return EntityType.LLAMA_SPIT;
    }

    @Override
    public ProjectileSource getShooter() {
        return (getHandle().getOwner() != null) ? (ProjectileSource) ((IMixinEntity)getHandle().getOwner()).getBukkitEntity() : null;
    }

    @Override
    public void setShooter(ProjectileSource source) {
        getHandle().setOwner((source != null) ? ((LivingEntityImpl) source).getHandle() : null);
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