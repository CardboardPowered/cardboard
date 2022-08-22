package org.cardboardpowered.mixin.entity;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.ActionResult;
import org.cardboardpowered.api.event.CardboardFireworkExplodeEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinInfo(events = {"FireworkExplodeEvent", "CardboradFireworkExplodeEvent"})
@Mixin(FireworkRocketEntity.class)
public class MixinFireworkRocketEntity extends MixinEntity{

    @Inject(method = "tick", cancellable = true, at = @At("HEAD"))
    private void bukkitFireworksExplode(CallbackInfo ci) {
        ActionResult result = CardboardFireworkExplodeEvent.EVENT.invoker().interact((FireworkRocketEntity) (Object) this);

        if (result == ActionResult.FAIL) {
            ci.cancel();
        }
    }

    @Inject(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private void bukkitDamageSource(CallbackInfo ci) {
        BukkitEventFactory.entityDamage = (FireworkRocketEntity) (Object) this;
    }

    @Inject(method = "explode", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private void bukkitDamageSourceReset(CallbackInfo ci) {
        BukkitEventFactory.entityDamage = null;
    }
}
