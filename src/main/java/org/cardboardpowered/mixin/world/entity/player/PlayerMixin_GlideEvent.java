package org.cardboardpowered.mixin.world.entity.player;

import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fires EntityToggleGlideEvent when a player asks to start gliding, so plugins can refuse it.
 */
@Mixin(Player.class)
@MixinInfo(events = {"EntityToggleGlideEvent"})
public class PlayerMixin_GlideEvent {

    @Inject(
            method = "tryToStartFallFlying",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;startFallFlying()V"),
            cancellable = true
    )
    private void cardboard$startGlideEvent(CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) (Object) this;

        if (self.level().isClientSide()) return;

        // Returning false makes the caller send the player a stopFallFlying update, undoing the client's guess.
        if (CraftEventFactory.callToggleGlideEvent(self, true).isCancelled()) {
            cir.setReturnValue(false);
        }
    }
}
