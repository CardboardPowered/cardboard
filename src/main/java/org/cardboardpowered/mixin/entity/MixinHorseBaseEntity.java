package org.cardboardpowered.mixin.entity;

import org.bukkit.entity.AbstractHorse;
import org.bukkit.event.entity.HorseJumpEvent;
import org.cardboardpowered.interfaces.IHorseBaseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinInventory;

import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.inventory.SimpleInventory;

@Mixin(HorseBaseEntity.class)
public class MixinHorseBaseEntity implements IHorseBaseEntity {
    
    @Shadow
    public SimpleInventory items;

    @Inject(at = @At("HEAD"), method = "startJumping", cancellable = true)
    public void callJumpEvent(int i, CallbackInfo ci) {
        float power = (i >= 90) ? 1.0F : (0.4F + 0.4F * (float) i / 90.0F);

        HorseJumpEvent event = BukkitEventFactory.callHorseJumpEvent((HorseBaseEntity)(Object)this, power);
        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
    }
    
    @Inject(at = @At("TAIL"), method = "onChestedStatusChanged")
    public void cardboard$setInvOwner(CallbackInfo ci) {
        ((IMixinInventory)items).cardboard$setOwner( (AbstractHorse) ((IMixinEntity)(Object)this).getBukkitEntity() );
    }

    @Override
    public SimpleInventory cardboard$get_items() {
        return items;
    }

}