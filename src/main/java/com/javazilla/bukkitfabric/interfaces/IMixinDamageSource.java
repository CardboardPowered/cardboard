package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.entity.damage.DamageSource;

public interface IMixinDamageSource {

    public boolean isSweep_BF();

    public DamageSource sweep_BF();

}