package org.cardboardpowered.mixin.entity.ai;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.PrepareRamTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.cardboardpowered.impl.entity.LivingEntityImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrepareRamTask.class)
public class MixinPrepareRamTask {

    @Inject(method = "method_36270",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/task/PrepareRamTask;findRam(Lnet/minecraft/entity/mob/PathAwareEntity;Lnet/minecraft/entity/LivingEntity;)V"), cancellable = true)
    private void targetEvent(PathAwareEntity pathAwareEntity, LivingEntity mob, CallbackInfo ci) {
        // CraftBukkit start
        EntityTargetEvent event = BukkitEventFactory.callEntityTargetLivingEvent(pathAwareEntity, mob, (mob instanceof ServerPlayerEntity) ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY);
        if (event.isCancelled() || event.getTarget() == null) {
            ci.cancel();
        }
        mob = ((LivingEntityImpl) event.getTarget()).getHandle();
        // CraftBukkit end
    }
}
