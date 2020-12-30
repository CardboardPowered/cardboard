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

import org.bukkit.Location;

import com.mojang.authlib.GameProfile;

import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public interface IMixinPlayerManager {

    ServerPlayerEntity moveToWorld(ServerPlayerEntity player, ServerWorld world, boolean flag, Location location, boolean avoidSuffocation);

    ServerPlayerEntity attemptLogin(ServerLoginNetworkHandler loginlistener, GameProfile gameprofile, String hostname);

    void sendScoreboardBF(ServerScoreboard newboard, ServerPlayerEntity handle);

}