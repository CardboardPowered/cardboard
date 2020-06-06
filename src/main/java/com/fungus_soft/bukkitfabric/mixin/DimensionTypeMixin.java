package com.fungus_soft.bukkitfabric.mixin;

import java.util.function.BiFunction;

import org.bukkit.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.fungus_soft.bukkitfabric.interfaces.IMixinDimensionType;

import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;

@Mixin(DimensionType.class)
public class DimensionTypeMixin implements IMixinDimensionType {

    @Shadow
    private final String saveDir;

    @Shadow
    private BiFunction<net.minecraft.world.World,DimensionType,? extends Dimension> factory;

    @Shadow
    private static DimensionType register(String str, DimensionType type) {
        return null;
    }

    public DimensionTypeMixin() {
        this.saveDir = null; // Won't be called
    }

    @Override
    public String getFolder() {
        return saveDir;
    }

    @Override
    public DimensionType registerDimension(String str, DimensionType type) {
        return register(str, type);
    }

    @Override
    public BiFunction<net.minecraft.world.World, DimensionType, ? extends Dimension> getFactory() {
        return factory;
    }


}