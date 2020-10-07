package com.javazilla.bukkitfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.impl.potion.PotionEffectTypeImpl;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.registry.Registry;

@Mixin(StatusEffects.class)
public class MixinStatusEffects {

    static {
        for (Object effect : Registry.STATUS_EFFECT) {
            org.bukkit.potion.PotionEffectType.registerPotionEffectType(new PotionEffectTypeImpl((StatusEffect) effect));
        }
    }

}