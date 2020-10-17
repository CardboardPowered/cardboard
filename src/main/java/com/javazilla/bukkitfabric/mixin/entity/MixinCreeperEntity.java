package com.javazilla.bukkitfabric.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinCreeperEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

@Mixin(CreeperEntity.class)
public abstract class MixinCreeperEntity extends Entity implements IMixinCreeperEntity {

    public MixinCreeperEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public static TrackedData<Boolean> CHARGED;

    @Shadow
    public int explosionRadius = 3;

    @Shadow
    public int fuseTime = 30;

    @Inject(at = @At("HEAD"), method="onStruckByLightning", cancellable = true)
    public void invokeCreeperPowerEvent(ServerWorld worldserver, LightningEntity lightning, CallbackInfo ci) {
        super.onStruckByLightning(worldserver, lightning);
        if (BukkitEventFactory.callCreeperPowerEvent((CreeperEntity)(Object)this, lightning, org.bukkit.event.entity.CreeperPowerEvent.PowerCause.LIGHTNING).isCancelled()) {
            ci.cancel();
            return;
        }
        this.setPowered(true);
        ci.cancel();
        return;
    }

    @Override
    public void setPowered(boolean powered) {
        this.dataTracker.set(CHARGED, powered);
    }

    @Override
    public void explodeBF() {
        explode();
    }

    @Shadow
    public void explode() {
    }

    @Override
    public int getExplosionRadiusBF() {
        return explosionRadius;
    }

    @Override
    public void setExplosionRadiusBF(int radius) {
        this.explosionRadius = radius;
    }

    @Override
    public void setFuseTimeBF(int ticks) {
        this.fuseTime = ticks;
    }

    @Override
    public int getFuseTimeBF() {
        return this.fuseTime;
    }

    @Override
    public boolean isPoweredBF() {
        return (Boolean) this.dataTracker.get(CHARGED);
    }


}