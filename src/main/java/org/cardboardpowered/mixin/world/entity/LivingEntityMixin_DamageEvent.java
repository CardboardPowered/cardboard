package org.cardboardpowered.mixin.world.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fires EntityDamageEvent / EntityDamageByEntityEvent so plugins can cancel or rescale damage.
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin_DamageEvent {

    @Shadow protected float lastHurt;

    // Set while re-entering hurtServer with the damage the event asked for, so the event fires once.
    @Unique private boolean cardboard$applyingEventDamage = false;

    @Inject(at = @At("HEAD"), method = "hurtServer", cancellable = true)
    private void cardboard_doEntityDamageEvent(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.cardboard$applyingEventDamage) return;

        LivingEntity self = (LivingEntity) (Object) this;

        // Vanilla drops these on the floor before doing anything, so there is no damage to report.
        if (self.isInvulnerableTo(world, source) || self.isDeadOrDying()) return;
        if (source.is(DamageTypeTags.IS_FIRE) && self.hasEffect(MobEffects.FIRE_RESISTANCE)) return;

        float damage = Math.max(amount, 0.0F);

        // The hurt cooldown swallows anything that isn't stronger than the hit we are still recovering from.
        if (self.invulnerableTime > 10 && !source.is(DamageTypeTags.BYPASSES_COOLDOWN) && damage <= this.lastHurt) return;

        EntityDamageEvent event = CraftEventFactory.callEntityDamageEvent(self, source, damage);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
            return;
        }

        // Note: the event carries the damage before shield blocking and armor, which is what
        // setDamage(double) overrides; the remaining reductions still apply on top of it.
        float newDamage = (float) event.getDamage();
        if (newDamage == damage) return;

        this.cardboard$applyingEventDamage = true;
        try {
            cir.setReturnValue(self.hurtServer(world, source, newDamage));
        } finally {
            this.cardboard$applyingEventDamage = false;
        }
    }

}
