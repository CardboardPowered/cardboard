/**
 * Cardboard
 * Copyright (C) 2023
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package com.javazilla.bukkitfabric.interfaces;

import java.util.Map;
import java.util.Queue;

import org.bukkit.craftbukkit.CraftServer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage.Session;

public interface IMixinMinecraftServer {

    Queue<Runnable> getProcessQueue();

    Map<RegistryKey<net.minecraft.world.World>, ServerWorld> getWorldMap();

    void convertWorld(String name);

    WorldGenerationProgressListenerFactory getWorldGenerationProgressListenerFactory();

    CommandManager setCommandManager(CommandManager commandManager);

    static MinecraftServer getServer() {
        return CraftServer.server;
    }

    void loadSpawn(WorldGenerationProgressListener worldGenerationProgressListener, ServerWorld internal);

    void initWorld(ServerWorld worldserver, ServerWorldProperties iworlddataserver, SaveProperties saveData, GeneratorOptions generatorsettings);

    WorldSaveHandler getSaveHandler_BF();

    Session getSessionBF();

    void cardboard_runOnMainThread(Runnable r);

}