package com.fungus_soft.bukkitfabric.mixin;

import java.util.function.BiFunction;

import org.bukkit.craftbukkit.CraftWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fungus_soft.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;

@Mixin(World.class)
public class MixinWorld implements IMixinWorld {

    private CraftWorld bukkit;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(LevelProperties levelProperties, DimensionType dimensionType, BiFunction<World, Dimension, ChunkManager> chunkManagerProvider, Profiler profiler, boolean client, CallbackInfo ci){
        this.bukkit = new CraftWorld(((ServerWorld)(Object)this));
    }

    @Override
    public CraftWorld getCraftWorld() {
        return bukkit;
    }

}