package com.javazilla.bukkitfabric.mixin.world;

import java.util.List;
import java.util.concurrent.Executor;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;

@Mixin(ServerWorld.class)
public class MixinServerWorld extends MixinWorld {

    @SuppressWarnings("rawtypes")
    @Inject(at = @At("TAIL"), method = "<init>")
    public void addToBukkit(MinecraftServer server, Executor a, LevelStorage.Session b, ServerWorldProperties c,
            RegistryKey d, DimensionType f, WorldGenerationProgressListener g, ChunkGenerator h, boolean bl, long l, List<Spawner> list, boolean bl2, CallbackInfo ci){
        ((CraftServer)Bukkit.getServer()).addWorldToMap(getWorldImpl());
    }

    @Inject(at = @At("HEAD"), method = "save")
    public void doWorldSaveEvent(ProgressListener aa, boolean bb, boolean cc, CallbackInfo ci) {
        if (!cc) {
            org.bukkit.Bukkit.getPluginManager().callEvent(new org.bukkit.event.world.WorldSaveEvent(getWorldImpl())); // WorldSaveEvent
        }
    }

}