package org.cardboardpowered.mixin.stat;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import org.bukkit.event.Cancellable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerStatHandler.class)
public abstract class MixinServerStatHandler extends StatHandler {

    @Inject(method = "setStat", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/stat/StatHandler;setStat(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/stat/Stat;I)V"))
    public void statsIncl(PlayerEntity player, Stat<?> stat, int value, CallbackInfo ci) {
        Cancellable cancellable = BukkitEventFactory.handleStatisticsIncrease(player, stat, this.getStat(stat), value);
        if (cancellable != null && cancellable.isCancelled()) {
            ci.cancel();
        }
    }
}
