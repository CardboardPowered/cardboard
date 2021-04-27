package com.javazilla.bukkitfabric.mixin.loot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;

@Mixin(SurvivesExplosionLootCondition.class)
public class MixinSurvivesExplosionLootCondition {

    @Inject(at = @At("HEAD"), method = "test", cancellable = true)
    public void cardboard_test(LootContext loottableinfo, CallbackInfoReturnable<Boolean> ci) {
        Float ofloat = (Float) loottableinfo.get(LootContextParameters.EXPLOSION_RADIUS);
        if (null == ofloat) {
            ci.setReturnValue(true);
            return;
        }
        ci.setReturnValue(loottableinfo.getRandom().nextFloat() < (1.0F / ofloat));
        return;
    }

}