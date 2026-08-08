package org.bukkit.craftbukkit.block;

import net.minecraft.world.level.block.entity.BlockEntityTypes;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Bed;
import org.bukkit.block.Block;

// 26.2: beds no longer have a BlockEntity (BlockEntityTypes.BED was removed),
// so this is now a plain CraftBlockState registered per bed Material.
public class CraftBed extends CraftBlockState implements Bed {

    private final org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer persistentDataContainer =
            new org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer(
                    new org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry());

    public CraftBed(Block block) {
        super(block);
    }

    public CraftBed(World world, net.minecraft.core.BlockPos pos,
                    net.minecraft.world.level.block.state.BlockState data) {
        super(world, pos, data);
    }

    protected CraftBed(CraftBed state, Location location) {
        super(state, location);
    }

    @Override
    public DyeColor getColor() {
        return switch (this.getType()) {
            case BLACK_BED -> DyeColor.BLACK;
            case BLUE_BED -> DyeColor.BLUE;
            case BROWN_BED -> DyeColor.BROWN;
            case CYAN_BED -> DyeColor.CYAN;
            case GRAY_BED -> DyeColor.GRAY;
            case GREEN_BED -> DyeColor.GREEN;
            case LIGHT_BLUE_BED -> DyeColor.LIGHT_BLUE;
            case LIGHT_GRAY_BED -> DyeColor.LIGHT_GRAY;
            case LIME_BED -> DyeColor.LIME;
            case MAGENTA_BED -> DyeColor.MAGENTA;
            case ORANGE_BED -> DyeColor.ORANGE;
            case PINK_BED -> DyeColor.PINK;
            case PURPLE_BED -> DyeColor.PURPLE;
            case RED_BED -> DyeColor.RED;
            case WHITE_BED -> DyeColor.WHITE;
            case YELLOW_BED -> DyeColor.YELLOW;
            default -> throw new IllegalArgumentException("Unknown DyeColor for " + this.getType());
        };
    }

    @Override
    public void setColor(DyeColor color) {
        throw new UnsupportedOperationException("Must set block type to appropriate bed colour");
    }

    // 26.2: Bed still extends TileState in the Bukkit API even though MC removed
    // the bed BlockEntity, so these are satisfied without a backing block entity.
    @Override
    public boolean isSnapshot() {
        return false;
    }

    @Override
    public org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() {
        // Beds have no BlockEntity in 26.2, so there is nothing to persist to.
        // The container is real but not backed by storage; writes will not survive.
        return this.persistentDataContainer;
    }

    @Override
    public CraftBed copy() {
        return new CraftBed(this, null);
    }

    @Override
    public CraftBed copy(Location location) {
        return new CraftBed(this, location);
    }
}
