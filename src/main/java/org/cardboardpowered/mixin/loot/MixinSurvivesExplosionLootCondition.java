package org.cardboardpowered.mixin.loot;

import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SurvivesExplosionLootCondition.class)
public class MixinSurvivesExplosionLootCondition {

    @Inject(at = @At("HEAD"), method = "test(Lnet/minecraft/loot/context/LootContext;)Z", cancellable = true)
    public void cardboard_test(LootContext loottableinfo, CallbackInfoReturnable<Boolean> ci) {
        Float ofloat = loottableinfo.get(LootContextParameters.EXPLOSION_RADIUS);
        if (ofloat == null) {
            ci.setReturnValue(true);
            return;
        }
        ci.setReturnValue(loottableinfo.getRandom().nextFloat() < (1.0F / ofloat));
        return;
    }

}
