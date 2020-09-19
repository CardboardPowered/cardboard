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
package com.javazilla.bukkitfabric;

import org.bukkit.Difficulty;

import net.minecraft.world.GameMode;

public class Utils {

    public static String getGitHash() {
        try {
            Class<?> version = Class.forName("com.javazilla.bukkitfabric.GitVersion");
            return (String) version.getField("GIT_SHA").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            return "dev";
        }
    }

    public static GameMode toFabric(org.bukkit.GameMode arg0) {
        switch (arg0) {
            case ADVENTURE:
                return GameMode.ADVENTURE;
            case CREATIVE:
                return GameMode.CREATIVE;
            case SPECTATOR:
                return GameMode.SPECTATOR;
            case SURVIVAL:
                return GameMode.SURVIVAL;
            default:
                break;
        }
        return GameMode.NOT_SET;
    }

    public static org.bukkit.GameMode fromFabric(GameMode gm) {
        switch (gm) {
            case ADVENTURE:
                return org.bukkit.GameMode.ADVENTURE;
            case CREATIVE:
                return org.bukkit.GameMode.CREATIVE;
            case NOT_SET:
                return org.bukkit.GameMode.SURVIVAL; 
            case SPECTATOR:
                return org.bukkit.GameMode.SPECTATOR;
            case SURVIVAL:
                return org.bukkit.GameMode.SURVIVAL;
            default:
                break;
        }
        return org.bukkit.GameMode.SURVIVAL;
    }

    public static Difficulty fromFabric(net.minecraft.world.Difficulty difficulty) {
        return Difficulty.valueOf(difficulty.name());
    }

}