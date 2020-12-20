package com.javazilla.bukkitfabric.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

@Mixin(ExperienceBottleEntity.class)
public class MixinExperienceBottleEntity extends MixinProjectileEntity {

    @Inject(at = @At("HEAD"), method = "onCollision", cancellable = true)
    public void doBukkitEvent(HitResult movingobjectposition, CallbackInfo ci) {
        HitResult.Type type = movingobjectposition.getType();
        if (type == HitResult.Type.ENTITY) {
            ((ExperienceBottleEntity)(Object)this).onEntityHit((EntityHitResult)movingobjectposition);
        } else if (type == HitResult.Type.BLOCK) this.onBlockHit((BlockHitResult)movingobjectposition);

        int i = 3 + this.world.random.nextInt(5) + this.world.random.nextInt(5);
        org.bukkit.event.entity.ExpBottleEvent event = BukkitEventFactory.callExpBottleEvent((ExperienceBottleEntity)(Object)this, i);
        i = event.getExperience();

        while (i > 0) {
            int j = ExperienceOrbEntity.roundToOrbSize(i);
            i -= j;
            this.world.spawnEntity(new ExperienceOrbEntity(this.world, ((Entity)(Object)this).getX(), ((Entity)(Object)this).getY(), ((Entity)(Object)this).getZ(), j));
        }
        this.removeBF();
        ci.cancel();
        return;
    }

}