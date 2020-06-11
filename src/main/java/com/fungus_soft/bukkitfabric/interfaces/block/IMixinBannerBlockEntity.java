package com.fungus_soft.bukkitfabric.interfaces.block;

import net.minecraft.nbt.ListTag;
import net.minecraft.util.DyeColor;

public interface IMixinBannerBlockEntity {

    public ListTag patternListTag();

    public DyeColor baseColor();

    public void setBaseColor(DyeColor dye);

    public void setPatternListTag(ListTag newPatterns);

}