package com.javazilla.bukkitfabric.interfaces;

import java.util.Map;
import java.util.Queue;

import org.bukkit.craftbukkit.CraftServer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.ServerWorldProperties;

public interface IMixinMinecraftServer {

    public Queue<Runnable> getProcessQueue();

    public Map<RegistryKey<net.minecraft.world.World>, ServerWorld> getWorldMap();

    public void convertWorld(String name);

    public WorldGenerationProgressListenerFactory getWorldGenerationProgressListenerFactory();

    public CommandManager setCommandManager(CommandManager commandManager);

    public static MinecraftServer getServer() {
        return CraftServer.server;
    }

    public void loadSpawn(WorldGenerationProgressListener worldGenerationProgressListener, ServerWorld internal);

    public void initWorld(ServerWorld worldserver, ServerWorldProperties iworlddataserver, SaveProperties saveData, GeneratorOptions generatorsettings);

    public WorldSaveHandler getSaveHandler_BF();

}