package org.cardboardpowered.mixin.item;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ArmorStandItem;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@MixinInfo(events = {"EntityPlaceEvent"})
@Mixin(value = ArmorStandItem.class, priority = 900)
public class MixinArmorStandItem {

    private transient ArmorStandEntity bukkitEntity;

    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ArmorStandEntity;refreshPositionAndAngles(DDDFF)V"))
    private void bukkitCaptureEntity(ArmorStandEntity instance, double x, double y, double z, float yaw, float pitch) {
        instance.refreshPositionAndAngles(x, y, z, yaw, pitch);
        bukkitEntity = instance;
    }

    @Inject(method = "useOnBlock", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V"))
    public void bukkitEntityPlace(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (BukkitEventFactory.callEntityPlaceEvent(context, bukkitEntity).isCancelled()) {
            cir.setReturnValue(ActionResult.FAIL);
        }
        bukkitEntity = null;
    }
}