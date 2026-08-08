package org.cardboardpowered.mixin.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.EntityTargetEvent;
import org.cardboardpowered.bridge.world.entity.MobBridge;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.server.level.ServerLevelBridge;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements MobBridge, EntityBridge {
    @Shadow
    @Nullable
    public LivingEntity target;

    @Shadow
    public abstract @Nullable LivingEntity getTarget();

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    public void setTargetCraftBukkit(LivingEntity livingEntity, CallbackInfo ci) {
        // CraftBukkit start - fire event
        boolean set = this.cardboard$setTarget(target, EntityTargetEvent.TargetReason.UNKNOWN);
        if (set) { // Let the other mods call their @Inject if set is false.
            ci.cancel();
        }
    }

    @Unique
    private static final java.util.concurrent.atomic.AtomicBoolean cardboard$warnedUnknownTarget =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    @Override
    public boolean cardboard$setTarget(@Nullable LivingEntity target, EntityTargetEvent.@Nullable TargetReason reason) {
        if (this.getTarget() == target) {
            return false;
        }
        if (reason != null) {
            if (reason == EntityTargetEvent.TargetReason.UNKNOWN && this.getTarget() != null && target == null) {
                reason = this.getTarget().isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED;
            }
            if (reason == EntityTargetEvent.TargetReason.UNKNOWN && cardboard$warnedUnknownTarget.compareAndSet(false, true)) {
                // Fires on every generic setTarget call, i.e. constantly once mobs are active.
                // Report it once per run so the signal survives without flooding the log.
                ((ServerLevelBridge)this.level()).getCraftServer().getLogger().log(java.util.logging.Level.WARNING,
                        "Unknown target reason, please report on the issue tracker (further occurrences suppressed)", new Exception());
            }
            CraftLivingEntity ctarget = null;
            if (target != null) {
                ctarget = (CraftLivingEntity) target.getBukkitEntity();
            }
            org.bukkit.event.entity.EntityTargetLivingEntityEvent event = new org.bukkit.event.entity.EntityTargetLivingEntityEvent(this.getBukkitEntity(), ctarget, reason);
            if (!event.callEvent()) {
                return false;
            }

            if (event.getTarget() != null) {
                target = ((CraftLivingEntity) event.getTarget()).getHandle();
            } else {
                target = null;
            }
        }
        this.target = target;
        return true;
        // CraftBukkit end
    }
}
