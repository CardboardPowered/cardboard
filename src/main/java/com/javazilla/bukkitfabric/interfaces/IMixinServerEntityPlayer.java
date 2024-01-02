/**
 * Cardboard - Spigot/Paper for Fabric.
 * Copyright (C) 2020-2021 Cardboard contributors
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

import org.cardboardpowered.impl.entity.PlayerImpl;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMixinServerEntityPlayer extends IMixinEntity {

    void reset();

    BlockPos getSpawnPoint(World world);

    void closeHandledScreen();

    int nextContainerCounter();

    void setConnectionBF(ClientConnection connection);

    ClientConnection getConnectionBF();

    void setBukkit(PlayerImpl plr);

    PlayerImpl getBukkit();

	void spawnIn(ServerWorld worldserver1);

	void copyFrom_unused(ServerPlayerEntity entityplayer, boolean flag);

}