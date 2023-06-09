package com.javazilla.bukkitfabric.interfaces;

import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.cardboardpowered.impl.CardboardAttributable;

public interface IMixinLivingEntity {

    int getExpReward();

    void pushEffectCause(EntityPotionEffectEvent.Cause cause);
    CardboardAttributable cardboard_getAttr();

}