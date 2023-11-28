package org.bukkit.craftbukkit.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.PositionImpl;
import net.minecraft.util.math.Vec3d;
import org.bukkit.Location;
import org.bukkit.World;

import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

public final class CraftLocation {

    private CraftLocation() {
    }

    public static Location toBukkit(Vec3d vec3D) {
        return toBukkit(vec3D, null);
    }

    public static Location toBukkit(Vec3d vec3D, World world) {
        return toBukkit(vec3D, world, 0.0F, 0.0F);
    }

    public static Location toBukkit(Vec3d vec3D, World world, float yaw, float pitch) {
        return new Location(world, vec3D.getX(), vec3D.getY(), vec3D.getZ(), yaw, pitch);
    }

    public static Location toBukkit(BlockPos BlockPos) {
        return toBukkit(BlockPos,  (World) null);
    }

    public static Location toBukkit(BlockPos blockPosition, net.minecraft.world.World world) {
        return toBukkit(blockPosition, ((IMixinWorld) world).getWorldImpl(), 0.0F, 0.0F);
    }

    public static Location toBukkit(BlockPos BlockPos, World world) {
        return toBukkit(BlockPos, world, 0.0F, 0.0F);
    }

    public static Location toBukkit(BlockPos BlockPos, World world, float yaw, float pitch) {
        return new Location(world, BlockPos.getX(), BlockPos.getY(), BlockPos.getZ(), yaw, pitch);
    }

    public static Location toBukkit(PositionImpl position) {
        return toBukkit(position, null, 0.0F, 0.0F);
    }

    public static Location toBukkit(PositionImpl position, World world) {
        return toBukkit(position, world, 0.0F, 0.0F);
    }

    public static Location toBukkit(PositionImpl position, World world, float yaw, float pitch) {
        return new Location(world, position.getX(), position.getY(), position.getZ(), yaw, pitch);
    }

    public static BlockPos toBlockPosition(Location location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static PositionImpl toPosition(Location location) {
        return new PositionImpl(location.getX(), location.getY(), location.getZ());
    }

    public static Vec3d toVec3D(Location location) {
        return new Vec3d(location.getX(), location.getY(), location.getZ());
    }
}
