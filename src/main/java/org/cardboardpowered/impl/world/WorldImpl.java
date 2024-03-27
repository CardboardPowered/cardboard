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
package org.cardboardpowered.impl.world;

//<<<<<<< HEAD
//=======
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.bukkit.BlockChangeDelegate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Difficulty;
import org.bukkit.Effect;
import org.bukkit.FeatureFlag;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameEvent;
import org.bukkit.GameRule;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Raid;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.StructureType;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.DragonBattle;
import org.bukkit.craftbukkit.CraftFeatureFlag;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.SpawnChangeEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataStoreBase;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Consumer;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.StructureSearchResult;
import org.bukkit.util.Vector;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.util.CardboardFluidRaytraceMode;
import org.cardboardpowered.impl.util.CardboardRayTraceResult;
import org.cardboardpowered.interfaces.IServerWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//>>>>>>> upstream/ver/1.20
import com.destroystokyo.paper.HeightmapType;
import com.google.common.base.Preconditions;
import com.javazilla.bukkitfabric.Utils;
import com.javazilla.bukkitfabric.impl.MetaDataStoreBase;
import com.javazilla.bukkitfabric.impl.MetadataStoreImpl;
import com.javazilla.bukkitfabric.interfaces.IMixinArrowEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinChunkHolder;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinThreadedAnvilChunkStorage;
import io.papermc.paper.world.MoonPhase;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import me.isaiah.common.cmixin.IMixinWorld;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.Blocks;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.entity.vehicle.SpawnerMinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.ServerWorldProperties;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.DragonBattle;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.SpawnChangeEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataStoreBase;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Consumer;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.cardboardpowered.impl.CardboardPotionUtil;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.util.CardboardFluidRaytraceMode;
import org.cardboardpowered.impl.util.CardboardRayTraceResult;
import org.cardboardpowered.interfaces.IServerWorld;
import org.cardboardpowered.interfaces.IWorldChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public class WorldImpl implements World {

	public static final int CUSTOM_DIMENSION_OFFSET = 10;
	private final MetaDataStoreBase<Block> blockMetadata = MetadataStoreImpl.newBlockMetadataStore(this);

	private ServerWorld nms;
	private String name;
	private WorldBorder worldBorder;
	private final List<BlockPopulator> populators = new ArrayList<BlockPopulator>();

	private static final Random rand = new Random();

	public WorldImpl(String name, ServerWorld world) {
		this.nms = world;
		this.name = name;
	}

	public WorldImpl(ServerWorld world) {
		this(((ServerWorldProperties) world.getLevelProperties()).getLevelName(), world);
	}

	@Override
	public Set<String> getListeningPluginChannels() {
		Set<String> result = new HashSet<String>();

		for(Player player : getPlayers())
			result.addAll(player.getListeningPluginChannels());

		return result;
	}

	@Override
	public void sendPluginMessage(Plugin plugin, String channel, byte[] message) {
		for(Player player : getPlayers())
			player.sendPluginMessage(plugin, channel, message);
	}

	@Override
	public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
		CraftServer.INSTANCE.getWorldMetadata().setMetadata(this, metadataKey, newMetadataValue);
	}

	@Override
	public List<MetadataValue> getMetadata(String metadataKey) {
		return CraftServer.INSTANCE.getWorldMetadata().getMetadata(this, metadataKey);
	}

	@Override
	public boolean hasMetadata(String metadataKey) {
		return CraftServer.INSTANCE.getWorldMetadata().hasMetadata(this, metadataKey);
	}

	@Override
	public void removeMetadata(String metadataKey, Plugin owningPlugin) {
		CraftServer.INSTANCE.getWorldMetadata().removeMetadata(this, metadataKey, owningPlugin);
	}

	@Override
	public boolean addPluginChunkTicket(int arg0, int arg1, Plugin arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canGenerateStructures() {
		return nms.getLevelProperties() instanceof LevelProperties prop
				&& prop.getGeneratorOptions().shouldGenerateStructures();
	}

	@Override
	public boolean createExplosion(double x, double y, double z, float power) {
		return createExplosion(x, y, z, power, false, true);
	}

	@Override
	public boolean createExplosion(double x, double y, double z, float power, boolean setFire) {
		return createExplosion(x, y, z, power, setFire, true);
	}

	@Override
	public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
		return createExplosion(x, y, z, power, setFire, breakBlocks, null);
	}

	@Override
	public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks, Entity source) {
		Explosion explosion = nms.createExplosion(source == null ? null : ((CraftEntity) source).getHandle(), x, y, z, power, setFire, breakBlocks ? net.minecraft.world.World.ExplosionSourceType.MOB : net.minecraft.world.World.ExplosionSourceType.NONE);
		return true; // TODO return wasCanceled
	}

	@Override
	public boolean createExplosion(Location loc, float power) {
		return createExplosion(loc, power, false);
	}

	@Override
	public boolean createExplosion(Location loc, float power, boolean setFire) {
		return createExplosion(loc, power, setFire, true);
	}

	@Override
	public boolean createExplosion(Location loc, float power, boolean setFire, boolean breakBlocks) {
		return createExplosion(loc, power, setFire, breakBlocks, null);
	}

	@Override
	public boolean createExplosion(Location loc, float power, boolean setFire, boolean breakBlocks, Entity source) {
		Preconditions.checkArgument(loc != null, "Location is null");
		Preconditions.checkArgument(this.equals(loc.getWorld()), "Location not in world");

		return createExplosion(loc.getX(), loc.getY(), loc.getZ(), power, setFire, breakBlocks, source);
	}

	@Override
	public Item dropItem(Location loc, ItemStack arg1) {
		ItemEntity entity = new ItemEntity(nms, loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(arg1));
		entity.pickupDelay = 10;
		nms.addEntity(entity);
		return (org.bukkit.entity.Item) (((IMixinEntity) entity).getBukkitEntity());
	}

	@Override
	public Item dropItemNaturally(Location loc, ItemStack arg1) {
		double xs = (nms.random.nextFloat() * 0.5F) + 0.25D;
		double ys = (nms.random.nextFloat() * 0.5F) + 0.25D;
		double zs = (nms.random.nextFloat() * 0.5F) + 0.25D;
		loc = loc.clone();
		loc.setX(loc.getX() + xs);
		loc.setY(loc.getY() + ys);
		loc.setZ(loc.getZ() + zs);
		return dropItem(loc, arg1);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public boolean generateTree(Location loc, TreeType type) {
		BlockPos pos = new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        /*ConfiguredFeature gen;
        switch (type) {
            case BIG_TREE:
                gen = ConfiguredFeatures.FANCY_OAK;
                break;
            case BIRCH:
                gen = ConfiguredFeatures.BIRCH;
                break;
            case REDWOOD:
                gen = ConfiguredFeatures.SPRUCE;
                break;
            case TALL_REDWOOD:
                gen = ConfiguredFeatures.TREES_GIANT_SPRUCE;
                break;
            case JUNGLE:
                gen = ConfiguredFeatures.JUNGLE_TREE;
                break;
            case SMALL_JUNGLE:
                gen = ConfiguredFeatures.JUNGLE_TREE_NO_VINE;
                break;
            case COCOA_TREE:
                gen = ConfiguredFeatures.JUNGLE_TREE;
                break;
            case JUNGLE_BUSH:
                gen = ConfiguredFeatures.JUNGLE_BUSH;
                break;
            case RED_MUSHROOM:
                gen = ConfiguredFeatures.HUGE_RED_MUSHROOM;
                break;
            case BROWN_MUSHROOM:
                gen = ConfiguredFeatures.HUGE_BROWN_MUSHROOM;
                break;
            case SWAMP:
                gen = ConfiguredFeatures.TREES_SWAMP;
                break;
            case ACACIA:
                gen = ConfiguredFeatures.ACACIA;
                break;
            case DARK_OAK:
                gen = ConfiguredFeatures.DARK_OAK;
                break;
            case MEGA_REDWOOD:
                gen = ConfiguredFeatures.MEGA_SPRUCE;
                break;
            case TALL_BIRCH:
                gen = ConfiguredFeatures.BIRCH_TALL;
                break;
            case CHORUS_PLANT:
                ChorusFlowerBlock.generate(nms, pos, rand, 8);
                return true;
            case CRIMSON_FUNGUS:
                gen = ConfiguredFeatures.CRIMSON_FUNGI;
                break;
            case WARPED_FUNGUS:
                gen = ConfiguredFeatures.WARPED_FUNGI;
                break;
            case TREE:
            default:
                gen = ConfiguredFeatures.OAK;
                break;
        }*/
		// TODO 1.18

		return false; // TODO 1.17ify: return gen.feature.generate(nms, nms.getChunkManager().getChunkGenerator(), rand, pos, gen.config);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public boolean generateTree(Location loc, TreeType arg1, BlockChangeDelegate arg2) {
		BlockPos pos = new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		ConfiguredFeature gen = null;
        /*switch (arg1) {
            case ACACIA:
                gen = ConfiguredFeatures.ACACIA;
                break;
            case BIG_TREE:
                gen = ConfiguredFeatures.FANCY_OAK;
                break;
            case BIRCH:
                gen = ConfiguredFeatures.BIRCH;
                break;
            case BROWN_MUSHROOM:
                gen = ConfiguredFeatures.BROWN_MUSHROOM_NORMAL;
                break;
            case CHORUS_PLANT:
                break;
            case COCOA_TREE:
                gen = ConfiguredFeatures.JUNGLE_TREE;
                break;
            case CRIMSON_FUNGUS:
                break;
            case DARK_OAK:
                gen = ConfiguredFeatures.DARK_OAK;
                break;
            case JUNGLE:
                gen = ConfiguredFeatures.JUNGLE_TREE;
                break;
            case JUNGLE_BUSH:
                gen = ConfiguredFeatures.JUNGLE_BUSH;
                break;
            case MEGA_REDWOOD:
                gen = ConfiguredFeatures.MEGA_SPRUCE;
                break;
            case REDWOOD:
                gen = ConfiguredFeatures.SPRUCE;
                break;
            case RED_MUSHROOM:
                break;
            case SMALL_JUNGLE:
                gen = ConfiguredFeatures.JUNGLE_TREE_NO_VINE;
                break;
            case SWAMP:
                break;
            case TALL_BIRCH:
                gen = ConfiguredFeatures.BIRCH_TALL;
                break;
            case TALL_REDWOOD:
                gen = ConfiguredFeatures.TREES_GIANT_SPRUCE;
                break;
            case TREE:
                gen = ConfiguredFeatures.OAK;
                break;
            case WARPED_FUNGUS:
                break;
            default:
                gen = ConfiguredFeatures.OAK;
                break;
            
        }*/
		// TODO 1.18
		// TODO 1.17ify gen.feature.generate(nms, nms.getChunkManager().getChunkGenerator(), rand, pos, gen.config);
		return false;
	}

	@Override
	public boolean getAllowAnimals() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean getAllowMonsters() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getAmbientSpawnLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getAnimalSpawnLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Biome getBiome(int arg0, int arg1) {
		return getBiome(arg0, 0, arg1);
	}

	@Override
	public Biome getBiome(int arg0, int arg1, int arg2) {
		try {
			return CraftBlock.biomeBaseToBiome(getHandle().getRegistryManager()
					.get(RegistryKeys.BIOME), nms.getBiomeForNoiseGen(arg0 >> 2, arg1 >> 2, arg2 >> 2).value());
		} catch(Exception e) {
			return CraftBlock.biomeBaseToBiome(getHandle().getRegistryManager().get(RegistryKeys.BIOME),
					(net.minecraft.world.biome.Biome) (Object) nms.getBiomeForNoiseGen(arg0 >> 2, arg1 >> 2, arg2 >> 2));
		}
	}

	@Override
	public Block getBlockAt(Location loc) {
		return getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	@Override
	public Block getBlockAt(int x, int y, int z) {
		return CraftBlock.at(nms, new BlockPos(x, y, z));
	}

	@Override
	public Chunk getChunkAt(Location arg0) {
		return getChunkAt(arg0.getBlockX() >> 4, arg0.getBlockZ() >> 4);
	}

	@Override
	public Chunk getChunkAt(Block arg0) {
		return getChunkAt(arg0.getX() >> 4, arg0.getZ() >> 4);
	}

	@Override
	public Chunk getChunkAt(int x, int z) {
		return ((IWorldChunk) nms.getChunkManager().getWorldChunk(x, z, true)).getBukkitChunk();
	}

	@Override
	public Difficulty getDifficulty() {
		return Difficulty.valueOf(nms.getDifficulty().getName().toUpperCase());
	}

	@Override
	public ChunkSnapshot getEmptyChunkSnapshot(int arg0, int arg1, boolean arg2, boolean arg3) {
		return CardboardChunk.getEmptyChunkSnapshot(arg0, arg1, this, arg2, arg3);
	}

	@Override
	public DragonBattle getEnderDragonBattle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Entity> getEntities() {
		List<Entity> list = new ArrayList<>();

		nms.iterateEntities().forEach(entity -> {
			Entity bukkitEntity = ((IMixinEntity) entity).getBukkitEntity();
			if(bukkitEntity != null && bukkitEntity.isValid())
				list.add(bukkitEntity);
		});

		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... arg0) {
		return (Collection<T>) getEntitiesByClasses(arg0);
	}

	@Override
	public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> arg0) {
		Collection<T> list = new ArrayList<>();

		for(Object entity : nms.iterateEntities()) {
			if(entity instanceof net.minecraft.entity.Entity) {
				Entity bukkitEntity = ((IMixinEntity) (net.minecraft.entity.Entity) entity).getBukkitEntity();

				if(bukkitEntity == null)
					continue;

				Class<?> bukkitClass = bukkitEntity.getClass();

				if(arg0.isAssignableFrom(bukkitClass) && bukkitEntity.isValid())
					list.add((T) bukkitEntity);
			}
		}

		return list;
	}

	@Override
	public Collection<Entity> getEntitiesByClasses(Class<?>... arg0) {
		Collection<Entity> list = new ArrayList<Entity>();

		for(Object entity : nms.iterateEntities()) {
			if(entity instanceof net.minecraft.entity.Entity) {
				Entity bukkitEntity = ((IMixinEntity) (net.minecraft.entity.Entity) entity).getBukkitEntity();

				if(bukkitEntity == null)
					continue;

				Class<?> bukkitClass = bukkitEntity.getClass();

				for(Class<?> clazz : arg0) {
					if(clazz.isAssignableFrom(bukkitClass)) {
						if(bukkitEntity.isValid())
							list.add(bukkitEntity);
						break;
					}
				}
			}
		}
		return list;
	}

	@Override
	public Environment getEnvironment() {
		Identifier id = nms.getDimension().effects();

		if(DimensionTypes.OVERWORLD_ID.equals(id))
			return Environment.NORMAL;
		else if(DimensionTypes.THE_NETHER_ID.equals(id))
			return Environment.NETHER;
		else if(DimensionTypes.THE_END_ID.equals(id))
			return Environment.THE_END;
		else
			return Environment.CUSTOM;
	}

	@Override
	public Collection<Chunk> getForceLoadedChunks() {
		Set<Chunk> chunks = new HashSet<>();

		for(long coord : nms.getForcedChunks())
			chunks.add(getChunkAt(ChunkPos.getPackedX(coord), ChunkPos.getPackedZ(coord)));

		return Collections.unmodifiableCollection(chunks);
	}

	@Override
	public long getFullTime() {
		return nms.getTimeOfDay();
	}

	@Override
	public <T> T getGameRuleDefault(GameRule<T> arg0) {
		return convert(arg0, getGameRuleDefinitions().get(arg0.getName()).createRule());
	}

	@Override
	public String getGameRuleValue(String arg0) {
		// In method contract for some reason
		if(arg0 == null)
			return null;

		GameRules.Rule<?> value = getHandle().getGameRules().get(getGameRulesNMS().get(arg0));
		return value != null ? value.toString() : "";
	}

	@Override
	public <T> T getGameRuleValue(GameRule<T> arg0) {
		return convert(arg0, getHandle().getGameRules().get(getGameRulesNMS().get(arg0.getName())));
	}

	private static Map<String, GameRules.Key<?>> gamerules;
	public static synchronized Map<String, GameRules.Key<?>> getGameRulesNMS() {
		if(gamerules != null) {
			return gamerules;
		}

		Map<String, GameRules.Key<?>> gamerules = new HashMap<>();
		GameRules.accept(new GameRules.Visitor() {
			@Override
			public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> gamerules_gamerulekey, GameRules.Type<T> gamerules_gameruledefinition) {
				gamerules.put(gamerules_gamerulekey.getName(), gamerules_gamerulekey);
			}
		});

		return WorldImpl.gamerules = gamerules;
	}

	private <T> T convert(GameRule<T> rule, GameRules.Rule<?> value) {
		if(value == null)
			return null;

		if(value instanceof GameRules.BooleanRule) {
			return rule.getType().cast(((GameRules.BooleanRule) value).get());
		} else if(value instanceof GameRules.IntRule) {
			return rule.getType().cast(value.getCommandResult());
		} else
			throw new IllegalArgumentException("Invalid GameRule type (" + value + ") for GameRule " + rule.getName());
	}

	private static Map<String, GameRules.Type<?>> gameruleDefinitions;
	public static synchronized Map<String, GameRules.Type<?>> getGameRuleDefinitions() {
		if(gameruleDefinitions != null)
			return gameruleDefinitions;

		Map<String, GameRules.Type<?>> gameruleDefinitions = new HashMap<>();
		GameRules.accept(new GameRules.Visitor() {
			@Override
			public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> gamerules_gamerulekey, GameRules.Type<T> gamerules_gameruledefinition) {
				gameruleDefinitions.put(gamerules_gamerulekey.getName(), gamerules_gameruledefinition);
			}
		});

		return WorldImpl.gameruleDefinitions = gameruleDefinitions;
	}

	@Override
	public String[] getGameRules() {
		return getGameRulesNMS().keySet().toArray(new String[getGameRulesNMS().size()]);
	}

	@Override
	public ChunkGenerator getGenerator() {
		return null;
	}

	@Override
	public Block getHighestBlockAt(Location arg0) {
		return getHighestBlockAt(arg0.getBlockX(), arg0.getBlockY());
	}

	@Override
	public Block getHighestBlockAt(int x, int z) {
		return getBlockAt(x, getHighestBlockYAt(x, z), z);
	}

	@Override
	public Block getHighestBlockAt(Location arg0, HeightMap arg1) {
		return getHighestBlockAt(arg0.getBlockX(), arg0.getBlockY());
	}

	@Override
	public Block getHighestBlockAt(int x, int z, org.bukkit.HeightMap heightMap) {
		return getBlockAt(x, getHighestBlockYAt(x, z, heightMap), z);
	}

	@Override
	public int getHighestBlockYAt(Location arg0) {
		return getHighestBlockYAt(arg0.getBlockX(), arg0.getBlockZ());
	}

	@Override
	public int getHighestBlockYAt(int arg0, int arg1) {
		return getHighestBlockYAt(arg0, arg1, HeightMap.MOTION_BLOCKING);
	}

	@Override
	public int getHighestBlockYAt(int x, int z, HeightMap map) {
		return nms.getTopY(
				switch(map) {
					case WORLD_SURFACE -> Type.WORLD_SURFACE;
					case OCEAN_FLOOR -> Type.OCEAN_FLOOR;
					case MOTION_BLOCKING -> Type.MOTION_BLOCKING;
					case WORLD_SURFACE_WG -> Type.WORLD_SURFACE_WG;
					case OCEAN_FLOOR_WG -> Type.OCEAN_FLOOR_WG;
					case MOTION_BLOCKING_NO_LEAVES -> Type.MOTION_BLOCKING_NO_LEAVES;
				},
				x, z
		);
	}

	@Override
	public int getHighestBlockYAt(Location loc, HeightMap heightMap) {
		return getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());
	}

	@Override
	public double getHumidity(int x, int z) {
		return getHumidity(x, 0, z);
	}

	@Override
	public double getHumidity(int x, int y, int z) {

		// TODO 1.19.4 add AW for
		// return nms.getBiomeForNoiseGen(x >> 2, y >> 2, z >> 2).value().weather.downfall();

		try {
			return 0; // nms.getBiomeForNoiseGen(x >> 2, y >> 2, z >> 2).value().getDownfall();
		} catch(Exception e) {
			// 1.18.1
			// return ((net.minecraft.world.biome.Biome) (Object) nms.getBiomeForNoiseGen(x >> 2, y >> 2, z >> 2) ).getDownfall();
		}
		return 0;
	}

	@Override
	public boolean getKeepSpawnInMemory() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<LivingEntity> getLivingEntities() {
		List<LivingEntity> list = new ArrayList<LivingEntity>();

		for(Object o : nms.iterateEntities()) {
			if(o instanceof net.minecraft.entity.Entity) {
				net.minecraft.entity.Entity mcEnt = (net.minecraft.entity.Entity) o;
				Entity bukkitEntity = ((IMixinEntity) mcEnt).getBukkitEntity();

				// Assuming that bukkitEntity isn't null
				if(bukkitEntity != null && bukkitEntity instanceof LivingEntity && bukkitEntity.isValid())
					list.add((LivingEntity) bukkitEntity);
			}
		}
		return list;
	}

	@SuppressWarnings("resource")
	@Override
	public Chunk[] getLoadedChunks() {
		Long2ObjectLinkedOpenHashMap<ChunkHolder> chunks = ((IMixinThreadedAnvilChunkStorage) (nms.getChunkManager().threadedAnvilChunkStorage)).getChunkHoldersBF();
		return chunks.values()
				.stream()
				.map(IMixinChunkHolder::getFullChunkNow)
				.filter(Objects::nonNull)
				.map(WorldImpl::getBukkitChunkForChunk)
				.toArray(Chunk[]::new);
	}

	private static Chunk getBukkitChunkForChunk(WorldChunk mc) {
		return ((IWorldChunk) mc).getBukkitChunk();
	}

	@Override
	public int getMaxHeight() {
		return nms.getHeight();
	}

	@Override
	public int getMonsterSpawnLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
		return this.getNearbyEntities(location, x, y, z, null);
	}

	@Override
	public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z, Predicate<Entity> filter) {
		BoundingBox aabb = BoundingBox.of(location, x, y, z);
		return this.getNearbyEntities(aabb, filter);
	}

	@Override
	public Collection<Entity> getNearbyEntities(BoundingBox boundingBox) {
		return this.getNearbyEntities(boundingBox, null);
	}

	@Override
	public Collection<Entity> getNearbyEntities(BoundingBox boundingBox, Predicate<Entity> filter) {
		Box bb = new Box(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
		List<net.minecraft.entity.Entity> entityList = nms.getOtherEntities((net.minecraft.entity.Entity) null, bb, null);
		List<Entity> bukkitEntityList = new ArrayList<org.bukkit.entity.Entity>(entityList.size());

		for(net.minecraft.entity.Entity entity : entityList) {
			Entity bukkitEntity = ((IMixinEntity) entity).getBukkitEntity();
			if(filter == null || filter.test(bukkitEntity))
				bukkitEntityList.add(bukkitEntity);
		}

		return bukkitEntityList;
	}

	@Override
	public boolean getPVP() {
		return nms.getServer().isPvpEnabled();
	}

	@Override
	public List<Player> getPlayers() {
		List<Player> list = new ArrayList<>(nms.getPlayers().size());

		for(ServerPlayerEntity player : nms.getPlayers())
			list.add((Player) ((IMixinServerEntityPlayer) player).getBukkitEntity());

		return list;
	}

	@Override
	public Map<Plugin, Collection<Chunk>> getPluginChunkTickets() {
		return Collections.emptyMap();
	}

	@Override
	public Collection<Plugin> getPluginChunkTickets(int arg0, int arg1) {
		return Collections.emptySet();
	}

	@Override
	public List<BlockPopulator> getPopulators() {
		return populators;
	}

	@Override
	public List<Raid> getRaids() {
		return Collections.emptyList();
	}

	@Override
	public int getSeaLevel() {
		return nms.getSeaLevel();
	}

	@Override
	public long getSeed() {
		return nms.getSeed();
	}

	@Override
	public Location getSpawnLocation() {
		BlockPos pos = nms.getSpawnPos();
		return new Location(this, pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public double getTemperature(int x, int z) {
		return getTemperature(x, 0, z);
	}

	@Override
	public double getTemperature(int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		IMixinWorld icommon = (IMixinWorld) nms;
		return icommon.I_get_biome_for_noise_gen(x >> 2, y >> 2, z >> 2).getTemperature(pos);
	}

	@Override
	public int getThunderDuration() {
		return nms.getLevelProperties() instanceof LevelProperties prop
				? prop.getThunderTime()
				: 0;
	}

	@Override
	public long getTicksPerAmbientSpawns() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTicksPerAnimalSpawns() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTicksPerMonsterSpawns() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTicksPerWaterSpawns() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTime() {
		return nms.getTime();
	}

	@Override
	public UUID getUID() {
		return Utils.getWorldUUID(getWorldFolder());
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && this.getClass() == obj.getClass() &&
				this.getName().equals(((WorldImpl) obj).getName());
	}

	@Override
	public int getWaterAnimalSpawnLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWeatherDuration() {
		return nms.getLevelProperties() instanceof LevelProperties prop
				? prop.getRainTime()
				: 0;
	}

	@Override
	public WorldBorder getWorldBorder() {
		if(this.worldBorder == null)
			this.worldBorder = new WorldBorderImpl(this);

		return this.worldBorder;
	}

	@Override
	public File getWorldFolder() {
		// FIXME BROKEN (check for DMM1 & DMM-1)
		return nms.getServer().getRunDirectory();
	}

	@Override
	public WorldType getWorldType() {
		return nms.isFlat() ? WorldType.FLAT : WorldType.NORMAL;
	}

	@Override
	public boolean hasStorm() {
		return nms.getLevelProperties().isRaining();
	}

	@Override
	public boolean isAutoSave() {
		return !nms.isSavingDisabled();
	}

	@Override
	public boolean isChunkForceLoaded(int arg0, int arg1) {
		return nms.getForcedChunks().contains(ChunkPos.toLong(arg0, arg1));
	}

	@Override
	public boolean isChunkGenerated(int x, int z) {
		try {
			return isChunkLoaded(x, z) || nms.getChunkManager().threadedAnvilChunkStorage.getNbt(new ChunkPos(x, z)) != null;
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public boolean isChunkInUse(int arg0, int arg1) {
		return isChunkLoaded(arg0, arg1);
	}

	@Override
	public boolean isChunkLoaded(Chunk arg0) {
		return isChunkLoaded(arg0.getX(), arg0.getZ());
	}

	@Override
	public boolean isChunkLoaded(int x, int z) {
		return (null != nms.getChunkManager().getWorldChunk(x, z, false));
	}

	@Override
	public boolean isGameRule(String arg0) {
		return getGameRulesNMS().containsKey(arg0);
	}

	@Override
	public boolean isHardcore() {
		return nms.getLevelProperties().isHardcore();
	}

	@Override
	public boolean isThundering() {
		return nms.getLevelProperties().isThundering();
	}

	@Override
	public void loadChunk(Chunk arg0) {
		loadChunk(arg0.getX(), arg0.getZ());
	}

	@Override
	public void loadChunk(int arg0, int arg1) {
		loadChunk(arg0, arg1, true);
	}

	@Override
	public boolean loadChunk(int x, int z, boolean generate) {
		net.minecraft.world.chunk.Chunk chunk = nms.getChunkManager()
				.getChunk(x, z, generate ? ChunkStatus.FULL : ChunkStatus.EMPTY, true);

		if(chunk instanceof WrapperProtoChunk)
			chunk = nms.getChunkManager().getChunk(x, z, ChunkStatus.FULL, true);

		if(chunk instanceof net.minecraft.world.chunk.WorldChunk) {
			nms.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(x, z), 1, Unit.INSTANCE);
			return true;
		}

		return false;
	}

	@Override
	public Raid locateNearestRaid(Location arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Location locateNearestStructure(Location origin, StructureType structureType, int radius, boolean findUnexplored) {
		// BlockPos originPos = new BlockPos(origin.getX(), origin.getY(), origin.getZ());
		// FIXME: 1.18.2
		return null;
		// BlockPos nearest = this.getHandle().getChunkManager().getChunkGenerator().locateStructure(this.getHandle(), StructureFeature..STRUCTURES.get(structureType.getName()), originPos, radius, findUnexplored);
		// return (nearest == null) ? null : new Location(this, nearest.getX(), nearest.getY(), nearest.getZ());
	}

	public void playEffect(Player player, Effect effect, int data) {
		this.playEffect(player.getLocation(), effect, data, 0);
	}

	@Override
	public void playEffect(Location location, Effect effect, int data) {
		this.playEffect(location, effect, data, 64);
	}

	@Override
	public <T> void playEffect(Location loc, Effect effect, T data) {
		this.playEffect(loc, effect, data, 64);
	}

	@Override
	public <T> void playEffect(Location loc, Effect effect, T data, int radius) {
		if(data != null) {
			Validate.isTrue(effect.getData() != null && effect.getData()
					.isAssignableFrom(data.getClass()), "Wrong kind of data for this effect!");
		} else {
			Validate.isTrue(effect.getData() == null || effect == Effect.ELECTRIC_SPARK, "Wrong kind of data for this effect!");
		}
		int datavalue = 0;// CraftEffect.getDataValue(effect, data);
		this.playEffect(loc, effect, datavalue, radius);
	}

	@Override
	public void playEffect(Location location, Effect effect, int data, int radius) {
		Validate.notNull(location, "Location cannot be null");
		Validate.notNull(effect, "Effect cannot be null");
		Validate.notNull(location.getWorld(), "World cannot be null");
		int packetData = effect.getId();
		WorldEventS2CPacket packet = new WorldEventS2CPacket(packetData, new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), data, false);
		radius *= radius;
		for(Player player : this.getPlayers()) {
			if(((PlayerImpl) player).getHandle().networkHandler == null || !location.getWorld()
					.equals(player.getWorld()) || (int) player.getLocation()
					.distanceSquared(location) > radius) continue;
			((PlayerImpl) player).getHandle().networkHandler.sendPacket(packet);
		}
	}


	@Override
	public void playSound(Location loc, Sound sound, float volume, float pitch) {
		playSound(loc, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
	}

	@Override
	public void playSound(Location loc, String sound, float volume, float pitch) {
		playSound(loc, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
	}

	@Override
	public void playSound(Location loc, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {
		if(loc == null || sound == null || category == null) return;

		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();

		getHandle().playSound(null, x, y, z, CraftSound.getSoundEffect(CraftSound.getSound(sound)), net.minecraft.sound.SoundCategory.valueOf(category.name()), volume, pitch);
	}

	@Override
	public void playSound(Location loc, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {
		// TODO: 1.19
    	
    	/*if (loc == null || sound == null || category == null) return;

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        PlaySoundIdS2CPacket packet = new PlaySoundIdS2CPacket(new Identifier(sound), net.minecraft.sound.SoundCategory.valueOf(category.name()), new Vec3d(x, y, z), volume, pitch);
        nms.getServer().getPlayerManager().sendToAround(null, x, y, z, volume > 1.0F ? 16.0F * volume : 16.0D, nms.getRegistryKey(), packet);
    */
	}

	@Override
	public RayTraceResult rayTrace(Location start, Vector direction, double maxDistance, FluidCollisionMode mode, boolean ignorePassableBlocks, double raySize, Predicate<Entity> filter) {
		RayTraceResult blockHit = this.rayTraceBlocks(start, direction, maxDistance, mode, ignorePassableBlocks);
		Vector startVec = null;
		double blockHitDistance = maxDistance;

		if(blockHit != null) {
			startVec = start.toVector();
			blockHitDistance = startVec.distance(blockHit.getHitPosition());
		}

		RayTraceResult entityHit = this.rayTraceEntities(start, direction, blockHitDistance, raySize, filter);
		if(blockHit == null) return entityHit;
		if(entityHit == null) return blockHit;

		double entityHitDistanceSquared = startVec.distanceSquared(entityHit.getHitPosition());
		return (entityHitDistanceSquared < (blockHitDistance * blockHitDistance)) ? entityHit : blockHit;
	}

	@Override
	public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance) {
		return this.rayTraceBlocks(start, direction, maxDistance, FluidCollisionMode.NEVER, false);
	}

	@Override
	public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode) {
		return this.rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode, false);
	}

	@Override
	public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance, FluidCollisionMode mode, boolean ignorePassableBlocks) {
		Validate.notNull(start, "Start location equals null");
		Validate.isTrue(this.equals(start.getWorld()), "Start location a different world");
		start.checkFinite();

		Validate.notNull(direction, "Direction equals null");
		direction.checkFinite();

		Validate.isTrue(direction.lengthSquared() > 0, "Direction's magnitude is 0");
		Validate.notNull(mode, "mode equals null");

		if(maxDistance < 0.0D) return null;

		Vector dir = direction.clone().normalize().multiply(maxDistance);
		Vec3d startPos = new Vec3d(start.getX(), start.getY(), start.getZ());
		Vec3d endPos = new Vec3d(start.getX() + dir.getX(), start.getY() + dir.getY(), start.getZ() + dir.getZ());
		HitResult nmsHitResult = this.getHandle().raycast(new RaycastContext(startPos, endPos, ignorePassableBlocks ?
				RaycastContext.ShapeType.COLLIDER : RaycastContext.ShapeType.OUTLINE, CardboardFluidRaytraceMode.toMc(mode), (net.minecraft.entity.Entity) null));

		return CardboardRayTraceResult.fromNMS(this, nmsHitResult);
	}

	@Override
	public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance) {
		return this.rayTraceEntities(start, direction, maxDistance, null);
	}

	@Override
	public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, double raySize) {
		return this.rayTraceEntities(start, direction, maxDistance, raySize, null);
	}

	@Override
	public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, Predicate<Entity> filter) {
		return this.rayTraceEntities(start, direction, maxDistance, 0.0, filter);
	}

	@Override
	public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, double raySize, Predicate<Entity> filter) {
		Validate.notNull(start, "Start location is null!");
		Validate.isTrue(this.equals(start.getWorld()), "Start location is from different world!");
		start.checkFinite();
		Validate.notNull(direction, "Direction is null!");
		direction.checkFinite();
		Validate.isTrue(direction.lengthSquared() > 0.0, "Direction's magnitude is 0!");
		if(maxDistance < 0.0) {
			return null;
		}
		Vector startPos = start.toVector();
		Vector dir = direction.clone().normalize().multiply(maxDistance);
		BoundingBox aabb = BoundingBox.of(startPos, startPos).expandDirectional(dir).expand(raySize);
		Collection<Entity> entities = this.getNearbyEntities(aabb, filter);
		Entity nearestHitEntity = null;
		RayTraceResult nearestHitResult = null;
		double nearestDistanceSq = Double.MAX_VALUE;
		for(Entity entity : entities) {
			double distanceSq;
			BoundingBox boundingBox = entity.getBoundingBox().expand(raySize);
			RayTraceResult hitResult = boundingBox.rayTrace(startPos, direction, maxDistance);
			if(hitResult == null || !((distanceSq = startPos.distanceSquared(hitResult.getHitPosition())) < nearestDistanceSq))
				continue;
			nearestHitEntity = entity;
			nearestHitResult = hitResult;
			nearestDistanceSq = distanceSq;
		}
		return nearestHitEntity == null ? null : new RayTraceResult(nearestHitResult.getHitPosition(), nearestHitEntity, nearestHitResult.getHitBlockFace());
	}

	@Override
	public boolean refreshChunk(int x, int z) {
		if(!this.isChunkLoaded(x, z)) return false;

		int px = x << 4;
		int pz = z << 4;

		int height = this.getMaxHeight() / 16;
		for(int idx = 0; idx < 64; idx++)
			this.nms.updateListeners(new BlockPos(px + (idx / height), ((idx % height) * 16), pz), Blocks.AIR.getDefaultState(), Blocks.STONE.getDefaultState(), 3);
		this.nms.updateListeners(new BlockPos(px + 15, (height * 16) - 1, pz + 15), Blocks.AIR.getDefaultState(), Blocks.STONE.getDefaultState(), 3);

		return true;
	}

	@Override
	public boolean regenerateChunk(int arg0, int arg1) {
		throw new UnsupportedOperationException("Not supported in Spigot 1.17");
	}

	@Override
	public boolean removePluginChunkTicket(int arg0, int arg1, Plugin arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removePluginChunkTickets(Plugin arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void save() {
		boolean oldSave = nms.savingDisabled;
		nms.savingDisabled = false;
		nms.save(null, false, false);
		nms.savingDisabled = oldSave;
	}

	@Override
	public void setAmbientSpawnLimit(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setAnimalSpawnLimit(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setAutoSave(boolean arg0) {
		nms.savingDisabled = !arg0;
	}

	@Override
	public void setBiome(int arg0, int arg1, Biome arg2) {
		for(int y = 0; y < getMaxHeight(); y++)
			setBiome(arg0, y, arg1, arg2);
	}

	@Override
	public void setBiome(int x, int y, int z, Biome bio) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setChunkForceLoaded(int arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDifficulty(Difficulty diff) {
		// FIXME BROKEN
		// nms.getLevelProperties().setDifficulty(net.minecraft.world.Difficulty.byOrdinal(diff.ordinal()));
	}

	@Override
	public void setFullTime(long time) {
		TimeSkipEvent event = new TimeSkipEvent(this, TimeSkipEvent.SkipReason.CUSTOM, time - nms.getTimeOfDay());
		CraftServer.INSTANCE.getPluginManager().callEvent(event);
		if(event.isCancelled())
			return;

		nms.setTimeOfDay(nms.getTimeOfDay() + event.getSkipAmount());

		for(Player p : getPlayers()) {
			PlayerImpl cp = (PlayerImpl) p;
			if(cp.getHandle().networkHandler == null) continue;

			cp.getHandle().networkHandler.sendPacket(new WorldTimeUpdateS2CPacket(cp.getHandle()
					.getWorld()
					.getTime(), cp.getHandle().getWorld().getTime(), cp.getHandle()
					.getWorld()
					.getGameRules()
					.getBoolean(GameRules.DO_DAYLIGHT_CYCLE)));
		}
	}

	@Override
	public <T> boolean setGameRule(GameRule<T> arg0, T arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setGameRuleValue(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHardcore(boolean arg0) {
		// FIXME BROKEN!!
		// nms.getLevelProperties().setHardcore(arg0);
	}

	@Override
	public void setKeepSpawnInMemory(boolean arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setMonsterSpawnLimit(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setPVP(boolean arg0) {
		nms.getServer().setPvpEnabled(arg0);
	}

	@Override
	public void setSpawnFlags(boolean arg0, boolean arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean setSpawnLocation(Location location) {
		return equals(location.getWorld()) ? setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ()) : false;
	}

	@Override
	public boolean setSpawnLocation(int x, int y, int z) {
		try {
			Location previousLocation = getSpawnLocation();
			nms.setSpawnPos(new BlockPos(x, y, z), 0);

			// Notify anyone who's listening.
			SpawnChangeEvent event = new SpawnChangeEvent(this, previousLocation);
			Bukkit.getPluginManager().callEvent(event);

			return true;
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public void setStorm(boolean arg0) {
		nms.getLevelProperties().setRaining(arg0);
	}

	@Override
	public void setThunderDuration(int arg0) {
		worldProperties().setThunderTime(arg0);
	}

	@Override
	public void setThundering(boolean arg0) {
		worldProperties().setThundering(arg0);
	}

	@Override
	public void setTicksPerAmbientSpawns(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setTicksPerAnimalSpawns(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setTicksPerMonsterSpawns(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setTicksPerWaterSpawns(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setTime(long arg0) {
		nms.setTimeOfDay(arg0);
	}

	@Override
	public void setWaterAnimalSpawnLimit(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setWeatherDuration(int arg0) {
		worldProperties().setRainTime(arg0);
	}

	private ServerWorldProperties worldProperties() {
		return ((IServerWorld) nms).cardboard_worldProperties();
	}

	@Override
	public <T extends Entity> T spawn(Location location, Class<T> clazz) throws IllegalArgumentException {
		return spawn(location, clazz, null, SpawnReason.CUSTOM);
	}

	@Override
	public <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<T> function) throws IllegalArgumentException {
		return spawn(location, clazz, function, SpawnReason.CUSTOM);
	}

	public <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<T> function, SpawnReason reason) throws IllegalArgumentException {
		net.minecraft.entity.Entity entity = createEntity(location, clazz);

		return addEntity(entity, reason, function);
	}

	public net.minecraft.entity.Entity createEntity(Location location, Class<? extends Entity> clazz) throws IllegalArgumentException {
		if(location == null || clazz == null)
			throw new IllegalArgumentException("Location or entity class cannot be null");

		net.minecraft.entity.Entity entity = null;

		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		float pitch = location.getPitch();
		float yaw = location.getYaw();

		if(Boat.class.isAssignableFrom(clazz)) {
			entity = new BoatEntity(nms, x, y, z);
			entity.refreshPositionAndAngles(x, y, z, yaw, pitch);
		} else if(FallingBlock.class.isAssignableFrom(clazz)) {
			// TODO 1.18.2
			// entity = new FallingBlockEntity(nms, x, y, z, nms.getBlockState(new BlockPos(x, y, z)));
		} else if(Projectile.class.isAssignableFrom(clazz)) {
			if(Snowball.class.isAssignableFrom(clazz)) {
				entity = new SnowballEntity(nms, x, y, z);
			} else if(Egg.class.isAssignableFrom(clazz)) {
				entity = new EggEntity(nms, x, y, z);
			} else if(AbstractArrow.class.isAssignableFrom(clazz)) {
				if(TippedArrow.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.ARROW.create(nms);
					// TODO set type
				} else if(SpectralArrow.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.SPECTRAL_ARROW.create(nms);
				} else if(Trident.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.TRIDENT.create(nms);
				} else {
					entity = net.minecraft.entity.EntityType.ARROW.create(nms);
				}
				entity.refreshPositionAndAngles(x, y, z, 0, 0);
			} else if(ThrownExpBottle.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.EXPERIENCE_BOTTLE.create(nms);
				entity.refreshPositionAndAngles(x, y, z, 0, 0);
			} else if(EnderPearl.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.ENDER_PEARL.create(nms);
				entity.refreshPositionAndAngles(x, y, z, 0, 0);
			} else if(ThrownPotion.class.isAssignableFrom(clazz)) {
				if(LingeringPotion.class.isAssignableFrom(clazz)) {
					entity = new PotionEntity(nms, x, y, z);
					((PotionEntity) entity).setItem(CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.LINGERING_POTION, 1)));
				} else {
					entity = new PotionEntity(nms, x, y, z);
					((PotionEntity) entity).setItem(CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.SPLASH_POTION, 1)));
				}
			} else if(Fireball.class.isAssignableFrom(clazz)) {
				// TODO Fireball
			} else if(ShulkerBullet.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.SHULKER_BULLET.create(nms);
				entity.refreshPositionAndAngles(x, y, z, yaw, pitch);
			} else if(LlamaSpit.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.LLAMA_SPIT.create(nms);
				entity.refreshPositionAndAngles(x, y, z, yaw, pitch);
			} else if(Firework.class.isAssignableFrom(clazz)) {
				entity = new FireworkRocketEntity(nms, x, y, z, net.minecraft.item.ItemStack.EMPTY);
			}
		} else if(Minecart.class.isAssignableFrom(clazz)) {
			if(PoweredMinecart.class.isAssignableFrom(clazz)) {
				entity = new FurnaceMinecartEntity(nms, x, y, z);
			} else if(StorageMinecart.class.isAssignableFrom(clazz)) {
				entity = new ChestMinecartEntity(nms, x, y, z);
			} else if(ExplosiveMinecart.class.isAssignableFrom(clazz)) {
				entity = new TntMinecartEntity(nms, x, y, z);
			} else if(HopperMinecart.class.isAssignableFrom(clazz)) {
				entity = new HopperMinecartEntity(nms, x, y, z);
			} else if(SpawnerMinecart.class.isAssignableFrom(clazz)) {
				entity = new SpawnerMinecartEntity(nms, x, y, z);
			} else if(CommandMinecart.class.isAssignableFrom(clazz)) {
				entity = new CommandBlockMinecartEntity(nms, x, y, z);
			} else entity = new MinecartEntity(nms, x, y, z);
		} else if(EnderSignal.class.isAssignableFrom(clazz)) {
			// TODO EnderSignal
		} else if(EnderCrystal.class.isAssignableFrom(clazz)) {
			entity = net.minecraft.entity.EntityType.END_CRYSTAL.create(nms);
			entity.refreshPositionAndAngles(x, y, z, 0, 0);
		} else if(LivingEntity.class.isAssignableFrom(clazz)) {
			if(Chicken.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.CHICKEN.create(nms);
			} else if(Cow.class.isAssignableFrom(clazz)) {
				if(MushroomCow.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.MOOSHROOM.create(nms);
				} else {
					entity = net.minecraft.entity.EntityType.COW.create(nms);
				}
			} else if(Golem.class.isAssignableFrom(clazz)) {
				if(Snowman.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.SNOW_GOLEM.create(nms);
				} else if(IronGolem.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.IRON_GOLEM.create(nms);
				} else if(Shulker.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.SHULKER.create(nms);
				}
			} else if(Creeper.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.CREEPER.create(nms);
			} else if(Ghast.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.GHAST.create(nms);
			} else if(Pig.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.PIG.create(nms);
			} else if(Player.class.isAssignableFrom(clazz)) {
				// need a net server handler for this one
			} else if(Sheep.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.SHEEP.create(nms);
			} else if(AbstractHorse.class.isAssignableFrom(clazz)) {
				if(ChestedHorse.class.isAssignableFrom(clazz)) {
					if(Donkey.class.isAssignableFrom(clazz)) {
						entity = net.minecraft.entity.EntityType.DONKEY.create(nms);
					} else if(Mule.class.isAssignableFrom(clazz)) {
						entity = net.minecraft.entity.EntityType.MULE.create(nms);
					} else if(Llama.class.isAssignableFrom(clazz)) {
						entity = TraderLlama.class.isAssignableFrom(clazz) ?
								net.minecraft.entity.EntityType.TRADER_LLAMA.create(nms) : net.minecraft.entity.EntityType.LLAMA.create(nms);
					}
				} else if(SkeletonHorse.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.SKELETON_HORSE.create(nms);
				} else if(ZombieHorse.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.ZOMBIE_HORSE.create(nms);
				} else entity = net.minecraft.entity.EntityType.HORSE.create(nms);
			} else if(Skeleton.class.isAssignableFrom(clazz)) {
				if(Stray.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.STRAY.create(nms);
				} else if(WitherSkeleton.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.WITHER_SKELETON.create(nms);
				} else {
					entity = net.minecraft.entity.EntityType.SKELETON.create(nms);
				}
			} else if(Slime.class.isAssignableFrom(clazz)) {
				if(MagmaCube.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.MAGMA_CUBE.create(nms);
				} else {
					entity = net.minecraft.entity.EntityType.SLIME.create(nms);
				}
			} else if(Spider.class.isAssignableFrom(clazz)) {
				if(CaveSpider.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.CAVE_SPIDER.create(nms);
				} else {
					entity = net.minecraft.entity.EntityType.SPIDER.create(nms);
				}
			} else if(Squid.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.SQUID.create(nms);
			} else if(Tameable.class.isAssignableFrom(clazz)) {
				if(Wolf.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.WOLF.create(nms);
				} else if(Parrot.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.PARROT.create(nms);
				} else if(Cat.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.CAT.create(nms);
				}
			} else if(PigZombie.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.ZOMBIFIED_PIGLIN.create(nms);
			} else if(Zombie.class.isAssignableFrom(clazz)) {
				if(Husk.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.HUSK.create(nms);
				} else if(ZombieVillager.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.ZOMBIE_VILLAGER.create(nms);
				} else if(Drowned.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.DROWNED.create(nms);
				} else {
					entity = new ZombieEntity(nms);
				}
			} else if(Giant.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.GIANT.create(nms);
			} else if(Silverfish.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.SILVERFISH.create(nms);
			} else if(Enderman.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.ENDERMAN.create(nms);
			} else if(Blaze.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.BLAZE.create(nms);
			} else if(AbstractVillager.class.isAssignableFrom(clazz)) {
				if(Villager.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.VILLAGER.create(nms);
				} else if(WanderingTrader.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.WANDERING_TRADER.create(nms);
				}
			} else if(Witch.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.WITCH.create(nms);
			} else if(Wither.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.WITHER.create(nms);
			} else if(ComplexLivingEntity.class.isAssignableFrom(clazz)) {
				if(EnderDragon.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.ENDER_DRAGON.create(nms);
				}
			} else if(Ambient.class.isAssignableFrom(clazz)) {
				if(Bat.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.BAT.create(nms);
				}
			} else if(Rabbit.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.RABBIT.create(nms);
			} else if(Endermite.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.ENDERMITE.create(nms);
			} else if(Guardian.class.isAssignableFrom(clazz)) {
				if(ElderGuardian.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.ELDER_GUARDIAN.create(nms);
				} else {
					entity = net.minecraft.entity.EntityType.GUARDIAN.create(nms);
				}
			} else if(ArmorStand.class.isAssignableFrom(clazz)) {
				entity = new ArmorStandEntity(nms, x, y, z);
			} else if(PolarBear.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.POLAR_BEAR.create(nms);
			} else if(Vex.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.VEX.create(nms);
			} else if(Illager.class.isAssignableFrom(clazz)) {
				if(Spellcaster.class.isAssignableFrom(clazz)) {
					if(Evoker.class.isAssignableFrom(clazz)) {
						entity = net.minecraft.entity.EntityType.EVOKER.create(nms);
					} else if(Illusioner.class.isAssignableFrom(clazz)) {
						entity = net.minecraft.entity.EntityType.ILLUSIONER.create(nms);
					}
				} else if(Vindicator.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.VINDICATOR.create(nms);
				} else if(Pillager.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.PILLAGER.create(nms);
				}
			} else if(Turtle.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.TURTLE.create(nms);
			} else if(Phantom.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.PHANTOM.create(nms);
			} else if(Fish.class.isAssignableFrom(clazz)) {
				if(Cod.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.COD.create(nms);
				} else if(PufferFish.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.PUFFERFISH.create(nms);
				} else if(Salmon.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.SALMON.create(nms);
				} else if(TropicalFish.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.entity.EntityType.TROPICAL_FISH.create(nms);
				}
			} else if(Dolphin.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.DOLPHIN.create(nms);
			} else if(Ocelot.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.OCELOT.create(nms);
			} else if(Ravager.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.RAVAGER.create(nms);
			} else if(Panda.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.PANDA.create(nms);
			} else if(Fox.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.FOX.create(nms);
			} else if(Bee.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.BEE.create(nms);
			} else if(Hoglin.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.HOGLIN.create(nms);
			} else if(Piglin.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.PIGLIN.create(nms);
			} else if(PiglinBrute.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.PIGLIN_BRUTE.create(nms);
			} else if(Strider.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.entity.EntityType.STRIDER.create(nms);
			} else if(Zoglin.class.isAssignableFrom(clazz))
				entity = net.minecraft.entity.EntityType.ZOGLIN.create(nms);

			if(entity != null) {
				entity.updatePositionAndAngles(x, y, z, yaw, pitch);
				entity.setHeadYaw(yaw); // SPIGOT-3587
			}
		} else if(Hanging.class.isAssignableFrom(clazz)) {
			BlockFace face = BlockFace.SELF;

			int width = 16; // 1 full block, also painting smallest size.
			int height = 16; // 1 full block, also painting smallest size.

			if(ItemFrame.class.isAssignableFrom(clazz)) {
				width = 12;
				height = 12;
			} else if(LeashHitch.class.isAssignableFrom(clazz)) {
				width = 9;
				height = 9;
			}

			BlockFace[] faces = new BlockFace[] {BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN};
			final BlockPos pos = BlockPos.ofFloored(x, y, z);
			for(BlockFace dir : faces) {
				net.minecraft.block.BlockState nmsBlock = nms.getBlockState(pos.offset(CraftBlock.blockFaceToNotch(dir)));
				if(nmsBlock.isSolid() || AbstractRedstoneGateBlock.isRedstoneGate(nmsBlock)) {
					// TODO
				}
			}

			if(LeashHitch.class.isAssignableFrom(clazz)) {
				entity = new LeashKnotEntity(nms, BlockPos.ofFloored(x, y, z));
				// TODO 1.17ify entity.teleporting = true;
			} else {
				// No valid face found
				Preconditions.checkArgument(face != BlockFace.SELF, "Cannot spawn hanging entity for %s at %s (no free face)", clazz.getName(), location);

				Direction dir = CraftBlock.blockFaceToNotch(face).getOpposite();
				if(Painting.class.isAssignableFrom(clazz)) {
					// TODO: 1.19
					// entity = new PaintingEntity(nms, new BlockPos(x, y, z), dir);
				} else if(ItemFrame.class.isAssignableFrom(clazz)) {
					entity = new ItemFrameEntity(nms, BlockPos.ofFloored(x, y, z), dir);
				}
			}

			if(entity != null && !((AbstractDecorationEntity) entity).canStayAttached())
				throw new IllegalArgumentException("Cannot spawn hanging entity for " + clazz.getName() + " at " + location);
		} else if(TNTPrimed.class.isAssignableFrom(clazz)) {
			entity = new TntEntity(nms, x, y, z, null);
		} else if(ExperienceOrb.class.isAssignableFrom(clazz)) {
			entity = new ExperienceOrbEntity(nms, x, y, z, 0);
		} else if(LightningStrike.class.isAssignableFrom(clazz)) {
			entity = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(nms);
		} else if(AreaEffectCloud.class.isAssignableFrom(clazz)) {
			entity = new AreaEffectCloudEntity(nms, x, y, z);
		} else if(EvokerFangs.class.isAssignableFrom(clazz))
			entity = new EvokerFangsEntity(nms, x, y, z, (float) Math.toRadians(yaw), 0, null);

		if(entity != null)
			return entity;
		throw new IllegalArgumentException("Cannot spawn an entity for " + clazz.getName());
	}

	public <T extends Entity> T addEntity(net.minecraft.entity.Entity entity, SpawnReason reason) throws IllegalArgumentException {
		return addEntity(entity, reason, null);
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> T addEntity(net.minecraft.entity.Entity entity, SpawnReason reason, Consumer<T> function) throws IllegalArgumentException {
		Preconditions.checkArgument(entity != null, "Cannot spawn null entity");

		// if (entity instanceof MobEntity)
		//    ((MobEntity) entity).initialize(nms, getHandle().getLocalDifficulty(entity.getBlockPos()), net.minecraft.entity.SpawnReason.COMMAND, (EntityData) null, null);

		if(function != null)
			function.accept((T) ((IMixinEntity) entity).getBukkitEntity());

		nms.addEntity(entity); // TODO spawn reason
		return (T) ((IMixinEntity) entity).getBukkitEntity();
	}

	@Override
	public Arrow spawnArrow(Location loc, Vector velocity, float speed, float spread) {
		return spawnArrow(loc, velocity, speed, spread, Arrow.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractArrow> T spawnArrow(Location loc, Vector velocity, float speed, float spread, Class<T> clazz) {
		Validate.notNull(loc, "Cant spawn arrow with a null location");
		Validate.notNull(velocity, "Cant spawn arrow with a null velocity");
		Validate.notNull(clazz, "Cant spawn an arrow with no class");

		PersistentProjectileEntity arrow;
		if(TippedArrow.class.isAssignableFrom(clazz)) {
			arrow = net.minecraft.entity.EntityType.ARROW.create(nms);
			((IMixinArrowEntity) arrow).setType(CardboardPotionUtil.fromBukkit(new PotionData(PotionType.WATER, false, false)));
		} else if(SpectralArrow.class.isAssignableFrom(clazz)) {
			arrow = net.minecraft.entity.EntityType.SPECTRAL_ARROW.create(nms);
		} else if(Trident.class.isAssignableFrom(clazz)) {
			arrow = net.minecraft.entity.EntityType.TRIDENT.create(nms);
		} else arrow = net.minecraft.entity.EntityType.ARROW.create(nms);

		arrow.refreshPositionAndAngles(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		arrow.setVelocity(velocity.getX(), velocity.getY(), velocity.getZ(), speed, spread);
		nms.spawnEntity(arrow);
		return (T) ((IMixinEntity) arrow).getBukkitEntity();
	}

	@Override
	public Entity spawnEntity(Location loc, EntityType entityType) {
		return spawn(loc, entityType.getEntityClass());
	}

	@Override
	public FallingBlock spawnFallingBlock(Location location, MaterialData data) throws IllegalArgumentException {
		Validate.notNull(data, "MaterialData cannot be null");
		return spawnFallingBlock(location, data.getItemType(), data.getData());
	}

	@Override
	public FallingBlock spawnFallingBlock(Location location, BlockData data) throws IllegalArgumentException {
		Validate.notNull(location, "Location cannot be null");
		Validate.notNull(data, "Material cannot be null");

		// FallingBlockEntity entity = new FallingBlockEntity(nms, location.getX(), location.getY(), location.getZ(), ((CraftBlockData) data).getState());
		FallingBlockEntity entity = FallingBlockEntity.spawnFromBlock(nms, BlockPos.ofFloored(location.getX(), location.getY(), location.getZ()), ((CraftBlockData) data).getState());
		entity.timeFalling = 1;

		nms.addEntity(entity/*, SpawnReason.CUSTOM*/);
		return (FallingBlock) ((IMixinEntity) entity).getBukkitEntity();
	}

	@Override
	public FallingBlock spawnFallingBlock(Location location, org.bukkit.Material material, byte data) throws IllegalArgumentException {
		Validate.notNull(location, "Location cannot be null");
		Validate.notNull(material, "Material cannot be null");
		Validate.isTrue(material.isBlock(), "Material must be a block");

		// TODO 1.18.1 / 1.18.2
		FallingBlockEntity entity = FallingBlockEntity.spawnFromBlock(nms, BlockPos.ofFloored(location.getX(), location.getY(), location.getZ()), CraftMagicNumbers.getBlock(material)
				.getDefaultState());
		// FallingBlockEntity entity = new FallingBlockEntity(nms, location.getX(), location.getY(), location.getZ(), CraftMagicNumbers.getBlock(material).getDefaultState());
		entity.timeFalling = 1;

		nms.addEntity(entity/*, SpawnReason.CUSTOM*/);
		return (FallingBlock) ((IMixinEntity) entity).getBukkitEntity();
	}

	@Override
	public void spawnParticle(Particle particle, Location location, int count) {
		spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count);
	}

	@Override
	public void spawnParticle(Particle particle, double x, double y, double z, int count) {
		spawnParticle(particle, x, y, z, count, null);
	}

	@Override
	public <T> void spawnParticle(Particle particle, Location location, int count, T data) {
		spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, data);
	}

	@Override
	public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {
		spawnParticle(particle, x, y, z, count, 0, 0, 0, data);
	}

	@Override
	public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ) {
		spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ);
	}

	@Override
	public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
		spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, null);
	}

	@Override
	public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, T data) {
		spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, data);
	}

	@Override
	public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, T data) {
		spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, 1, data);
	}

	@Override
	public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {
		spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra);
	}

	@Override
	public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {
		spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, null);
	}

	@Override
	public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
		spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data);
	}

	@Override
	public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
		spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, data, false);
	}

	@Override
	public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {
		spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data, force);
	}

	@Override
	public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {
		if(data != null && !particle.getDataType().isInstance(data))
			throw new IllegalArgumentException("data should be " + particle.getDataType() + " got " + data.getClass());
		// TODO Bukkit4Fabric: method
		getHandle().addParticle(
				// null, // Sender
				CraftParticle.toNMS(particle, data), // Particle
				x, y, z, // Position
				(double) count,  // Count
				offsetX, offsetY//, offsetZ // Random offset
				// extra // Speed?
				// force
		);
	}

	@Override
	public LightningStrike strikeLightning(Location loc) {
		LightningEntity lightning = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(nms);
		lightning.refreshPositionAfterTeleport(loc.getX(), loc.getY(), loc.getZ());
		// nms.strikeLightning(lightning);
		return (LightningStrike) ((IMixinEntity) lightning).getBukkitEntity();
	}

	@Override
	public LightningStrike strikeLightningEffect(Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean unloadChunk(Chunk chunk) {
		return unloadChunk(chunk.getX(), chunk.getZ());
	}

	@Override
	public boolean unloadChunk(int x, int z) {
		return unloadChunk(x, z, true);
	}

	@Override
	public boolean unloadChunk(int x, int z, boolean save) {
		return unloadChunk0(x, z, save);
	}

	private boolean unloadChunk0(int x, int z, boolean save) {
		net.minecraft.world.chunk.WorldChunk chunk = nms.getChunk(x, z);

		// chunk.mustNotSave = !save;
		unloadChunkRequest(x, z);

		nms.getChunkManager().executeQueuedTasks();
		return !isChunkLoaded(x, z);
	}


	@Override
	public boolean unloadChunkRequest(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public ServerWorld getHandle() {
		return nms;
	}

	@Override
	public int getViewDistance() {
		// TODO Auto-generated method stub
		return 8;
	}

	public void setWaterAmbientSpawnLimit(int i) {
		// TODO Auto-generated method stub
	}

	@Override
	public Spigot spigot() {
		return new Spigot() {
			// TODO Auto-generated method stub
		};
	}

	public MetadataStoreBase<Block> getBlockMetadata() {
		return blockMetadata;
	}

	@Override
	public long getTicksPerWaterAmbientSpawns() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWaterAmbientSpawnLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTicksPerWaterAmbientSpawns(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean setSpawnLocation(int x, int y, int z, float angle) {
		try {
			Location previousLocation = getSpawnLocation();
			nms.setSpawnPos(new BlockPos(x, y, z), angle);

			SpawnChangeEvent event = new SpawnChangeEvent(this, previousLocation);
			CraftServer.INSTANCE.getPluginManager().callEvent(event);

			return true;
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public boolean createExplosion(Entity arg0, Location arg1, float arg2, boolean arg3, boolean arg4) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public CompletableFuture<Chunk> getChunkAtAsync(int arg0, int arg1, boolean arg2, boolean arg3) {
		Chunk c = this.getChunkAt(arg0, arg1);
		return CompletableFuture.completedFuture(c);
	}

	@Override
	public int getChunkCount() {
		return nms.getChunkManager().getTotalChunksLoadedCount();
	}

	@Override
	public int getClearWeatherDuration() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Entity getEntity(UUID arg0) {
		return ((IMixinEntity) nms.getEntity(arg0)).getBukkitEntity();
	}

	@Override
	public int getEntityCount() {
		return (int) nms.iterateEntities().spliterator().getExactSizeIfKnown();//.entitiesByUuid.size();
	}

	@Override
	public int getHighestBlockYAt(int arg0, int arg1, HeightmapType arg2) throws UnsupportedOperationException {
		return this.getHighestBlockYAt(arg0, arg1);
	}

	@Override
	public MoonPhase getMoonPhase() {
		return MoonPhase.getPhase(nms.getLunarTime());
	}

	@Override
	public int getNoTickViewDistance() {
		return 0;
	}

	@Override
	public int getPlayerCount() {
		return nms.getPlayers().size();
	}

	@Override
	public int getTickableTileEntityCount() {
		return 0; // TODO: 1.17ify return nms.tickingBlockEntities.size();
	}

	@Override
	public int getTileEntityCount() {
		return 0; // TODO: 1.17ify return nms.blockEntities.size();
	}

	@Override
	public boolean isClearWeather() {
		return !nms.isRaining();
	}

	@Override
	public boolean isDayTime() {
		return nms.isDay();
	}

	@Override
	public void setClearWeatherDuration(int arg0) {
		nms.setWeather(arg0, 0, false, false);
	}

	@Override
	public void setNoTickViewDistance(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setViewDistance(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public <T> void spawnParticle(Particle arg0, List<Player> arg1, Player arg2, double arg3, double arg4, double arg5,
	                              int arg6, double arg7, double arg8, double arg9, double arg10, T arg11, boolean arg12) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean doesBedWork() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doesRespawnAnchorWork() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public @NotNull Item dropItem(@NotNull Location arg0, @NotNull ItemStack arg1, @Nullable Consumer<Item> arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull Item dropItemNaturally(@NotNull Location arg0, @NotNull ItemStack arg1,
	                                       @Nullable Consumer<Item> arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getCoordinateScale() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getGameTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public @NotNull Collection<Material> getInfiniburn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull NamespacedKey getKey() {
		// TODO Auto-generated method stub
		return NamespacedKey.minecraft(this.getName());
	}

	@Override
	public int getMinHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasBedrockCeiling() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasRaids() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasSkylight() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFixedTime() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNatural() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPiglinSafe() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUltrawarm() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean lineOfSightExists(@NotNull Location arg0, @NotNull Location arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public @Nullable Location locateNearestBiome(@NotNull Location arg0, @NotNull Biome arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @Nullable Location locateNearestBiome(@NotNull Location arg0, @NotNull Biome arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean generateTree(@NotNull Location arg0, @NotNull Random arg1, @NotNull TreeType arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean generateTree(@NotNull Location arg0, @NotNull Random arg1, @NotNull TreeType arg2,
	                            @Nullable Consumer<BlockState> arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public @NotNull Biome getBiome(@NotNull Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull BlockData getBlockData(@NotNull Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull BlockData getBlockData(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull BlockState getBlockState(@NotNull Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull BlockState getBlockState(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull Material getType(@NotNull Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull Material getType(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBiome(@NotNull Location arg0, @NotNull Biome arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBlockData(@NotNull Location arg0, @NotNull BlockData arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBlockData(int arg0, int arg1, int arg2, @NotNull BlockData arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setType(@NotNull Location arg0, @NotNull Material arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setType(int arg0, int arg1, int arg2, @NotNull Material arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends Entity> @NotNull T spawn(@NotNull Location arg0, @NotNull Class<T> arg1, boolean arg2,
	                                           @Nullable Consumer<T> arg3) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull Entity spawnEntity(@NotNull Location arg0, @NotNull EntityType arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @Nullable Location findLightningRod(@NotNull Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @Nullable Location findLightningTarget(@NotNull Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @Nullable BiomeProvider getBiomeProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLogicalHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSendViewDistance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTicksPerWaterUndergroundCreatureSpawns() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWaterUndergroundCreatureSpawnLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasCeiling() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasSkyLight() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBedWorks() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRespawnAnchorWorks() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUltraWarm() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void sendGameEvent(@Nullable Entity arg0, @NotNull GameEvent arg1, @NotNull Vector arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setSendViewDistance(int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTicksPerWaterUndergroundCreatureSpawns(int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWaterUndergroundCreatureSpawnLimit(int i) {
		// TODO Auto-generated method stub

	}

	// 1.18.2 API:

	@Override
	public boolean generateTree(@NotNull Location arg0, @NotNull Random arg1, @NotNull TreeType arg2,
	                            @Nullable Predicate<BlockState> arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public @NotNull Biome getComputedBiome(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull BiomeProvider vanillaBiomeProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull PersistentDataContainer getPersistentDataContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSimulationDistance() {
		// TODO Auto-generated method stub
		return 8;
	}

	@Override
	public int getSpawnLimit(@NotNull SpawnCategory arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTicksPerSpawns(@NotNull SpawnCategory arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void playSound(@NotNull Entity arg0, @NotNull Sound arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void playSound(@NotNull Entity arg0, @NotNull Sound arg1, @NotNull SoundCategory arg2, float arg3,
	                      float arg4) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSimulationDistance(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSpawnLimit(@NotNull SpawnCategory arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTicksPerSpawns(@NotNull SpawnCategory arg0, int arg1) {
		// TODO Auto-generated method stub

	}
	
	// 1.19.2

	@Override
    public boolean hasCollisionsIn(@NotNull BoundingBox boundingBox) {
        Box aabb = new Box(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        return !this.getHandle().isSpaceEmpty(aabb);
    }

	@Override
	public @Nullable StructureSearchResult locateNearestStructure(@NotNull Location arg0,
			org.bukkit.generator.structure.@NotNull StructureType arg1, int arg2, boolean arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @Nullable StructureSearchResult locateNearestStructure(@NotNull Location arg0, @NotNull Structure arg1,
			int arg2, boolean arg3) {
		// TODO Auto-generated method stub
		return null;
	}
	
	// 1.19.4

	// @Override
    public org.bukkit.Chunk getChunkAt(int x2, int z2, boolean generate) {
        if (generate) {
            return this.getChunkAt(x2, z2);
        }
        return new CardboardChunk(this.getHandle(), x2, z2);
    }

	// @Override
	public void playSound(@NotNull Entity arg0, @NotNull String arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub
		
	}

	// @Override
	public void playSound(@NotNull Entity arg0, @NotNull String arg1, @NotNull SoundCategory arg2, float arg3,
			float arg4) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public @NotNull Set<FeatureFlag> getFeatureFlags() {
        return CraftFeatureFlag.getFromNMS(this.getHandle().getEnabledFeatures()).stream().map(FeatureFlag.class::cast).collect(Collectors.toUnmodifiableSet());
	}

}
