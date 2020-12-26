package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.entity.damage.DamageSource;

public interface IMixinDamageSource {

    boolean isSweep_BF();

    DamageSource sweep_BF();

}