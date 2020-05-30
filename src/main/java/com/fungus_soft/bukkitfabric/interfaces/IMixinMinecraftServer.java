package com.fungus_soft.bukkitfabric.interfaces;

import java.util.Map;
import java.util.Queue;

import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.dimension.DimensionType;

public interface IMixinMinecraftServer {

    public Queue<Runnable> getProcessQueue();

    public Map<DimensionType, ServerWorld> getWorldMap();

    public void convertWorld(String name);

    public WorldGenerationProgressListenerFactory getWorldGenerationProgressListenerFactory();

}