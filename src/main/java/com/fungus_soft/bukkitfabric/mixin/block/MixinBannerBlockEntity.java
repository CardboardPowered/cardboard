package com.fungus_soft.bukkitfabric.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.DyeColor;

import com.fungus_soft.bukkitfabric.interfaces.block.IMixinBannerBlockEntity;

@Mixin(BannerBlockEntity.class)
public class MixinBannerBlockEntity implements IMixinBannerBlockEntity {

    @Shadow
    public ListTag patternListTag;

    @Shadow
    public DyeColor baseColor;

    @Override
    public ListTag patternListTag() {
        return patternListTag;
    }

    @Override
    public DyeColor baseColor() {
        return baseColor;
    }

    @Override
    public void setBaseColor(DyeColor dye) {
        this.baseColor = dye;
    }

    @Override
    public void setPatternListTag(ListTag newPatterns) {
        this.patternListTag = newPatterns;
    }

}