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

    @Override
    public boolean cardboard$setTarget(@Nullable LivingEntity target, EntityTargetEvent.@Nullable TargetReason reason) {
        if (this.getTarget() == target) {
            return false;
        }
        if (reason != null) {
            if (reason == EntityTargetEvent.TargetReason.UNKNOWN && this.getTarget() != null && target == null) {
                reason = this.getTarget().isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED;
            }
            if (reason == EntityTargetEvent.TargetReason.UNKNOWN) {
                cardboard$warnUnknownTargetReason(target);
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

    // Cardboard start - the warning below fires from vanilla/mod code paths that have no mapped
    // TargetReason, which can happen every tick for every mob. Log each distinct call site once.
    @org.spongepowered.asm.mixin.Unique
    private static final java.util.Set<String> cardboard$reportedUnknownTargetSites = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @org.spongepowered.asm.mixin.Unique
    private void cardboard$warnUnknownTargetReason(@Nullable LivingEntity newTarget) {
        java.util.List<StackWalker.StackFrame> callers = StackWalker.getInstance().walk(frames -> frames
                .dropWhile(frame -> frame.getMethodName().startsWith("cardboard$")
                        || frame.getMethodName().equals("setTargetCraftBukkit")
                        || frame.getMethodName().equals("setTarget"))
                .limit(4)
                .collect(java.util.stream.Collectors.toList()));

        String mobType = EntityType.getKey(this.getType()).toString();
        String callSite = callers.isEmpty() ? "unknown" : callers.get(0).toString();
        if (!cardboard$reportedUnknownTargetSites.add(mobType + '@' + callSite)) {
            return;
        }

        StringBuilder message = new StringBuilder()
                .append("Unknown EntityTargetEvent.TargetReason for ").append(mobType)
                .append(" (uuid=").append(this.getUUID())
                .append(", world=").append(this.level().dimension().identifier())
                .append(", pos=").append(this.blockPosition().toShortString())
                .append("), old target=").append(cardboard$describeTarget(this.getTarget()))
                .append(", new target=").append(cardboard$describeTarget(newTarget))
                .append("\n  called from:");
        for (StackWalker.StackFrame frame : callers) {
            message.append("\n    ").append(frame);
        }
        message.append("\n  Only the first occurrence per mob type and call site is logged.")
                .append(" Please report this on the Cardboard issue tracker.");

        ((ServerLevelBridge) this.level()).getCraftServer().getLogger()
                .log(java.util.logging.Level.WARNING, message.toString());
    }

    @org.spongepowered.asm.mixin.Unique
    private static String cardboard$describeTarget(@Nullable LivingEntity entity) {
        return entity == null ? "none" : EntityType.getKey(entity.getType()) + "/" + entity.getUUID();
    }
    // Cardboard end
}
