package org.bukkit.craftbukkit.block;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Chunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundGroup;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.destroystokyo.paper.block.BlockSoundGroup;
import com.google.common.base.Preconditions;

import org.cardboardpowered.impl.block.*;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.world.WorldImpl;
import org.jetbrains.annotations.NotNull;

import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.LightType;

public class CraftBlock implements Block {

    private final ServerWorld world;
    private final BlockPos position;

    public CraftBlock(ServerWorld world, BlockPos position) {
        this.world = world;
        this.position = position.toImmutable();
    }

    public static CraftBlock at(ServerWorld world, BlockPos position) {
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
        return ((IMixinWorld)world).getWorldImpl();
    }

    public WorldImpl getWorldImpl() {
        return (WorldImpl) getWorld();
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
        setData(data, applyPhysics ? 3 : 2);
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

    public boolean setTypeAndData(final net.minecraft.block.BlockState blockData, final boolean applyPhysics) {
        if (!blockData.isAir() && blockData.getBlock() instanceof BlockWithEntity && blockData.getBlock() != getNMSBlock()) {
            if (world instanceof net.minecraft.world.World)
                ((net.minecraft.world.World) world).removeBlockEntity(position);
            else world.setBlockState(position, Blocks.AIR.getDefaultState(), 0);
        }

        if (applyPhysics)
            return world.setBlockState(position, blockData, 3);
        else {
            net.minecraft.block.BlockState old = world.getBlockState(position);
            boolean success = world.setBlockState(position, blockData, 2 | 16 | 1024);
            if (success)
                world.toServerWorld().updateListeners(position, old, blockData, 3);
            return success;
        }
    }

    @Override
    public Material getType() {
        return CraftMagicNumbers.getMaterial(world.getBlockState(position).getBlock());
    }

    @Override
    public byte getLightLevel() {
        return (byte) world.toServerWorld().getLightLevel(position);
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

        for (BlockFace face : values)
            if ((this.getX() + face.getModX() == block.getX()) && (this.getY() + face.getModY() == block.getY()) && (this.getZ() + face.getModZ() == block.getZ()))
                return face;

        return null;
    }

    @Override
    public String toString() {
        return "CraftBlock{pos=" + position + ",type=" + getType() + ",data=" + getNMS() + ",fluid=" + world.getFluidState(position) + '}';
    }

    public static BlockFace notchToBlockFace(Direction notch) {
        if (notch == null) return BlockFace.SELF;
        return BlockFace.valueOf(notch.name());
    }

    public static Direction blockFaceToNotch(BlockFace face) {
        return Direction.valueOf(face.name());
    }

    @SuppressWarnings("unchecked")
    @Override
    public org.bukkit.block.BlockState getState() {
        Material material = getType();

        switch (material) {
            case ACACIA_SIGN:
            case ACACIA_WALL_SIGN:
            case BIRCH_SIGN:
            case BIRCH_WALL_SIGN:
            case CRIMSON_SIGN:
            case CRIMSON_WALL_SIGN:
            case DARK_OAK_SIGN:
            case DARK_OAK_WALL_SIGN:
            case JUNGLE_SIGN:
            case JUNGLE_WALL_SIGN:
            case OAK_SIGN:
            case OAK_WALL_SIGN:
            case SPRUCE_SIGN:
            case SPRUCE_WALL_SIGN:
            case WARPED_SIGN:
            case WARPED_WALL_SIGN:
                return new CardboardSign(this);
            case CHEST:
            case TRAPPED_CHEST:
                return new CardboardChest(this);
            case FURNACE:
                return new CardboardFurnaceFurnace(this);
            case DISPENSER:
                return new CardboardDispenser(this);
            case DROPPER:
                return new CardboardDropper(this);
            case END_GATEWAY:
                return new CardboardEndGateway(this);
            case HOPPER:
                return new CardboardHopper(this);
            case SPAWNER:
                return new CardboardMobspawner(this);
            case JUKEBOX:
                return new CardboardJukebox(this);
            case BREWING_STAND:
                return new CardboardBrewingStand(this);
            case CREEPER_HEAD:
            case CREEPER_WALL_HEAD:
            case DRAGON_HEAD:
            case DRAGON_WALL_HEAD:
            case PLAYER_HEAD:
            case PLAYER_WALL_HEAD:
            case SKELETON_SKULL:
            case SKELETON_WALL_SKULL:
            case WITHER_SKELETON_SKULL:
            case WITHER_SKELETON_WALL_SKULL:
            case ZOMBIE_HEAD:
            case ZOMBIE_WALL_HEAD:
                return new CardboardSkull(this);
            case COMMAND_BLOCK:
            case CHAIN_COMMAND_BLOCK:
            case REPEATING_COMMAND_BLOCK:
                return new CardboardCommandBlock(this);
            case BEACON:
                return new CardboardBeacon(this);
            case BLACK_BANNER:
            case BLACK_WALL_BANNER:
            case BLUE_BANNER:
            case BLUE_WALL_BANNER:
            case BROWN_BANNER:
            case BROWN_WALL_BANNER:
            case CYAN_BANNER:
            case CYAN_WALL_BANNER:
            case GRAY_BANNER:
            case GRAY_WALL_BANNER:
            case GREEN_BANNER:
            case GREEN_WALL_BANNER:
            case LIGHT_BLUE_BANNER:
            case LIGHT_BLUE_WALL_BANNER:
            case LIGHT_GRAY_BANNER:
            case LIGHT_GRAY_WALL_BANNER:
            case LIME_BANNER:
            case LIME_WALL_BANNER:
            case MAGENTA_BANNER:
            case MAGENTA_WALL_BANNER:
            case ORANGE_BANNER:
            case ORANGE_WALL_BANNER:
            case PINK_BANNER:
            case PINK_WALL_BANNER:
            case PURPLE_BANNER:
            case PURPLE_WALL_BANNER:
            case RED_BANNER:
            case RED_WALL_BANNER:
            case WHITE_BANNER:
            case WHITE_WALL_BANNER:
            case YELLOW_BANNER:
            case YELLOW_WALL_BANNER:
                return new CardboardBanner(this);
            case STRUCTURE_BLOCK:
                return new CardboardStructureBlock(this);
            case SHULKER_BOX:
            case WHITE_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case BLACK_SHULKER_BOX:
                return new CardboardShulkerBox(this);
            case ENCHANTING_TABLE:
                return new CardboardEnchantingTable(this);
            case ENDER_CHEST:
                return new CardboardEnderchest(this);
            case DAYLIGHT_DETECTOR:
                return new CardboardDaylightDetector(this);
            case COMPARATOR:
                return new CardboardComparator(this);
            case BLACK_BED:
            case BLUE_BED:
            case BROWN_BED:
            case CYAN_BED:
            case GRAY_BED:
            case GREEN_BED:
            case LIGHT_BLUE_BED:
            case LIGHT_GRAY_BED:
            case LIME_BED:
            case MAGENTA_BED:
            case ORANGE_BED:
            case PINK_BED:
            case PURPLE_BED:
            case RED_BED:
            case WHITE_BED:
            case YELLOW_BED:
                return new CardboardBed(this);
            case CONDUIT:
                return new CardboardConduit(this);
            case BARREL:
                return new CardboardBarrel(this);
            case BELL:
                return new CardboardBell(this);
            case BLAST_FURNACE:
                return new CardboardBlastFurnace(this);
            case CAMPFIRE:
            case SOUL_CAMPFIRE:
                return new CardboardCampfire(this);
            case JIGSAW:
                return new CardboardJigsaw(this);
            case LECTERN:
                return new CardboardLectern(this);
            case SMOKER:
                return new CardboardSmoker(this);
            case BEEHIVE:
            case BEE_NEST:
                return new CardboardBeehive(this);
            default:
                BlockEntity tileEntity = world.getBlockEntity(position);
                if (tileEntity != null) {
                    return new CardboardBlockEntityState<BlockEntity>(this, (Class<BlockEntity>) tileEntity.getClass()); // block with unhandled BlockEntity:
                } else return new CraftBlockState(this); // Block without BlockEntity
        }
    }

    @Override
    public Biome getBiome() {
        return getWorld().getBiome(getX(), getY(), getZ());
    }

    @Override
    public void setBiome(Biome bio) {
        getWorld().setBiome(getX(), getY(), getZ(), bio);
    }

    public static Biome biomeBaseToBiome(net.minecraft.registry.Registry<net.minecraft.world.biome.Biome> registry, RegistryEntry<net.minecraft.world.biome.Biome> base) {
        return biomeBaseToBiome(registry, base.value());
    }

    public static Biome biomeBaseToBiome(Registry<net.minecraft.world.biome.Biome> registry, net.minecraft.world.biome.Biome biome) {
        if (biome == null)
            return null;
        return org.bukkit.Registry.BIOME.get(CraftNamespacedKey.fromMinecraft(registry.getKey(biome).get().getValue()));
    }

    public static net.minecraft.world.biome.Biome biomeToBiomeBase(net.minecraft.registry.Registry<net.minecraft.world.biome.Biome> registry, Biome bio) {
        return (null == bio) ? null : registry.get(CraftNamespacedKey.toMinecraft(bio.getKey()));
    }

    @Override
    public double getTemperature() {
        return 0;
        // TODO: 1.18.2
        //return world.getBiome(position).getTemperature(position);
    }

    @Override
    public double getHumidity() {
        return getWorld().getHumidity(getX(), getY(), getZ());
    }

    @Override
    public boolean isBlockPowered() {
        return world.toServerWorld().getReceivedRedstonePower(position) > 0;
    }

    @Override
    public boolean isBlockIndirectlyPowered() {
        return world.toServerWorld().isReceivingRedstonePower(position);
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
        return world.toServerWorld().isEmittingRedstonePower(position, blockFaceToNotch(face));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isBlockFaceIndirectlyPowered(BlockFace face) {
        int power = world.toServerWorld().getEmittedRedstonePower(position, blockFaceToNotch(face));

        Block relative = getRelative(face);
        if (relative.getType() == Material.REDSTONE_WIRE)
            return Math.max(power, relative.getData()) > 0;

        return power > 0;
    }

    @Override
    public int getBlockPower(BlockFace face) {
        int power = 0;
        net.minecraft.world.World world = this.world;
        int x = getX();
        int y = getY();
        int z = getZ();
        if ((face == BlockFace.DOWN || face == BlockFace.SELF) && world.isEmittingRedstonePower(new BlockPos(x, y - 1, z), Direction.DOWN)) power = getPower(power, world.getBlockState(new BlockPos(x, y - 1, z)));
        if ((face == BlockFace.UP || face == BlockFace.SELF) && world.isEmittingRedstonePower(new BlockPos(x, y + 1, z), Direction.UP)) power = getPower(power, world.getBlockState(new BlockPos(x, y + 1, z)));
        if ((face == BlockFace.EAST || face == BlockFace.SELF) && world.isEmittingRedstonePower(new BlockPos(x + 1, y, z), Direction.EAST)) power = getPower(power, world.getBlockState(new BlockPos(x + 1, y, z)));
        if ((face == BlockFace.WEST || face == BlockFace.SELF) && world.isEmittingRedstonePower(new BlockPos(x - 1, y, z), Direction.WEST)) power = getPower(power, world.getBlockState(new BlockPos(x - 1, y, z)));
        if ((face == BlockFace.NORTH || face == BlockFace.SELF) && world.isEmittingRedstonePower(new BlockPos(x, y, z - 1), Direction.NORTH)) power = getPower(power, world.getBlockState(new BlockPos(x, y, z - 1)));
        if ((face == BlockFace.SOUTH || face == BlockFace.SELF) && world.isEmittingRedstonePower(new BlockPos(x, y, z + 1), Direction.SOUTH)) power = getPower(power, world.getBlockState(new BlockPos(x, y, z + 1)));
        return power > 0 ? power : (face == BlockFace.SELF ? isBlockIndirectlyPowered() : isBlockFaceIndirectlyPowered(face)) ? 15 : 0;
    }

    private static int getPower(int i, net.minecraft.block.BlockState iblockdata) {
        if (!iblockdata.getBlock().equals(Blocks.REDSTONE_WIRE)) {
            return i;
        } else {
            int j = iblockdata.get(RedstoneWireBlock.POWER);
            return j > i ? j : i;
        }
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

    @SuppressWarnings("deprecation")
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
        // Order matters here, need to drop before setting to air so skulls can get their data
        net.minecraft.block.BlockState iblockdata = this.getNMS();
        net.minecraft.block.Block block = iblockdata.getBlock();
        net.minecraft.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        boolean result = false;

        // Modeled off EntityHuman#hasBlock
        if (block != Blocks.AIR && (item == null || !iblockdata.isToolRequired() || nmsItem.isSuitableFor(iblockdata))) {
            net.minecraft.block.Block.dropStacks(iblockdata, world, position, world.getBlockEntity(position), null, nmsItem);
            result = true;
        }

        return setTypeAndData(Blocks.AIR.getDefaultState(), true) && result;
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
        net.minecraft.block.BlockState iblockdata = getNMS();
        net.minecraft.item.ItemStack nms = CraftItemStack.asNMSCopy(item);

        // Modelled off EntityHuman#hasBlock
        if (item == null || !iblockdata.isToolRequired() || nms.isSuitableFor(iblockdata)) {
            return net.minecraft.block.Block.getDroppedStacks(iblockdata, world, position, world.getBlockEntity(position), entity == null ? null : ((CraftEntity) entity).getHandle(), nms)
                    .stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());
        } else return Collections.emptyList();
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
        return getNMS().getCollisionShape(world, position).isEmpty();
    }

    @Override
    public RayTraceResult rayTrace(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode) {
        // TODO auto-generated method stub
        return null;
    }

    @Override
    public BoundingBox getBoundingBox() {
        VoxelShape shape = getNMS().getOutlineShape(world, position);

        if (shape.isEmpty())
            return new BoundingBox();

        Box aabb = shape.getBoundingBox();
        return new BoundingBox(getX() + aabb.minX, getY() + aabb.minY, getZ() + aabb.minZ, getX() + aabb.maxX, getY() + aabb.maxY, getZ() + aabb.maxZ);
    }

    @Override
    public boolean applyBoneMeal(BlockFace face) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean breakNaturally(ItemStack arg0, boolean arg1) {
        return this.breakNaturally(arg0);
    }

    @Override
    public float getDestroySpeed(ItemStack arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public BlockSoundGroup getSoundGroup() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public org.bukkit.block.BlockState getState(boolean arg0) {
        return CraftBlockState.getBlockState(world, position);
    }

    @Override
    public String getTranslationKey() {
        return getNMS().getBlock().getTranslationKey();
    }

    @Override
    public boolean isBuildable() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isBurnable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isReplaceable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSolid() {
        return getBlockData().getMaterial().isSolid();
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isPreferredTool(@NotNull ItemStack arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isValidTool(@NotNull ItemStack arg0) {
        // TODO Auto-generated method stub
        return false;
    }
    
    // 1.17 API Start

    @Override
    public float getBreakSpeed(@NotNull Player arg0) {
        return getNMS().calcBlockBreakingDelta( ((PlayerImpl)arg0).getHandle() , world, position);
    }

    @Override
    public org.bukkit.util.VoxelShape getCollisionShape() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NotNull String translationKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean breakNaturally(boolean bl) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCollidable() {
        // TODO Auto-generated method stub
        return false;
    }
    
    // 1.18 api:

	@Override
	public boolean canPlace(@NotNull BlockData arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public @NotNull SoundGroup getBlockSoundGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull Biome getComputedBiome() {
		// TODO Auto-generated method stub
		return null;
	}

	// 1.19.2 api
	
	@Override
	public void randomTick() {
		// TODO Auto-generated method stub
	}

	@Override
	public void tick() {
		// TODO Auto-generated method stub
		
	}

	public WorldImpl getCraftWorld() {
		// TODO Auto-generated method stub
		return this.getWorldImpl();
	}

    //

}