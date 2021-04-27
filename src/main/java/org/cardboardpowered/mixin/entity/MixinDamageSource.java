package org.cardboardpowered.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.interfaces.IMixinDamageSource;

import net.minecraft.entity.damage.DamageSource;

@Mixin(DamageSource.class)
public class MixinDamageSource implements IMixinDamageSource {

    private boolean sweep_BF;

    @Override
    public boolean isSweep_BF() {
        return sweep_BF;
    }

    @Override
    public DamageSource sweep_BF() {
        sweep_BF = true;
        return (DamageSource)(Object)this;
    }

}