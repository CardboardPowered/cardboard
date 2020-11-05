package com.javazilla.bukkitfabric.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinTnt;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;

@Mixin(TntEntity.class)
public class MixinTntEntity implements IMixinTnt {

    @Shadow
    public LivingEntity causingEntity;

    @Override
    public void setSourceBF(LivingEntity entity) {
        this.causingEntity = entity;
    }

}
