package com.fungus_soft.bukkitfabric.mixin;

import java.util.List;
import java.util.concurrent.Executor;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fungus_soft.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld implements IMixinWorld {

    @Inject(at = @At(value = "TAIL"), method = "<init>")
    public void addToBukkit(MinecraftServer server, Executor a, LevelStorage.Session b, ServerWorldProperties c,
            RegistryKey d, RegistryKey e, DimensionType f, WorldGenerationProgressListener g, ChunkGenerator h, boolean bl, long l, List<Spawner> list, boolean bl2, CallbackInfo ci){
        ((CraftServer)Bukkit.getServer()).addWorldToMap(getCraftWorld());
    }

}