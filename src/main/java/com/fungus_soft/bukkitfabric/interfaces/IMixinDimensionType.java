package com.fungus_soft.bukkitfabric.interfaces;

import net.minecraft.world.dimension.DimensionType;

public interface IMixinDimensionType {

    public String getFolder();

    public DimensionType registerDimension(String str, DimensionType type);

}