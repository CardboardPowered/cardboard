package com.fungus_soft.bukkitfabric.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.fungus_soft.bukkitfabric.interfaces.IMixinMinecraftServer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements IMixinMinecraftServer {

    @Shadow
    private final Map<DimensionType, ServerWorld> worlds;

    @Shadow
    private final WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory;

    @Shadow
    public void upgradeWorld(String name) {
    }

    public MinecraftServerMixin() {
        this.worlds = null; // Won't be called
        this.worldGenerationProgressListenerFactory = null;
    }

    @Overwrite
    public String getServerModName() {
        return "Bukkit4Fabric";
    }

    @Override
    public Map<DimensionType, ServerWorld> getWorldMap() {
        return worlds;
    }

    @Override
    public void convertWorld(String name) {
        upgradeWorld(name);
    }

    @Override
    public WorldGenerationProgressListenerFactory getWorldGenerationProgressListenerFactory() {
        return worldGenerationProgressListenerFactory;
    }

}