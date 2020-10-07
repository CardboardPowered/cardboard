package com.javazilla.bukkitfabric.impl;

import com.google.common.base.Preconditions;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.craftbukkit.CraftWorld;

public class WorldBorderImpl implements WorldBorder {

    private final World world;
    private final net.minecraft.world.border.WorldBorder handle;

    public WorldBorderImpl(CraftWorld world) {
        this.world = world;
        this.handle = world.getHandle().getWorldBorder();
    }

    @Override
    public void reset() {
        this.setSize(6.0E7D);
        this.setDamageAmount(0.2D);
        this.setDamageBuffer(5.0D);
        this.setWarningDistance(5);
        this.setWarningTime(15);
        this.setCenter(0, 0);
    }

    @Override
    public double getSize() {
        return this.handle.getSize();
    }

    @Override
    public void setSize(double newSize) {
        this.setSize(newSize, 0L);
    }

    @Override
    public void setSize(double newSize, long time) {
        newSize = Math.min(6.0E7D, Math.max(1.0D, newSize));
        time = Math.min(9223372036854775L, Math.max(0L, time));

        if (time > 0L)
            this.handle.interpolateSize(this.handle.getSize(), newSize, time * 1000L);
        else this.handle.setSize(newSize);
    }

    @Override
    public Location getCenter() {
        return new Location(this.world, this.handle.getCenterX(), 0, this.handle.getCenterZ());
    }

    @Override
    public void setCenter(double x, double z) {
        this.handle.setCenter(Math.min(3.0E7D, Math.max(-3.0E7D, x)), Math.min(3.0E7D, Math.max(-3.0E7D, z)));
    }

    @Override
    public void setCenter(Location location) {
        this.setCenter(location.getX(), location.getZ());
    }

    @Override
    public double getDamageBuffer() {
        return this.handle.getBuffer();
    }

    @Override
    public void setDamageBuffer(double blocks) {
        this.handle.setBuffer(blocks);
    }

    @Override
    public double getDamageAmount() {
        return this.handle.getDamagePerBlock();
    }

    @Override
    public void setDamageAmount(double damage) {
        this.handle.setDamagePerBlock(damage);
    }

    @Override
    public int getWarningTime() {
        return this.handle.getWarningTime();
    }

    @Override
    public void setWarningTime(int time) {
        this.handle.setWarningTime(time);
    }

    @Override
    public int getWarningDistance() {
        return this.handle.getWarningBlocks();
    }

    @Override
    public void setWarningDistance(int distance) {
        this.handle.setWarningBlocks(distance);
    }

    @Override
    public boolean isInside(Location location) {
        Preconditions.checkArgument(location != null, "Null Location");
        return location.getWorld().equals(this.world) && this.handle.contains(new BlockPos(location.getX(), location.getY(), location.getZ()));
    }

}
