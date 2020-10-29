package com.javazilla.bukkitfabric.mixin.entity;

import org.bukkit.projectiles.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

@Mixin(ProjectileEntity.class)
public class MixinProjectileEntity extends MixinEntity {

    @Inject(at = @At("TAIL"), method = "setOwner")
    public void setProjectileSource(Entity entity, CallbackInfo ci) {
        this.projectileSource = (entity != null && ((IMixinEntity)entity).getBukkitEntity() instanceof ProjectileSource) ? (ProjectileSource) ((IMixinEntity)entity).getBukkitEntity() : null;
    }

    @Inject(at = @At("HEAD"), method = "onCollision")
    public void fireProjectileHitEvent(HitResult hitResult, CallbackInfo ci) {
        BukkitEventFactory.callProjectileHitEvent((ProjectileEntity)(Object)this, hitResult);
    }

    @Shadow
    public void onBlockHit(BlockHitResult blockHitResult) {
    }

}