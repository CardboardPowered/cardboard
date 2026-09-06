package org.cardboardpowered.mixin.world.entity;

import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fires EntityToggleGlideEvent when the game stops an elytra flight on its own,
 * which is everything but the player-initiated start handled in PlayerMixin_GlideEvent.
 */
@Mixin(LivingEntity.class)
@MixinInfo(events = {"EntityToggleGlideEvent"})
public class LivingEntityMixin_GlideEvent {

    @Redirect(
            method = "updateFallFlying",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setSharedFlag(IZ)V")
    )
    private void cardboard$stopGlideEvent(LivingEntity self, int flag, boolean value) {
        // Vanilla clears flag 7 every tick the entity cannot glide, so only report an actual change.
        if (flag == 7 && self.isFallFlying() != value
                && CraftEventFactory.callToggleGlideEvent(self, value).isCancelled()) {
            return;
        }
        self.setSharedFlag(flag, value);
    }
}
