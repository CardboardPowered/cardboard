package com.fungus_soft.bukkitfabric.interfaces;

import java.util.function.BiFunction;

import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;

public interface IMixinDimensionType {

    public String getFolder();

    public DimensionType registerDimension(String str, DimensionType type);

    public BiFunction<net.minecraft.world.World,DimensionType,? extends Dimension> getFactory();

}