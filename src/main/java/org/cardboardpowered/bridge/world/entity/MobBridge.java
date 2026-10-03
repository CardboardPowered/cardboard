package org.cardboardpowered.bridge.world.entity;

import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jspecify.annotations.Nullable;

public interface MobBridge {
    boolean cardboard$setTarget(@Nullable LivingEntity target, EntityTargetEvent.@Nullable TargetReason reason);

    /**
     * Calls the mob's own {@code setTarget}, reporting {@code reason} to
     * {@link org.bukkit.event.entity.EntityTargetLivingEntityEvent} instead of UNKNOWN.
     * Goals should use this rather than calling {@link #cardboard$setTarget} directly, which
     * would skip the subclass overrides of {@code setTarget} that Creeper, EnderMan, Fox and
     * ZombifiedPiglin rely on.
     */
    void cardboard$setTargetWithReason(@Nullable LivingEntity target, EntityTargetEvent.TargetReason reason);
}
