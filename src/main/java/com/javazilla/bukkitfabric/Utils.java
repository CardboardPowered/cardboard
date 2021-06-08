/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
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
 * 
 * You should have received a copy of the GNU General Public License
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
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.EquipmentSlot;

import org.cardboardpowered.impl.world.WorldImpl;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class Utils {

    public static String getGitHash() {
        try {
            Class<?> version = Class.forName("com.javazilla.bukkitfabric.GitVersion");
            return (String) version.getField("GIT_SHA").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            return "-unknown-";
        }
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
            } finally { if (dis != null) try { dis.close(); } catch (IOException ex) {/*NOOP*/} }
        }
        UUID uuid = UUID.randomUUID();
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(file1));
            dos.writeLong(uuid.getMostSignificantBits());
            dos.writeLong(uuid.getLeastSignificantBits());
        } catch (IOException ex) {
            BukkitFabricMod.LOGGER.warning("Failed to write " + file1 + ", " + ex.getMessage());
        } finally { if (dos != null) try {dos.close();} catch (IOException ex) {/*NOOP*/} }
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

    public static Object fromNmsGlobalPos(Object object) {
        if (object instanceof GlobalPos) return fromNmsGlobalPos((GlobalPos) object);
        else if (object instanceof Long) return object;
        else if (object instanceof UUID) return object;
        else if (object instanceof Boolean) return object;
        throw new UnsupportedOperationException("Do not know how to map " + object);
    }

    public static Object toNmsGlobalPos(Object object) {
        if (object == null) return null;
        else if (object instanceof Location) return toNmsGlobalPos((Location) object);
        else if (object instanceof Long)     return object;
        else if (object instanceof UUID)     return object;
        else if (object instanceof Boolean)  return object;
        throw new UnsupportedOperationException("Do not know how to map " + object);
    }

    public static Location fromNmsGlobalPos(GlobalPos globalPos) {
        return new org.bukkit.Location(((IMixinWorld) Objects.requireNonNull(CraftServer.INSTANCE.getServer().getWorld(globalPos.getDimension()))).getWorldImpl(), globalPos.getPos().getX(), globalPos.getPos().getY(), globalPos.getPos().getZ());
    }

    public static GlobalPos toNmsGlobalPos(Location location) {
        return GlobalPos.create(((WorldImpl) Objects.requireNonNull(location.getWorld())).getHandle().getRegistryKey(), new BlockPos(location.getX(), location.getY(), location.getZ()));
    }

    private static final net.minecraft.entity.EquipmentSlot[] slots = new net.minecraft.entity.EquipmentSlot[EquipmentSlot.values().length];
    private static final EquipmentSlot[] enums = new EquipmentSlot[net.minecraft.entity.EquipmentSlot.values().length];

    static {
        set(EquipmentSlot.HAND, net.minecraft.entity.EquipmentSlot.MAINHAND);
        set(EquipmentSlot.OFF_HAND, net.minecraft.entity.EquipmentSlot.OFFHAND);
        set(EquipmentSlot.FEET, net.minecraft.entity.EquipmentSlot.FEET);
        set(EquipmentSlot.LEGS, net.minecraft.entity.EquipmentSlot.LEGS);
        set(EquipmentSlot.CHEST, net.minecraft.entity.EquipmentSlot.CHEST);
        set(EquipmentSlot.HEAD, net.minecraft.entity.EquipmentSlot.HEAD);
    }

    private static void set(EquipmentSlot type, net.minecraft.entity.EquipmentSlot value) {
        slots[type.ordinal()] = value;
        enums[value.ordinal()] = type;
    }

    public static EquipmentSlot getSlot(net.minecraft.entity.EquipmentSlot nms) {
        return enums[nms.ordinal()];
    }

    public static net.minecraft.entity.EquipmentSlot getNMS(EquipmentSlot slot) {
        return slots[slot.ordinal()];
    }

}