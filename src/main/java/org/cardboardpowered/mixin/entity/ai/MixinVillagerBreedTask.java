package org.cardboardpowered.mixin.entity.ai;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.entity.ai.brain.task.VillagerBreedTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import org.bukkit.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(VillagerBreedTask.class)
public class MixinVillagerBreedTask {

    @Redirect(method = "createChild", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;setBreedingAge(I)V",
            ordinal = 0))
    private void moveDownSetAge0(VillagerEntity instance, int i) {}

    @Redirect(method = "createChild", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;setBreedingAge(I)V",
            ordinal = 1))
    private void moveDownSetAge1(VillagerEntity instance, int i) {}

    @Inject(method = "createChild", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void breadEvent(ServerWorld world, VillagerEntity parent,
                            VillagerEntity partner, CallbackInfoReturnable<Optional<VillagerEntity>> cir,
                            VillagerEntity villagerEntity) {
        // CraftBukkit start - call EntityBreedEvent
        if (BukkitEventFactory.callEntityBreedEvent((LivingEntity) villagerEntity, (LivingEntity) parent, (LivingEntity) partner, null, null, 0).isCancelled()) {
            cir.setReturnValue(Optional.empty());
        }
        // CraftBukkit end
        parent.setBreedingAge(6000);
        partner.setBreedingAge(6000);
    }
}
