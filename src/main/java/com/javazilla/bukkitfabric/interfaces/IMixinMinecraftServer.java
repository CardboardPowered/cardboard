/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage.Session;

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

    public Session getSessionBF();

}