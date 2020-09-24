package org.bukkit.craftbukkit.entity.memory;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;

public final class CraftMemoryMapper {

    private CraftMemoryMapper() {}

    public static Object fromNms(Object object) {
        if (object instanceof GlobalPos) {
            return fromNms((GlobalPos) object);
        } else if (object instanceof Long) {
            return (Long) object;
        } else if (object instanceof UUID) {
            return (UUID) object;
        } else if (object instanceof Boolean)
            return (Boolean) object;

        throw new UnsupportedOperationException("Do not know how to map " + object);
    }

    public static Object toNms(Object object) {
        if (object == null) return null;
        else if (object instanceof Location) return toNms((Location) object);
        else if (object instanceof Long)     return (Long) object;
        else if (object instanceof UUID)     return (UUID) object;
        else if (object instanceof Boolean)  return (Boolean) object;

        throw new UnsupportedOperationException("Do not know how to map " + object);
    }

    public static Location fromNms(GlobalPos globalPos) {
        return new org.bukkit.Location(((IMixinWorld)((CraftServer) CraftServer.INSTANCE).getServer().getWorld(globalPos.getDimension())).getCraftWorld(), globalPos.getPos().getX(), globalPos.getPos().getY(), globalPos.getPos().getZ());
    }

    public static GlobalPos toNms(Location location) {
        return GlobalPos.create(((CraftWorld) location.getWorld()).getHandle().getRegistryKey(), new BlockPos(location.getX(), location.getY(), location.getZ()));
    }

}