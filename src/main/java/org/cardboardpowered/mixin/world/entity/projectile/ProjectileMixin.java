package org.cardboardpowered.mixin.world.entity.projectile;

import org.bukkit.projectiles.ProjectileSource;
import org.cardboardpowered.mixin.world.entity.EntityMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.world.entity.EntityBridge;

@Mixin(Projectile.class)
public class ProjectileMixin extends EntityMixin {

    @Inject(at = @At("TAIL"), method = "setOwner")
    public void setProjectileSource(EntityReference<Entity> entity, CallbackInfo ci) {
        // recreateFromPacket sets the owner on client projectiles, which have no Bukkit side
        if (((Projectile)(Object)this).level().isClientSide()) return;
    	cb$refreshProjectileSource(false);
    }

    @Inject(at = @At("HEAD"), method = "onHit")
    public void fireProjectileHitEvent(HitResult hitResult, CallbackInfo ci) {
        // ThrowableProjectile.tick runs hit detection on the client too
        if (((Projectile)(Object)this).level().isClientSide()) return;
        CraftEventFactory.callProjectileHitEvent((Projectile)(Object)this, hitResult);
    }

    @Shadow
    public void onHitBlock(BlockHitResult blockHitResult) {
    }
    
    private void cb$refreshProjectileSource(boolean fillCache) {
        CraftEntity craftEntity;
        Entity owner;
        if (fillCache) {
            this.getOwner();
        }
        if ((owner = this.getOwner()) != null && this.projectileSource == null && (craftEntity = ((EntityBridge)owner).getBukkitEntity()) instanceof ProjectileSource) {
            ProjectileSource source = (ProjectileSource)craftEntity;
            this.projectileSource = source;
        }
    }
    
    @Shadow
    public Entity getOwner() {
        return null; // Shadowed
    }

}