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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.Difficulty;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.util.Vector;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
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

    @SuppressWarnings("deprecation")
    public static GameMode toFabric(org.bukkit.GameMode arg0) {
        return GameMode.byId(arg0.getValue());
    }

    @SuppressWarnings("deprecation")
    public static org.bukkit.GameMode fromFabric(GameMode gm) {
        return org.bukkit.GameMode.getByValue(gm.id);
    }

    public static Difficulty fromFabric(net.minecraft.world.Difficulty difficulty) {
        return Difficulty.valueOf(difficulty.name());
    }

    public static Vector toBukkit(Vec3d nms) {
        return new Vector(nms.x, nms.y, nms.z);
    }

    public static Vec3d toMojang(Vector bukkit) {
        return new Vec3d(bukkit.getX(), bukkit.getY(), bukkit.getZ());
    }

    public static UUID getWorldUUID(File baseDir) {
        File file1 = new File(baseDir, "uid.dat");
        if (file1.exists()) {
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(new FileInputStream(file1));
                return new UUID(dis.readLong(), dis.readLong());
            } catch (IOException ex) {
                BukkitFabricMod.LOGGER.warning("Failed to read " + file1 + ", generating new random UUID. " + ex.getMessage());
            } finally {
                if (dis != null)
                    try { dis.close(); } catch (IOException ex) {/*NOOP*/}
            }
        }
        UUID uuid = UUID.randomUUID();
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(file1));
            dos.writeLong(uuid.getMostSignificantBits());
            dos.writeLong(uuid.getLeastSignificantBits());
        } catch (IOException ex) {
            BukkitFabricMod.LOGGER.warning("Failed to write " + file1 + ", " + ex.getMessage());
        } finally {
            if (dos != null)
                try {dos.close();} catch (IOException ex) {/*NOOP*/}
        }
        return uuid;
    }

    @SuppressWarnings("unchecked")
    public static <T, U> MemoryModuleType<U> fromMemoryKey(MemoryKey<T> memoryKey) {
        return (MemoryModuleType<U>) Registry.MEMORY_MODULE_TYPE.get(CraftNamespacedKey.toMinecraft(memoryKey.getKey()));
    }

    @SuppressWarnings("unchecked")
    public static <T, U> MemoryKey<U> toMemoryKey(MemoryModuleType<T> memoryModuleType) {
        return MemoryKey.getByKey(CraftNamespacedKey.fromMinecraft(Registry.MEMORY_MODULE_TYPE.getId(memoryModuleType)));
    }

}