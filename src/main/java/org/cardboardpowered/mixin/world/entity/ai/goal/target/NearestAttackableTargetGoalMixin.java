package org.cardboardpowered.mixin.world.entity.ai.goal.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.cardboardpowered.bridge.world.entity.MobBridge;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Reports the reason a mob picked its target through this goal. Going through Mob.setTarget
 * leaves it as TargetReason.UNKNOWN, which is both useless to plugins and the single loudest
 * source of the unknown reason warning.
 */
@Mixin(NearestAttackableTargetGoal.class)
@MixinInfo(events = {"EntityTargetLivingEntityEvent"})
public class NearestAttackableTargetGoalMixin {

    @Redirect(
            method = "start",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V")
    )
    private void cardboard$setTargetWithReason(Mob mob, LivingEntity target) {
        ((MobBridge) mob).cardboard$setTarget(target, target instanceof Player
                ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER
                : EntityTargetEvent.TargetReason.CLOSEST_ENTITY);
    }
}
