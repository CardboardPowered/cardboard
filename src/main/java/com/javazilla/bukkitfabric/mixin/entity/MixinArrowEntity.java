package com.javazilla.bukkitfabric.mixin.entity;

import java.util.Collection;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinArrowEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Mixin(ArrowEntity.class)
public class MixinArrowEntity implements IMixinArrowEntity {

    @Shadow
    public Potion potion;

    @Shadow
    public Set<StatusEffectInstance> effects;

    @Shadow
    private static TrackedData<Integer> COLOR;

    @Override
    public void setType(String string) {
        this.potion = Registry.POTION.get(new Identifier(string));
        (((Entity)(Object)this).getDataTracker()).set(COLOR, PotionUtil.getColor((Collection<StatusEffectInstance>) PotionUtil.getPotionEffects(this.potion, (Collection<StatusEffectInstance>) this.effects)));
    }

}
