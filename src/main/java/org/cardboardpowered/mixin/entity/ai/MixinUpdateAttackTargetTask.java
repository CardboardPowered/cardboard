package org.cardboardpowered.mixin.entity.ai;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryQueryResult;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.bukkit.event.entity.EntityTargetEvent;
import org.cardboardpowered.impl.entity.LivingEntityImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(UpdateAttackTargetTask.class)
public class MixinUpdateAttackTargetTask {
    
    @Inject(method = "method_47123", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/brain/MemoryQueryResult;remember(Ljava/lang/Object;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static <E extends MobEntity> void banner$targetEvent(Predicate<E> predicate, Function<E, Optional<? extends LivingEntity>> function,
                                           MemoryQueryResult memoryAccessor, MemoryQueryResult memoryAccessor2,
                                           ServerWorld serverLevel, MobEntity mob, long l, CallbackInfoReturnable<Boolean> cir,
                                           Optional optional, LivingEntity livingEntity) {
        // CraftBukkit start
        EntityTargetEvent event = BukkitEventFactory.callEntityTargetLivingEvent(mob, livingEntity, (livingEntity instanceof ServerPlayerEntity) ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
        if (event.getTarget() == null) {
            memoryAccessor.forget();
            cir.setReturnValue(true);
        }
        livingEntity = ((LivingEntityImpl) event.getTarget()).getHandle();
        // CraftBukkit end
    }

}
