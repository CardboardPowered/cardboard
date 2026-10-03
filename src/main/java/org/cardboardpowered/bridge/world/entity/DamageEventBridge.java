package org.cardboardpowered.bridge.world.entity;

/**
 * Shared re-entrancy flag for the damage events. Set while a hit is being re-applied with the
 * amount a plugin asked for, so the event fires once per hit no matter which hurtServer override
 * caught it. Lives on LivingEntity, so players inherit it.
 */
public interface DamageEventBridge {

    boolean cardboard$isApplyingEventDamage();

    void cardboard$setApplyingEventDamage(boolean applying);

}
