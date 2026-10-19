package org.cardboardpowered.mixin.world.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.api.event.CardboardFireworkExplodeEvent;
import org.cardboardpowered.mixin.world.entity.EntityMixin;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinInfo(events = {"FireworkExplodeEvent", "CardboradFireworkExplodeEvent"})
@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin extends EntityMixin {

    @Inject(method = "tick", cancellable = true, at = @At("HEAD"))
    private void bukkitFireworksExplode(CallbackInfo ci) {
        // Client entities have no Bukkit side; on a remote server CraftServer.console is null
        if (((FireworkRocketEntity) (Object) this).level().isClientSide()) return;
        InteractionResult result = CardboardFireworkExplodeEvent.EVENT.invoker().interact((FireworkRocketEntity) (Object) this);

        if (result == InteractionResult.FAIL) {
            ci.cancel();
        }
    }

    @Inject(method = "dealExplosionDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private void bukkitDamageSource(ServerLevel world, CallbackInfo ci) {
        CraftEventFactory.entityDamage = (FireworkRocketEntity) (Object) this;
    }

    @Inject(method = "dealExplosionDamage", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private void bukkitDamageSourceReset(ServerLevel world, CallbackInfo ci) {
        CraftEventFactory.entityDamage = null;
    }
}
