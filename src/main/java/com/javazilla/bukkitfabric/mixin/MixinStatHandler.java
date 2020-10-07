package com.javazilla.bukkitfabric.mixin;

import org.bukkit.event.Cancellable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;

@Mixin(StatHandler.class)
public class MixinStatHandler {

    @Overwrite
    public void increaseStat(PlayerEntity player, Stat<?> statistic, int i) {
        int j = (int) Math.min((long) this.getStat(statistic) + (long) i, 2147483647L);

        Cancellable cancellable = BukkitEventFactory.handleStatisticsIncrease(player, statistic, this.getStat(statistic), j);
        if (cancellable != null && cancellable.isCancelled()) return;
        this.setStat(player, statistic, j);
    }

    @Shadow
    public void setStat(PlayerEntity player, Stat<?> statistic, int i) {
    }

    @Shadow
    public int getStat(Stat<?> statistic) {
        return 0;
    }

}