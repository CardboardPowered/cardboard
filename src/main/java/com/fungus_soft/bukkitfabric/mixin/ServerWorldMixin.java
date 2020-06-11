package com.fungus_soft.bukkitfabric.mixin;

import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fungus_soft.bukkitfabric.interfaces.IMixinBukkitGetter;
import com.fungus_soft.bukkitfabric.interfaces.IMixinServerWorld;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements IMixinBukkitGetter, IMixinServerWorld {

    protected ServerWorldMixin(LevelProperties levelProperties, DimensionType dimensionType,
            BiFunction<World, Dimension, ChunkManager> chunkManagerProvider, Profiler profiler, boolean isClient) {
        super(levelProperties, dimensionType, chunkManagerProvider, profiler, isClient);
    }


    @Inject(at = @At(value = "HEAD"), method = "init")
    public void addToBukkit(LevelInfo d, CallbackInfo ci){
        ((CraftServer)Bukkit.getServer()).addWorldToMap(getCraftWorld());
    }

    @Override
    public CraftWorld getBukkitObject() {
        return getCraftWorld();
    }

}