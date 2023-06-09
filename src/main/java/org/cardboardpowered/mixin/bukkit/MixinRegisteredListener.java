package org.cardboardpowered.mixin.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredListener;
import org.cardboardpowered.fabric.FabricHookBukkitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author wdog5
 * an implementation of FabricHookBukkit Event, allow mods to hook bukkit events
 * {@link org.cardboardpowered.fabric.FabricHookBukkitEvent}
 */
@Mixin(value = RegisteredListener.class, remap = false)
public class MixinRegisteredListener {

    @Inject(method = "callEvent", at = @At(value = "INVOKE",
            target = "Lorg/bukkit/plugin/EventExecutor;execute(Lorg/bukkit/event/Listener;Lorg/bukkit/event/Event;)V",
            shift = At.Shift.BEFORE))
    private void hookEvent(Event event, CallbackInfo ci) {
        if (Bukkit.getServer() != null) {
            FabricHookBukkitEvent.EVENT.invoker().hook(event);
        }
    }
}
