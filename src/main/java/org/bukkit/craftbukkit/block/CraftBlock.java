package org.bukkit.craftbukkit.block;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
//import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.google.common.base.Preconditions;

import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;

public class CraftBlock implements Block {
    private final net.minecraft.world.IWorld world;
    private final BlockPos position;

    public CraftBlock(IWorld world, BlockPos position) {
        this.world = world;
        this.position = position.toImmutable();
    }

    public static CraftBlock at(IWorld world, BlockPos position) {
        return new CraftBlock(world, position);
    }

    private net.minecraft.block.Block getNMSBlock() {
        return getNMS().getBlock();
    }

    public net.minecraft.block.BlockState getNMS() {
        return world.getBlockState(position);
    }

    public BlockPos getPosition() {
        return position;
    }

    @Override
    public World getWorld() {
        return new CraftWorld(world.getWorld());
    }

    public CraftWorld getCraftWorld() {
        return (CraftWorld) getWorld();
    }

    @Override
    public Location getLocation() {
        return new Location(getWorld(), position.getX(), position.getY(), position.getZ());
    }

    @Override
    public Location getLocation(Location loc) {
        if (loc != null) {
            loc.setWorld(getWorld());
            loc.setX(position.getX());
            loc.setY(position.getY());
            loc.setZ(position.getZ());
            loc.setYaw(0);
            loc.setPitch(0);
        }

        return loc;
    }

    public BlockVector getVector() {
        return new BlockVector(getX(), getY(), getZ());
    }

    @Override
    public int getX() {
        return position.getX();
    }

    @Override
    public int getY() {
        return position.getY();
    }

    @Override
    public int getZ() {
        return position.getZ();
    }

    @Override
    public Chunk getChunk() {
        return getWorld().getChunkAt(this);
    }

    public void setData(final byte data) {
        setData(data, 3);
    }

    public void setData(final byte data, boolean applyPhysics) {
        if (applyPhysics) {
            setData(data, 3);
        } else {
            setData(data, 2);
        }
    }

    private void setData(final byte data, int flag) {
        world.setBlockState(position, CraftMagicNumbers.getBlock(getType(), data), flag);
    }

    @Override
    public byte getData() {
        BlockState blockData = world.getBlockState(position);
        return CraftMagicNumbers.toLegacyData(blockData);
    }

    @Override
    public BlockData getBlockData() {
       return CraftBlockData.fromData(getNMS());
    }

    @Override
    public void setType(final Material type) {
        setType(type, true);
    }

    @Override
    public void setType(Material type, boolean applyPhysics) {
        Preconditions.checkArgument(type != null, "Material cannot be null");
        setBlockData(type.createBlockData(), applyPhysics);
    }

    @Override
    public void setBlockData(BlockData data) {
        setBlockData(data, true);
    }

    @Override
    public void setBlockData(BlockData data, boolean applyPhysics) {
        Preconditions.checkArgument(data != null, "BlockData cannot be null");
        setTypeAndData(((CraftBlockData) data).getState(), applyPhysics);
    }

    public boolean setTypeAndData(final BlockState blockData, final boolean applyPhysics) {
        // TODO auto-generated method stub
        return false;
    }

    @Override
    public Material getType() {
        return CraftMagicNumbers.getMaterial(world.getBlockState(position).getBlock());
    }

    @Override
    public byte getLightLevel() {
        return (byte) world.getWorld().getLightLevel(position);
    }

    @Override
    public byte getLightFromSky() {
        return (byte) world.getLightLevel(LightType.SKY, position);
    }

    @Override
    public byte getLightFromBlocks() {
        return (byte) world.getLightLevel(LightType.BLOCK, position);
    }


    public Block getFace(final BlockFace face) {
        return getRelative(face, 1);
    }

    public Block getFace(final BlockFace face, final int distance) {
        return getRelative(face, distance);
    }

    @Override
    public Block getRelative(final int modX, final int modY, final int modZ) {
        return getWorld().getBlockAt(getX() + modX, getY() + modY, getZ() + modZ);
    }

    @Override
    public Block getRelative(BlockFace face) {
        return getRelative(face, 1);
    }

    @Override
    public Block getRelative(BlockFace face, int distance) {
        return getRelative(face.getModX() * distance, face.getModY() * distance, face.getModZ() * distance);
    }

    @Override
    public BlockFace getFace(final Block block) {
        BlockFace[] values = BlockFace.values();

        for (BlockFace face : values) {
            if ((this.getX() + face.getModX() == block.getX()) && (this.getY() + face.getModY() == block.getY()) && (this.getZ() + face.getModZ() == block.getZ())) {
                return face;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "CraftBlock{pos=" + position + ",type=" + getType() + ",data=" + getNMS() + ",fluid=" + world.getFluidState(position) + '}';
    }

    public static BlockFace notchToBlockFace(Direction notch) {
        if (notch == null) return BlockFace.SELF;
        switch (notch) {
        case DOWN:
            return BlockFace.DOWN;
        case UP:
            return BlockFace.UP;
        case NORTH:
            return BlockFace.NORTH;
        case SOUTH:
            return BlockFace.SOUTH;
        case WEST:
            return BlockFace.WEST;
        case EAST:
            return BlockFace.EAST;
        default:
            return BlockFace.SELF;
        }
    }

    public static Direction blockFaceToNotch(BlockFace face) {
        switch (face) {
        case DOWN:
            return Direction.DOWN;
        case UP:
            return Direction.UP;
        case NORTH:
            return Direction.NORTH;
        case SOUTH:
            return Direction.SOUTH;
        case WEST:
            return Direction.WEST;
        case EAST:
            return Direction.EAST;
        default:
            return null;
        }
    }

    @Override
    public org.bukkit.block.BlockState getState() {
        // TODO auto-generated method stub
        return null;
    }

    @Override
    public Biome getBiome() {
        return getWorld().getBiome(getX(), getY(), getZ());
    }

    @Override
    public void setBiome(Biome bio) {
        getWorld().setBiome(getX(), getY(), getZ(), bio);
    }

    public static Biome biomeBaseToBiome(net.minecraft.world.biome.Biome base) {
        if (base == null) {
            return null;
        }

        return Biome.valueOf(Registry.BIOME.getId(base).getNamespace().toUpperCase(java.util.Locale.ENGLISH));
    }

    public static net.minecraft.world.biome.Biome biomeToBiomeBase(Biome bio) {
        if (bio == null) {
            return null;
        }

        return Registry.BIOME.get(new Identifier(bio.name().toLowerCase(java.util.Locale.ENGLISH)));
    }

    @Override
    public double getTemperature() {
        return world.getBiome(position).getTemperature(position);
    }

    @Override
    public double getHumidity() {
        return getWorld().getHumidity(getX(), getY(), getZ());
    }

    @Override
    public boolean isBlockPowered() {
        return world.getWorld().getReceivedRedstonePower(position) > 0;
    }

    @Override
    public boolean isBlockIndirectlyPowered() {
        // TODO auto-generated method stub
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CraftBlock)) return false;
        CraftBlock other = (CraftBlock) o;

        return this.position.equals(other.position) && this.getWorld().equals(other.getWorld());
    }

    @Override
    public int hashCode() {
        return this.position.hashCode() ^ this.getWorld().hashCode();
    }

    @Override
    public boolean isBlockFacePowered(BlockFace face) {
        return world.getWorld().isEmittingRedstonePower(position, blockFaceToNotch(face));
    }

    @Override
    public boolean isBlockFaceIndirectlyPowered(BlockFace face) {
        int power = world.getWorld().getEmittedRedstonePower(position, blockFaceToNotch(face));

        Block relative = getRelative(face);
        if (relative.getType() == Material.REDSTONE_WIRE) {
            return Math.max(power, relative.getData()) > 0;
        }

        return power > 0;
    }

    @Override
    public int getBlockPower(BlockFace face) {
        int power = 0;
        // TODO auto-generated method stub
        return power > 0 ? power : (face == BlockFace.SELF ? isBlockIndirectlyPowered() : isBlockFaceIndirectlyPowered(face)) ? 15 : 0;
    }

    @Override
    public int getBlockPower() {
        return getBlockPower(BlockFace.SELF);
    }

    @Override
    public boolean isEmpty() {
        return getNMS().isAir();
    }

    @Override
    public boolean isLiquid() {
        return getNMS().getMaterial().isLiquid();
    }

    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        return PistonMoveReaction.getById(getNMS().getPistonBehavior().ordinal());
    }

    @Override
    public boolean breakNaturally() {
        return breakNaturally(new ItemStack(Material.AIR));
    }

    @Override
    public boolean breakNaturally(ItemStack item) {
        // TODO auto-generated method stub
        return false;
    }

    @Override
    public Collection<ItemStack> getDrops() {
        return getDrops(new ItemStack(Material.AIR));
    }

    @Override
    public Collection<ItemStack> getDrops(ItemStack item) {
        return getDrops(item, null);
    }

    @Override
    public Collection<ItemStack> getDrops(ItemStack item, Entity entity) {
        // TODO auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        // TODO auto-generated method stub
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        // TODO auto-generated method stub
        return null;
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        // TODO auto-generated method stub
        return false;
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        // TODO auto-generated method stub
    }

    @Override
    public boolean isPassable() {
        return this.getNMS().getCollisionShape(world, position).isEmpty();
    }

    @Override
    public RayTraceResult rayTrace(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode) {
        // TODO auto-generated method stub
        return null;
    }

    @Override
    public BoundingBox getBoundingBox() {
        // TODO auto-generated method stub
        return null;
    }
}
