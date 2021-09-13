package org.cardboardpowered.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import org.cardboardpowered.interfaces.ITnt;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;

@Mixin(TntEntity.class)
public class MixinTntEntity implements ITnt {

    @Shadow
    public LivingEntity causingEntity;

    @Override
    public void setSourceBF(LivingEntity entity) {
        this.causingEntity = entity;
    }

}
