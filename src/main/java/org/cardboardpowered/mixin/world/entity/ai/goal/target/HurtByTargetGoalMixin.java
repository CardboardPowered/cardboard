package org.cardboardpowered.mixin.world.entity.ai.goal.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import org.bukkit.event.entity.EntityTargetEvent;
import org.cardboardpowered.bridge.world.entity.MobBridge;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Reports why a mob retaliated. Going through Mob.setTarget leaves the reason as
 * TargetReason.UNKNOWN, which is useless to plugins and, since every mob that is ever hit
 * reaches this goal, a loud source of the unknown reason warning.
 */
@Mixin(HurtByTargetGoal.class)
@MixinInfo(events = {"EntityTargetLivingEntityEvent"})
public class HurtByTargetGoalMixin {

    @Redirect(
            method = "start",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V")
    )
    private void cardboard$setTargetWithReason(Mob mob, LivingEntity target) {
        ((MobBridge) mob).cardboard$setTargetWithReason(target, EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY);
    }

    @Redirect(
            method = "alertOther",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V")
    )
    private void cardboard$alertOtherWithReason(Mob other, LivingEntity target) {
        ((MobBridge) other).cardboard$setTargetWithReason(target, EntityTargetEvent.TargetReason.TARGET_ATTACKED_NEARBY_ENTITY);
    }
}
