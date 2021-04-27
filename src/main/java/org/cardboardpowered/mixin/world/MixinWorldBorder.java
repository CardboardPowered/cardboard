package org.cardboardpowered.mixin.world;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;

@Mixin(WorldBorder.class)
public class MixinWorldBorder {

    @Shadow
    public List<WorldBorderListener> listeners;

    @Inject(at = @At("HEAD"), method = "addListener", cancellable = true)
    public void addListenerBF(WorldBorderListener listener, CallbackInfo ci) {
        if (listeners.contains(listener)) {
            ci.cancel();
            return;
        }
    }

}
