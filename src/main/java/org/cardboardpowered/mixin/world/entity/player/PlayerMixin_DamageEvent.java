package org.cardboardpowered.mixin.world.entity.player;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityDamageEvent;
import org.cardboardpowered.bridge.world.entity.DamageEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Player#hurtServer ends with {@code f == 0.0F ? false : super.hurtServer(...)}, so a hit that
 * deals no damage — a snowball or a thrown egg — never reaches LivingEntity#hurtServer, where the
 * damage events are fired. Fire the event here for that case, and let the hit through when a plugin
 * turns it into real damage. Mobs need none of this: nothing filters a zero out on their way down.
 */
@Mixin(Player.class)
public class PlayerMixin_DamageEvent {

    @Inject(at = @At("HEAD"), method = "hurtServer", cancellable = true)
    private void cardboard_doZeroDamageEvent(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (amount != 0.0F) return; // Real damage reaches the event on its own.

        Player self = (Player) (Object) this;
        DamageEventBridge bridge = (DamageEventBridge) self;
        if (bridge.cardboard$isApplyingEventDamage()) return;

        // The same guards vanilla applies before it gets to the zero check.
        if (self.isInvulnerableTo(world, source)) return;
        if (self.getAbilities().invulnerable && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (self.isDeadOrDying()) return;

        // Difficulty scaling sits between here and the zero check, and leaves a zero a zero.
        EntityDamageEvent event = CraftEventFactory.callEntityDamageEvent(self, source, 0.0F);
        if (event.isCancelled() || event.getDamage() == 0.0D) {
            cir.setReturnValue(false); // What vanilla would have returned.
            return;
        }

        bridge.cardboard$setApplyingEventDamage(true);
        try {
            cir.setReturnValue(self.hurtServer(world, source, (float) event.getDamage()));
        } finally {
            bridge.cardboard$setApplyingEventDamage(false);
        }
    }

}
