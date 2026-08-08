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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.LevelAccessor;
import org.apache.commons.lang.Validate;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.DragonBattle;
import org.bukkit.craftbukkit.*;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockType;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.generator.structure.CraftGeneratedStructure;
import org.bukkit.craftbukkit.generator.structure.CraftStructure;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftBiomeSearchResult;
import org.bukkit.craftbukkit.util.CraftDifficulty;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.SpawnChangeEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataStoreBase;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BiomeSearchResult;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Consumer;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.StructureSearchResult;
import org.bukkit.util.Vector;
import org.cardboardpowered.bridge.world.level.LevelBridge;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftRayTraceResult;
import org.cardboardpowered.bridge.server.level.ServerLevelBridge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import org.cardboardpowered.impl.MetadataStoreImpl;
import org.checkerframework.checker.index.qual.Positive;
import org.cardboardpowered.bridge.server.level.ChunkHolderBridge;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.cardboardpowered.bridge.server.level.ChunkMapBridge;
import com.mojang.datafixers.util.Pair;

import io.papermc.paper.block.fluid.FluidData;
import io.papermc.paper.entity.poi.PoiSearchResult;
import io.papermc.paper.entity.poi.PoiType;
import io.papermc.paper.entity.poi.PoiType.Occupancy;
import io.papermc.paper.math.Position;
import io.papermc.paper.raytracing.PositionedRayTraceConfigurationBuilder;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.world.MoonPhase;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import me.isaiah.common.cmixin.IMixinWorld;
import net.kyori.adventure.util.TriState;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import net.minecraft.world.entity.vehicle.minecart.MinecartSpawner;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.cardboardpowered.ChunkTicketBridge;
import org.cardboardpowered.bridge.world.level.chunk.LevelChunkBridge;

@SuppressWarnings("deprecation")
/**
 * Cardboard Bukkit Implementation of CraftWorld.
 */
public class CraftWorld extends CraftRegionAccessor implements World {

	public static final int CUSTOM_DIMENSION_OFFSET = 10;
	private final MetadataStoreBase<Block> blockMetadata = MetadataStoreImpl.newBlockMetadataStore(this);

	private ServerLevel world;
	private String name;
	private WorldBorder worldBorder;
	private final List<BlockPopulator> populators = new ArrayList<BlockPopulator>();

	private static final Random rand = new Random();

	public CraftWorld(String name, ServerLevel world) {
		this.world = world;
		this.name = name;
	}

	public CraftWorld(ServerLevel world) {
		this(((ServerLevelData) world.getLevelData()).getLevelName(), world);
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
		return true; // TODO
		/*
		return world.getLevelData() instanceof PrimaryLevelData prop
				&& prop.worldGenOptions().generateStructures();
				*/
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
		// Explosion explosion =
		world.explode(source == null ? null : ((CraftEntity) source).getHandle(), x, y, z, power, setFire, breakBlocks ? net.minecraft.world.level.Level.ExplosionInteraction.MOB : net.minecraft.world.level.Level.ExplosionInteraction.NONE);
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
		ItemEntity entity = new ItemEntity(world, loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(arg1));
		entity.pickupDelay = 10;
		world.addEntity(entity);
		return (org.bukkit.entity.Item) (((EntityBridge) entity).getBukkitEntity());
	}

	@Override
	public Item dropItemNaturally(Location loc, ItemStack arg1) {
		double xs = (world.getRandom().nextFloat() * 0.5F) + 0.25D;
		double ys = (world.getRandom().nextFloat() * 0.5F) + 0.25D;
		double zs = (world.getRandom().nextFloat() * 0.5F) + 0.25D;
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
	public Block getBlockAt(Location loc) {
		return getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	@Override
	public Block getBlockAt(int x, int y, int z) {
		return CraftBlock.at(world, new BlockPos(x, y, z));
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
		return ((LevelChunkBridge) world.getChunkSource().getChunk(x, z, true)).getBukkitChunk();
	}

	@Override
	public Difficulty getDifficulty() {
		return CraftDifficulty.toBukkit(this.getHandle().getDifficulty());
		// return Difficulty.valueOf(world.getDifficulty().getKey().toUpperCase());
	}

	@Override
	public ChunkSnapshot getEmptyChunkSnapshot(int arg0, int arg1, boolean arg2, boolean arg3) {
		return CraftChunk.getEmptyChunkSnapshot(arg0, arg1, this, arg2, arg3);
	}

	@Override
	public DragonBattle getEnderDragonBattle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Entity> getEntities() {
		List<Entity> list = new ArrayList<>();

		world.getAllEntities().forEach(entity -> {
			Entity bukkitEntity = ((EntityBridge) entity).getBukkitEntity();
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

		for(Object entity : world.getAllEntities()) {
			if(entity instanceof net.minecraft.world.entity.Entity) {
				Entity bukkitEntity = ((EntityBridge) (net.minecraft.world.entity.Entity) entity).getBukkitEntity();

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

		for(Object entity : world.getAllEntities()) {
			if(entity instanceof net.minecraft.world.entity.Entity) {
				Entity bukkitEntity = ((EntityBridge) (net.minecraft.world.entity.Entity) entity).getBukkitEntity();

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
		
		Holder<DimensionType> type = world.dimensionTypeRegistration();
		
		if (type.is(BuiltinDimensionTypes.OVERWORLD)) {
			return Environment.NORMAL;
		} else if (type.is(BuiltinDimensionTypes.NETHER)) {
			return Environment.NETHER;
		} else if (type.is(BuiltinDimensionTypes.END)) {
			return Environment.THE_END;
		}
		
		/*
		Identifier id = nms.getDimension().effects();

		if(DimensionTypes.OVERWORLD_ID.equals(id))
			return Environment.NORMAL;
		else if(DimensionTypes.THE_NETHER_ID.equals(id))
			return Environment.NETHER;
		else if(DimensionTypes.THE_END_ID.equals(id))
			return Environment.THE_END;
		else
		*/
			return Environment.CUSTOM;
	}

	@Override
	public Collection<Chunk> getForceLoadedChunks() {
		Set<Chunk> chunks = new HashSet<>();

		for(long coord : world.getForceLoadedChunks())
			chunks.add(getChunkAt(ChunkPos.getX(coord), ChunkPos.getZ(coord)));

		return Collections.unmodifiableCollection(chunks);
	}

	@Override
	public long getFullTime() {
		return world.getDefaultClockTime();
	}
	
	public static <T> T shimLegacyValue(T value, GameRule<?> gameRule) {
		return (T)(gameRule instanceof CraftGameRule.LegacyGameRuleWrapper legacyGameRuleWrapper
			? legacyGameRuleWrapper.getToLegacyFromModern().apply(value)
			: value);
	}

	@Override
	public <T> T getGameRuleDefault(@NotNull GameRule<T> rule) {
		Preconditions.checkArgument(rule != null, "GameRule cannot be null");
		T value = CraftGameRule.<T>bukkitToMinecraft(rule).defaultValue();
		return shimLegacyValue(value, rule);
	}

	@Override
	public String getGameRuleValue(String rule) {
		if (rule == null) {
			return null;
		} else {
			GameRule<?> bukkit = GameRule.getByName(rule);
			if (bukkit == null) {
				throw new IllegalArgumentException("Unknown gamerule: " + rule);
			} else {
				return this.getHandle().getGameRules().getAsString(CraftGameRule.bukkitToMinecraft(bukkit));
			}
		}
	}

	@Override
	public <T> T getGameRuleValue(@NotNull GameRule<T> rule) {
		Preconditions.checkArgument(rule != null, "GameRule cannot be null");
		T value = this.getHandle().getGameRules().get(CraftGameRule.bukkitToMinecraft(rule));
		return shimLegacyValue(value, rule);
	}

	/*
	private static Map<String, GameRules.Key<?>> gamerules;

	public synchronized Map<String, GameRules.Key<?>> getGameRulesNMS() {
		if (CraftWorld.gamerules != null) {
			return CraftWorld.gamerules;
		}
		return CraftWorld.gamerules = getGameRulesNMS(this.getHandle().getGameRules());
	}
	
	public static Map<String, GameRules.Key<?>> getGameRulesNMS(GameRules gameRules) {
        Map<String, GameRules.Key<?>> gamerules = new HashMap<>();
        gameRules.accept(new GameRules.Visitor() {
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                gamerules.put(key.getName(), key);
            }
        });
        return gamerules;
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
	public synchronized Map<String, GameRules.Type<?>> getGameRuleDefinitions() {
        if (gameruleDefinitions != null) {
            return CraftWorld.gameruleDefinitions;
        }

        Map<String, GameRules.Type<?>> gameruleDefinitions = new HashMap<>();
        this.getHandle().getGameRules().accept(new GameRules.Visitor() {
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                gameruleDefinitions.put(key.getName(), type);
            }
        });

        return CraftWorld.gameruleDefinitions = gameruleDefinitions;
    }
    */

	@Override
	public String[] getGameRules() {
		return this.getHandle().getGameRules().availableRules().map(net.minecraft.world.level.gamerules.GameRule::id).toArray(String[]::new);
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
		return world.getHeight(
				switch(map) {
					case WORLD_SURFACE -> Types.WORLD_SURFACE;
					case OCEAN_FLOOR -> Types.OCEAN_FLOOR;
					case MOTION_BLOCKING -> Types.MOTION_BLOCKING;
					case WORLD_SURFACE_WG -> Types.WORLD_SURFACE_WG;
					case OCEAN_FLOOR_WG -> Types.OCEAN_FLOOR_WG;
					case MOTION_BLOCKING_NO_LEAVES -> Types.MOTION_BLOCKING_NO_LEAVES;
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

		for(Object o : world.getAllEntities()) {
			if(o instanceof net.minecraft.world.entity.Entity) {
				net.minecraft.world.entity.Entity mcEnt = (net.minecraft.world.entity.Entity) o;
				Entity bukkitEntity = ((EntityBridge) mcEnt).getBukkitEntity();

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
		Long2ObjectLinkedOpenHashMap<ChunkHolder> chunks = ((ChunkMapBridge) (world.getChunkSource().chunkMap)).getChunkHoldersBF();
		return chunks.values()
				.stream()
				.map(ChunkHolderBridge::getFullChunkNow)
				.filter(Objects::nonNull)
				.map(CraftWorld::getBukkitChunkForChunk)
				.toArray(Chunk[]::new);
	}

	private static Chunk getBukkitChunkForChunk(LevelChunk mc) {
		return ((LevelChunkBridge) mc).getBukkitChunk();
	}

	@Override
	public int getMaxHeight() {
		return world.getHeight();
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
	public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z, Predicate<? super Entity> filter) {
		BoundingBox aabb = BoundingBox.of(location, x, y, z);
		return this.getNearbyEntities(aabb, filter);
	}

	@Override
	public Collection<Entity> getNearbyEntities(BoundingBox boundingBox) {
		return this.getNearbyEntities(boundingBox, null);
	}

	@Override
	public Collection<Entity> getNearbyEntities(BoundingBox boundingBox, Predicate<? super Entity> filter) {
		AABB bb = new AABB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
		List<net.minecraft.world.entity.Entity> entityList = world.getEntities((net.minecraft.world.entity.Entity) null, bb, null);
		List<Entity> bukkitEntityList = new ArrayList<org.bukkit.entity.Entity>(entityList.size());

		for(net.minecraft.world.entity.Entity entity : entityList) {
			Entity bukkitEntity = ((EntityBridge) entity).getBukkitEntity();
			if(filter == null || filter.test(bukkitEntity))
				bukkitEntityList.add(bukkitEntity);
		}

		return bukkitEntityList;
	}

	@Override
	public boolean getPVP() {
		return this.world.getGameRules().get(GameRules.PVP);
		// return this.nms.pvpMode.toBooleanOrElseGet(() -> this.world.getGameRules().getValue(GameRules.PVP));
	}

	@Override
	public List<Player> getPlayers() {
		List<Player> list = new ArrayList<>(world.players().size());

		for(ServerPlayer player : world.players())
			list.add((Player) ((ServerPlayerBridge) player).getBukkitEntity());

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
		return world.getSeaLevel();
	}

	@Override
	public long getSeed() {
		return world.getSeed();
	}

	@Override
	public double getTemperature(int x, int z) {
		return getTemperature(x, 0, z);
	}

	@Override
	public double getTemperature(int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		IMixinWorld icommon = (IMixinWorld) world;
		return icommon.I_get_biome_for_noise_gen(x >> 2, y >> 2, z >> 2).getTemperature(pos, this.world.getSeaLevel());
	}

	@Override
	public int getThunderDuration() {
		return this.world.getWeatherData().getThunderTime();
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
		return world.getGameTime();
	}

	@Override
	public UUID getUID() {
		return world.cardboard$get_uuid();
		// return Utils.getWorldUUID(getWorldFolder());
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && this.getClass() == obj.getClass() &&
				this.getName().equals(((CraftWorld) obj).getName());
	}

	@Override
	public int getWaterAnimalSpawnLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWeatherDuration() {
		return this.world.getWeatherData().getRainTime();
	}

	@Override
	public WorldBorder getWorldBorder() {
		if(this.worldBorder == null)
			this.worldBorder = new CraftWorldBorder(this);

		return this.worldBorder;
	}

	@Override
	public File getWorldFolder() {
		// FIXME BROKEN (check for DMM1 & DMM-1)
		return world.getServer().getServerDirectory().toFile();
	}

	@Override
	public WorldType getWorldType() {
		return world.isFlat() ? WorldType.FLAT : WorldType.NORMAL;
	}

	@Override
	public boolean hasStorm() {
		return this.world.getWeatherData().isRaining();
	}

	@Override
	public boolean isAutoSave() {
		return !world.noSave();
	}

	@Override
	public boolean isChunkForceLoaded(int arg0, int arg1) {
		return world.getForceLoadedChunks().contains(ChunkPos.pack(arg0, arg1));
	}

	@Override
	public boolean isChunkGenerated(int x, int z) {
		try {
			return isChunkLoaded(x, z) || world.getChunkSource().chunkMap.read(new ChunkPos(x, z)) != null;
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
		return (null != world.getChunkSource().getChunk(x, z, false));
	}

	@Override
	public boolean isGameRule(String rule) {
		Preconditions.checkArgument(rule != null, "String rule cannot be null");
		Preconditions.checkArgument(!rule.isEmpty(), "String rule cannot be empty");
		GameRule<?> bukkit = GameRule.getByName(rule);
		return bukkit == null ? false : this.getHandle().getGameRules().rules.has(CraftGameRule.bukkitToMinecraft(bukkit));
	}

	@Override
	public boolean isHardcore() {
		return world.getLevelData().isHardcore();
	}

	@Override
	public boolean isThundering() {
		return this.world.getWeatherData().isThundering();
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
		net.minecraft.world.level.chunk.ChunkAccess chunk = world.getChunkSource()
				.getChunk(x, z, generate ? ChunkStatus.FULL : ChunkStatus.EMPTY, true);

		if(chunk instanceof ImposterProtoChunk)
			chunk = world.getChunkSource().getChunk(x, z, ChunkStatus.FULL, true);

		if(chunk instanceof net.minecraft.world.level.chunk.LevelChunk) {
			world.getChunkSource().addTicketWithRadius(ChunkTicketBridge.PLUGIN_TICKET, new ChunkPos(x, z), 1);
			// nms.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(x, z), 1, Unit.INSTANCE);
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
		ClientboundLevelEventPacket packet = new ClientboundLevelEventPacket(packetData, new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), data, false);
		radius *= radius;
		for(Player player : this.getPlayers()) {
			if(((CraftPlayer) player).getHandle().connection == null || !location.getWorld()
					.equals(player.getWorld()) || (int) player.getLocation()
					.distanceSquared(location) > radius) continue;
			((CraftPlayer) player).getHandle().connection.send(packet);
		}
	}

	@Override
	public void playSound(Location loc, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {
		this.playSound(loc, sound, category, volume, pitch, this.getHandle().getRandom().nextLong());
	}

	@Override
	public void playSound(Location loc, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {
		this.playSound(loc, sound, category, volume, pitch, this.getHandle().getRandom().nextLong());
	}

	@Override
	public void playSound(Location loc, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch, long seed) {
		org.spigotmc.AsyncCatcher.catchOp("play sound"); // Paper
		if (loc == null || sound == null || category == null) return;

		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();

		this.getHandle().playSeededSound(null, x, y, z, CraftSound.bukkitToMinecraft(sound), SoundSource.valueOf(category.name()), volume, pitch, seed);
	}

	@Override
	public void playSound(Location loc, String sound, org.bukkit.SoundCategory category, float volume, float pitch, long seed) {
		org.spigotmc.AsyncCatcher.catchOp("play sound"); // Paper
		if (loc == null || sound == null || category == null) return;

		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();

		ClientboundSoundPacket packet = new ClientboundSoundPacket(Holder.direct(SoundEvent.createVariableRangeEvent(Identifier.parse(sound))), SoundSource.valueOf(category.name()), x, y, z, volume, pitch, seed);
		this.world.getServer().getPlayerList().broadcast(null, x, y, z, volume > 1.0F ? 16.0F * volume : 16.0D, this.world.dimension(), packet);
	}

	@Override
	public void playSound(Entity entity, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {
		this.playSound(entity, sound, category, volume, pitch, this.getHandle().getRandom().nextLong());
	}

	@Override
	public void playSound(Entity entity, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {
		this.playSound(entity, sound, category, volume, pitch, this.getHandle().getRandom().nextLong());
	}

	@Override
	public void playSound(Entity entity, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch, long seed) {
		org.spigotmc.AsyncCatcher.catchOp("play sound"); // Paper
		if (!(entity instanceof CraftEntity craftEntity) || entity.getWorld() != this || sound == null || category == null) return;

		ClientboundSoundEntityPacket packet = new ClientboundSoundEntityPacket(CraftSound.bukkitToMinecraftHolder(sound), net.minecraft.sounds.SoundSource.valueOf(category.name()), craftEntity.getHandle(), volume, pitch, seed);
		ChunkMap.TrackedEntity entityTracker = this.getHandle().getChunkSource().chunkMap.entityMap.get(entity.getEntityId());
		if (entityTracker != null) {
			entityTracker.sendToTrackingPlayersAndSelf(packet);
		}
	}
	// Paper start - Adventure
	@Override
	public void playSound(final net.kyori.adventure.sound.Sound sound) {
		org.spigotmc.AsyncCatcher.catchOp("play sound"); // Paper
		final long seed = sound.seed().orElseGet(this.world.getRandom()::nextLong);
		for (ServerPlayer player : this.getHandle().players()) {
			player.connection.send(io.papermc.paper.adventure.PaperAdventure.asSoundPacket(sound, player.getX(), player.getY(), player.getZ(), seed, null));
		}
	}

	@Override
	public void playSound(Entity entity, String sound, org.bukkit.SoundCategory category, float volume, float pitch, long seed) {
		org.spigotmc.AsyncCatcher.catchOp("play sound"); // Paper
		if (!(entity instanceof CraftEntity craftEntity) || entity.getWorld() != this || sound == null || category == null) return;

		ClientboundSoundEntityPacket packet = new ClientboundSoundEntityPacket(Holder.direct(SoundEvent.createVariableRangeEvent(Identifier.parse(sound))), net.minecraft.sounds.SoundSource.valueOf(category.name()), craftEntity.getHandle(), volume, pitch, seed);
		ChunkMap.TrackedEntity entityTracker = this.getHandle().getChunkSource().chunkMap.entityMap.get(entity.getEntityId());
		if (entityTracker != null) {
			entityTracker.sendToTrackingPlayersAndSelf(packet);
		}
	}

	@Override
	public void playSound(final net.kyori.adventure.sound.Sound sound, final double x, final double y, final double z) {
		org.spigotmc.AsyncCatcher.catchOp("play sound"); // Paper
		io.papermc.paper.adventure.PaperAdventure.asSoundPacket(sound, x, y, z, sound.seed().orElseGet(this.world.getRandom()::nextLong), this.playSound0(x, y, z));
	}

	@Override
	public void playSound(final net.kyori.adventure.sound.Sound sound, final net.kyori.adventure.sound.Sound.Emitter emitter) {
		org.spigotmc.AsyncCatcher.catchOp("play sound"); // Paper
		final long seed = sound.seed().orElseGet(this.getHandle().getRandom()::nextLong);
		if (emitter == net.kyori.adventure.sound.Sound.Emitter.self()) {
			for (ServerPlayer player : this.getHandle().players()) {
				player.connection.send(io.papermc.paper.adventure.PaperAdventure.asSoundPacket(sound, player, seed, null));
			}
		} else if (emitter instanceof CraftEntity craftEntity) {
			final net.minecraft.world.entity.Entity entity = craftEntity.getHandle();
			io.papermc.paper.adventure.PaperAdventure.asSoundPacket(sound, entity, seed, this.playSound0(entity.getX(), entity.getY(), entity.getZ()));
		} else {
			throw new IllegalArgumentException("Sound emitter must be an Entity or self(), but was: " + emitter);
		}
	}

	private java.util.function.BiConsumer<net.minecraft.network.protocol.Packet<?>, Float> playSound0(final double x, final double y, final double z) {
		return (packet, distance) -> this.world.getServer().getPlayerList().broadcast(null, x, y, z, distance, this.world.dimension(), packet);
	}
	// Paper end - Adventure

	@Override
	public RayTraceResult rayTrace(Location start, Vector direction, double maxDistance, FluidCollisionMode mode, boolean ignorePassableBlocks, double raySize, Predicate<? super Entity> filter) {
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
		Vec3 startPos = new Vec3(start.getX(), start.getY(), start.getZ());
		Vec3 endPos = new Vec3(start.getX() + dir.getX(), start.getY() + dir.getY(), start.getZ() + dir.getZ());
		HitResult nmsHitResult = this.getHandle().clip(new ClipContext(startPos, endPos, ignorePassableBlocks ?
				ClipContext.Block.COLLIDER : ClipContext.Block.OUTLINE, CraftFluidCollisionMode.toFluid(mode), (net.minecraft.world.entity.Entity) null));

		return CraftRayTraceResult.convertFromInternal((LevelAccessor) this, nmsHitResult);
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
	public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, Predicate<? super Entity> filter) {
		return this.rayTraceEntities(start, direction, maxDistance, 0.0, filter);
	}

	@Override
	public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, double raySize, Predicate<? super Entity> filter) {
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
			this.world.sendBlockUpdated(new BlockPos(px + (idx / height), ((idx % height) * 16), pz), Blocks.AIR.defaultBlockState(), Blocks.STONE.defaultBlockState(), 3);
		this.world.sendBlockUpdated(new BlockPos(px + 15, (height * 16) - 1, pz + 15), Blocks.AIR.defaultBlockState(), Blocks.STONE.defaultBlockState(), 3);

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
		save(false);
	}

	@Override
	public void save(boolean flush) {
		boolean oldSave = world.noSave;
		world.noSave = false;
		world.save(null, flush, false);
		world.noSave = oldSave;
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
		world.noSave = !arg0;
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
		
		net.minecraft.world.Difficulty mc = net.minecraft.world.Difficulty.NORMAL;
		
		switch (diff) {
			case EASY:
				mc = net.minecraft.world.Difficulty.EASY;
				break;
			case HARD:
				mc = net.minecraft.world.Difficulty.HARD;
				break;
			case NORMAL:
				mc = net.minecraft.world.Difficulty.NORMAL;
				break;
			case PEACEFUL:
				mc = net.minecraft.world.Difficulty.PEACEFUL;
				break;
			default:
				break;
		}
		
		// to do : per world diff
		this.getHandle().getServer().setDifficulty(mc, true);
	}

	@Override
    public void setFullTime(long time) {
        if (this.world.dimensionType().defaultClock().isEmpty()) {
            throw new IllegalArgumentException("Cannot set time in world without world clock");
        }

        final long currentClockTime = this.world.getDefaultClockTime();
        final TimeSkipEvent event = new TimeSkipEvent(this, TimeSkipEvent.SkipReason.CUSTOM, time - currentClockTime);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        // Updates the clock for all players
        this.world.clockManager().setTotalTicks(this.world.dimensionType().defaultClock().get(), currentClockTime + event.getSkipAmount());
    }
	
	/*
	@Override
	public void setFullTime(long time) {
		TimeSkipEvent event = new TimeSkipEvent(this, TimeSkipEvent.SkipReason.CUSTOM, time - world.getDayTime());
		CraftServer.INSTANCE.getPluginManager().callEvent(event);
		if(event.isCancelled())
			return;

		world.setDayTime(world.getDayTime() + event.getSkipAmount());

		for(Player p : getPlayers()) {
			CraftPlayer cp = (CraftPlayer) p;
			if(cp.getHandle().connection == null) continue;

			cp.getHandle().connection.send(new ClientboundSetTimePacket(cp.getHandle()
					.level()
					.getGameTime(), cp.getHandle().level().getGameTime(), cp.getHandle()
					.level()
					.getGameRules()
					.get(GameRules.ADVANCE_TIME)));
		}
	}
	*/

	@Override
	public <T> boolean setGameRule(GameRule<T> rule, @NotNull T newValue) {
		Preconditions.checkArgument(rule != null, "GameRule cannot be null");
		Preconditions.checkArgument(newValue != null, "GameRule value cannot be null");
		net.minecraft.world.level.gamerules.GameRule<T> nms = CraftGameRule.bukkitToMinecraft(rule);
		if (!this.getHandle().getGameRules().rules.has(nms)) {
			return false;
		} else {
			if (rule instanceof CraftGameRule.LegacyGameRuleWrapper legacyGameRuleWrapper) {
				newValue = (T)legacyGameRuleWrapper.getFromLegacyToModern().apply(newValue);
			}

			return !CraftEventFactory.handleGameRuleSet(nms, newValue, this.getHandle(), null).cancelled();
		}
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

	// TODO: Is this unused in Paper, besides in CraftWorld?
	public TriState pvpMode = TriState.NOT_SET;
	
	@Override
	public void setPVP(boolean pvp) {
		if (this.world.getGameRules().get(GameRules.PVP) != pvp) {
			this.pvpMode = TriState.byBoolean(pvp);
		}
		
		// nms.getServer()
		
		// nms.getServer().setPvpEnabled(pvp);
	}

	@Override
	public void setSpawnFlags(boolean arg0, boolean arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public Location getSpawnLocation() {
		final LevelData.RespawnData respawnData = this.world.serverLevelData.getRespawnData();
		return CraftLocation.toBukkit(respawnData.pos(), this, respawnData.yaw(), respawnData.pitch());
	}

	@Override
	public boolean setSpawnLocation(Location location) {
		Preconditions.checkArgument(location != null, "location");

		return this.equals(location.getWorld()) ? this.setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch()) : false;
	}

	private boolean setSpawnLocation(int x, int y, int z, float yaw, float pitch) {
		try {
			Location previousLocation = this.getSpawnLocation();
			this.world.serverLevelData.setSpawn(LevelData.RespawnData.of(this.world.dimension(), new BlockPos(x, y, z), yaw, pitch));

			CraftServer.INSTANCE.getServer().updateEffectiveRespawnData();
			new SpawnChangeEvent(this, previousLocation).callEvent();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean setSpawnLocation(int x, int y, int z, float yaw) {
		return this.setSpawnLocation(x, y, z, yaw, 0);
	}

	@Override
	public void setStorm(boolean arg0) {
		this.world.getWeatherData().setRaining(arg0);
        this.setWeatherDuration(0);
        this.setClearWeatherDuration(0);
	}

	@Override
	public void setThunderDuration(int dur) {
		this.world.getWeatherData().setThunderTime(dur);
	}

	@Override
	public void setThundering(boolean arg0) {
		this.world.getWeatherData().setThundering(arg0);
        this.setThunderDuration(0);
        this.setClearWeatherDuration(0);
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
    public void setTime(long time) {
        long margin = (time - this.getFullTime()) % SharedConstants.TICKS_PER_GAME_DAY;
        if (margin < 0) margin += SharedConstants.TICKS_PER_GAME_DAY;
        this.setFullTime(this.getFullTime() + margin);
    }

	@Override
	public void setWaterAnimalSpawnLimit(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setWeatherDuration(int arg0) {
		this.world.getWeatherData().setRainTime(arg0);
	}

	private ServerLevelData worldProperties() {
		return ((ServerLevelBridge) world).cardboard_worldProperties();
	}

	@Override
	public <T extends Entity> T spawn(Location location, Class<T> clazz) throws IllegalArgumentException {
		return spawn(location, clazz, null, SpawnReason.CUSTOM);
	}

	@Override
	public <T extends Entity> T spawn(Location location, Class<T> clazz, java.util.function.Consumer<? super T> function) throws IllegalArgumentException {
		return spawn(location, clazz, function, SpawnReason.CUSTOM);
	}

	public <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<T> function, SpawnReason reason) throws IllegalArgumentException {
		net.minecraft.world.entity.Entity entity = createEntity_Old(location, clazz);

		return addEntity(entity, reason, function);
	}

	public net.minecraft.world.entity.Entity createEntity_Old(Location location, Class<? extends Entity> clazz) throws IllegalArgumentException {
		if(location == null || clazz == null)
			throw new IllegalArgumentException("Location or entity class cannot be null");

		net.minecraft.world.entity.Entity entity = null;

		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		float pitch = location.getPitch();
		float yaw = location.getYaw();

		if(Boat.class.isAssignableFrom(clazz)) {
			// entity = new BoatEntity(nms, x, y, z);
			entity = net.minecraft.world.entity.EntityTypes.OAK_BOAT.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			entity.setPos(x, y, z);
			entity.snapTo(x, y, z, yaw, pitch);
		} else if(FallingBlock.class.isAssignableFrom(clazz)) {
			// TODO 1.18.2
			// entity = new FallingBlockEntity(nms, x, y, z, nms.getBlockState(new BlockPos(x, y, z)));
		} else if(Projectile.class.isAssignableFrom(clazz)) {
			if(Snowball.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.SNOWBALL.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				entity.setPos(x, y, z);
			} else if(Egg.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.EGG.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				entity.setPos(x, y, z);
			} else if(AbstractArrow.class.isAssignableFrom(clazz)) {
				if(TippedArrow.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.ARROW.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
					// TODO set type
				} else if(SpectralArrow.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.SPECTRAL_ARROW.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else if(Trident.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.TRIDENT.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else {
					entity = net.minecraft.world.entity.EntityTypes.ARROW.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				}
				entity.snapTo(x, y, z, 0, 0);
			} else if(ThrownExpBottle.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.EXPERIENCE_BOTTLE.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				entity.snapTo(x, y, z, 0, 0);
			} else if(EnderPearl.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.ENDER_PEARL.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				entity.snapTo(x, y, z, 0, 0);
			} else if(ThrownPotion.class.isAssignableFrom(clazz)) {
				/*
				if(LingeringPotion.class.isAssignableFrom(clazz)) {
					entity = new PotionEntity(nms, x, y, z, new net.minecraft.item.ItemStack(Items.LINGERING_POTION));
					((PotionEntity) entity).setItem(CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.LINGERING_POTION, 1)));
				} else {
					entity = new PotionEntity(nms, x, y, z, new net.minecraft.item.ItemStack(Items.SPLASH_POTION));
					((PotionEntity) entity).setItem(CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.SPLASH_POTION, 1)));
				}
				*/
				// TODO 1.21.8
			} else if(Fireball.class.isAssignableFrom(clazz)) {
				// TODO Fireball
			} else if(ShulkerBullet.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.SHULKER_BULLET.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				entity.snapTo(x, y, z, yaw, pitch);
			} else if(LlamaSpit.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.LLAMA_SPIT.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				entity.snapTo(x, y, z, yaw, pitch);
			} else if(Firework.class.isAssignableFrom(clazz)) {
				entity = new FireworkRocketEntity(world, x, y, z, net.minecraft.world.item.ItemStack.EMPTY);
			}
		} else if(Minecart.class.isAssignableFrom(clazz)) {
			if(PoweredMinecart.class.isAssignableFrom(clazz)) {
				entity = new MinecartFurnace(net.minecraft.world.entity.EntityTypes.FURNACE_MINECART, world);
				entity.setPos(x, y, z);
			} else if(StorageMinecart.class.isAssignableFrom(clazz)) {
				entity = new MinecartChest(net.minecraft.world.entity.EntityTypes.CHEST_MINECART, world);
				entity.setPos(x, y, z);
			} else if(ExplosiveMinecart.class.isAssignableFrom(clazz)) {
				entity = new MinecartTNT(net.minecraft.world.entity.EntityTypes.TNT_MINECART, world);
				entity.setPos(x, y, z);
			} else if(HopperMinecart.class.isAssignableFrom(clazz)) {
				entity = new MinecartHopper(net.minecraft.world.entity.EntityTypes.HOPPER_MINECART, world);
				entity.setPos(x, y, z);
				
			} else if(SpawnerMinecart.class.isAssignableFrom(clazz)) {
				// entity = new SpawnerMinecartEntity(nms, x, y, z);
				entity = new MinecartSpawner(net.minecraft.world.entity.EntityTypes.SPAWNER_MINECART, world);
				entity.setPos(x, y, z);
				
			} else if(CommandMinecart.class.isAssignableFrom(clazz)) {
				entity = new MinecartCommandBlock(net.minecraft.world.entity.EntityTypes.COMMAND_BLOCK_MINECART, world);
				entity.setPos(x, y, z);
			} else {
				entity = new net.minecraft.world.entity.vehicle.minecart.Minecart(net.minecraft.world.entity.EntityTypes.MINECART, world);
				entity.setPos(x, y, z);
			}
		} else if(EnderSignal.class.isAssignableFrom(clazz)) {
			// TODO EnderSignal
		} else if(EnderCrystal.class.isAssignableFrom(clazz)) {
			entity = net.minecraft.world.entity.EntityTypes.END_CRYSTAL.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			entity.snapTo(x, y, z, 0, 0);
		} else if(LivingEntity.class.isAssignableFrom(clazz)) {
			if(Chicken.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.CHICKEN.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Cow.class.isAssignableFrom(clazz)) {
				if(MushroomCow.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.MOOSHROOM.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else {
					entity = net.minecraft.world.entity.EntityTypes.COW.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				}
			} else if(Golem.class.isAssignableFrom(clazz)) {
				if(Snowman.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.SNOW_GOLEM.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else if(IronGolem.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.IRON_GOLEM.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else if(Shulker.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.SHULKER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				}
			} else if(Creeper.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.CREEPER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Ghast.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.GHAST.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Pig.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.PIG.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Player.class.isAssignableFrom(clazz)) {
				// need a net server handler for this one
			} else if(Sheep.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.SHEEP.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(AbstractHorse.class.isAssignableFrom(clazz)) {
				if(ChestedHorse.class.isAssignableFrom(clazz)) {
					if(Donkey.class.isAssignableFrom(clazz)) {
						entity = net.minecraft.world.entity.EntityTypes.DONKEY.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
					} else if(Mule.class.isAssignableFrom(clazz)) {
						entity = net.minecraft.world.entity.EntityTypes.MULE.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
					} else if(Llama.class.isAssignableFrom(clazz)) {
						entity = TraderLlama.class.isAssignableFrom(clazz) ?
								net.minecraft.world.entity.EntityTypes.TRADER_LLAMA.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND)
								: net.minecraft.world.entity.EntityTypes.LLAMA.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
					}
				} else if(SkeletonHorse.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.SKELETON_HORSE.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else if(ZombieHorse.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.ZOMBIE_HORSE.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else entity = net.minecraft.world.entity.EntityTypes.HORSE.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Skeleton.class.isAssignableFrom(clazz)) {
				if(Stray.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.STRAY.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else if(WitherSkeleton.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.WITHER_SKELETON.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else {
					entity = net.minecraft.world.entity.EntityTypes.SKELETON.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				}
			} else if(Slime.class.isAssignableFrom(clazz)) {
				if(MagmaCube.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.MAGMA_CUBE.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else {
					entity = net.minecraft.world.entity.EntityTypes.SLIME.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				}
			} else if(Spider.class.isAssignableFrom(clazz)) {
				if(CaveSpider.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.CAVE_SPIDER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else {
					entity = net.minecraft.world.entity.EntityTypes.SPIDER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				}
			} else if(Squid.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.SQUID.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Tameable.class.isAssignableFrom(clazz)) {
				if(Wolf.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.WOLF.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else if(Parrot.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.PARROT.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else if(Cat.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.CAT.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				}
			} else if(PigZombie.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.ZOMBIFIED_PIGLIN.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Zombie.class.isAssignableFrom(clazz)) {
				if(Husk.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.HUSK.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else if(ZombieVillager.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.ZOMBIE_VILLAGER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else if(Drowned.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.DROWNED.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else {
					entity = new net.minecraft.world.entity.monster.zombie.Zombie(world);
				}
			} else if(Giant.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.GIANT.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Silverfish.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.SILVERFISH.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Enderman.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.ENDERMAN.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Blaze.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.BLAZE.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(AbstractVillager.class.isAssignableFrom(clazz)) {
				if(Villager.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.VILLAGER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else if(WanderingTrader.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.WANDERING_TRADER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				}
			} else if(Witch.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.WITCH.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Wither.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.WITHER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(ComplexLivingEntity.class.isAssignableFrom(clazz)) {
				if(EnderDragon.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.ENDER_DRAGON.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				}
			} else if(Ambient.class.isAssignableFrom(clazz)) {
				if(Bat.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.BAT.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				}
			} else if(Rabbit.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.RABBIT.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Endermite.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.ENDERMITE.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Guardian.class.isAssignableFrom(clazz)) {
				if(ElderGuardian.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.ELDER_GUARDIAN.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else {
					entity = net.minecraft.world.entity.EntityTypes.GUARDIAN.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				}
			} else if(ArmorStand.class.isAssignableFrom(clazz)) {
				entity = new net.minecraft.world.entity.decoration.ArmorStand(world, x, y, z);
			} else if(PolarBear.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.POLAR_BEAR.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Vex.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.VEX.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Illager.class.isAssignableFrom(clazz)) {
				if(Spellcaster.class.isAssignableFrom(clazz)) {
					if(Evoker.class.isAssignableFrom(clazz)) {
						entity = net.minecraft.world.entity.EntityTypes.EVOKER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
					} else if(Illusioner.class.isAssignableFrom(clazz)) {
						entity = net.minecraft.world.entity.EntityTypes.ILLUSIONER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
					}
				} else if(Vindicator.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.VINDICATOR.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else if(Pillager.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.PILLAGER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				}
			} else if(Turtle.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.TURTLE.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Phantom.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.PHANTOM.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Fish.class.isAssignableFrom(clazz)) {
				if(Cod.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.COD.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else if(PufferFish.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.PUFFERFISH.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else if(Salmon.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.SALMON.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				} else if(TropicalFish.class.isAssignableFrom(clazz)) {
					entity = net.minecraft.world.entity.EntityTypes.TROPICAL_FISH.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				}
			} else if(Dolphin.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.DOLPHIN.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Ocelot.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.OCELOT.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Ravager.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.RAVAGER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Panda.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.PANDA.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Fox.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.FOX.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Bee.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.BEE.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Hoglin.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.HOGLIN.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Piglin.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.PIGLIN.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(PiglinBrute.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.PIGLIN_BRUTE.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Strider.class.isAssignableFrom(clazz)) {
				entity = net.minecraft.world.entity.EntityTypes.STRIDER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
			} else if(Zoglin.class.isAssignableFrom(clazz))
				entity = net.minecraft.world.entity.EntityTypes.ZOGLIN.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);

			if(entity != null) {
				entity.absSnapTo(x, y, z, yaw, pitch);
				entity.setYHeadRot(yaw); // SPIGOT-3587
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
			final BlockPos pos = BlockPos.containing(x, y, z);
			for(BlockFace dir : faces) {
				net.minecraft.world.level.block.state.BlockState nmsBlock = world.getBlockState(pos.relative(CraftBlock.blockFaceToNotch(dir)));
				if(nmsBlock.isSolid() || DiodeBlock.isDiode(nmsBlock)) {
					// TODO
				}
			}

			if(LeashHitch.class.isAssignableFrom(clazz)) {
				entity = new LeashFenceKnotEntity(world, BlockPos.containing(x, y, z));
				// TODO 1.17ify entity.teleporting = true;
			} else {
				// No valid face found
				Preconditions.checkArgument(face != BlockFace.SELF, "Cannot spawn hanging entity for %s at %s (no free face)", clazz.getName(), location);

				Direction dir = CraftBlock.blockFaceToNotch(face).getOpposite();
				if(Painting.class.isAssignableFrom(clazz)) {
					// TODO: 1.19
					// entity = new PaintingEntity(nms, new BlockPos(x, y, z), dir);
				} else if(ItemFrame.class.isAssignableFrom(clazz)) {
					entity = new net.minecraft.world.entity.decoration.ItemFrame(world, BlockPos.containing(x, y, z), dir);
				}
			}

			if(entity != null && !((HangingEntity) entity).survives())
				throw new IllegalArgumentException("Cannot spawn hanging entity for " + clazz.getName() + " at " + location);
		} else if(TNTPrimed.class.isAssignableFrom(clazz)) {
			entity = new PrimedTnt(world, x, y, z, null);
		} else if(ExperienceOrb.class.isAssignableFrom(clazz)) {
			entity = new net.minecraft.world.entity.ExperienceOrb(world, x, y, z, 0);
		} else if(LightningStrike.class.isAssignableFrom(clazz)) {
			entity = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
		} else if(AreaEffectCloud.class.isAssignableFrom(clazz)) {
			entity = new net.minecraft.world.entity.AreaEffectCloud(world, x, y, z);
		} else if(EvokerFangs.class.isAssignableFrom(clazz))
			entity = new net.minecraft.world.entity.projectile.EvokerFangs(world, x, y, z, (float) Math.toRadians(yaw), 0, null);

		if(entity != null)
			return entity;
		throw new IllegalArgumentException("Cannot spawn an entity for " + clazz.getName());
	}

	public <T extends Entity> T addEntity(net.minecraft.world.entity.Entity entity, SpawnReason reason) throws IllegalArgumentException {
		return addEntity(entity, reason, null);
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> T addEntity(net.minecraft.world.entity.Entity entity, SpawnReason reason, Consumer<T> function) throws IllegalArgumentException {
		Preconditions.checkArgument(entity != null, "Cannot spawn null entity");

		// if (entity instanceof MobEntity)
		//    ((MobEntity) entity).initialize(nms, getHandle().getLocalDifficulty(entity.getBlockPos()), net.minecraft.entity.SpawnReason.COMMAND, (EntityData) null, null);

		if(function != null)
			function.accept((T) ((EntityBridge) entity).getBukkitEntity());

		world.addEntity(entity); // TODO spawn reason
		return (T) ((EntityBridge) entity).getBukkitEntity();
	}

	@Override
	public <T extends AbstractArrow> T spawnArrow(Location location, Vector direction, float speed, float spread, Class<T> clazz) {
		Preconditions.checkArgument(location != null, "Location cannot be null");
		Preconditions.checkArgument(direction != null, "Vector cannot be null");
		Preconditions.checkArgument(clazz != null, "clazz Entity for the arrow cannot be null");

		net.minecraft.world.entity.projectile.arrow.AbstractArrow arrow;
		if (TippedArrow.class.isAssignableFrom(clazz)) {
			arrow = net.minecraft.world.entity.EntityTypes.ARROW.create(this.world, EntitySpawnReason.COMMAND);
			((Arrow) arrow.getBukkitEntity()).setBasePotionType(PotionType.WATER);
		} else if (SpectralArrow.class.isAssignableFrom(clazz)) {
			arrow = net.minecraft.world.entity.EntityTypes.SPECTRAL_ARROW.create(this.world, EntitySpawnReason.COMMAND);
		} else if (Trident.class.isAssignableFrom(clazz)) {
			arrow = net.minecraft.world.entity.EntityTypes.TRIDENT.create(this.world, EntitySpawnReason.COMMAND);
		} else {
			arrow = net.minecraft.world.entity.EntityTypes.ARROW.create(this.world, EntitySpawnReason.COMMAND);
		}

		arrow.snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		arrow.shoot(direction.getX(), direction.getY(), direction.getZ(), speed, spread);
		this.world.addFreshEntity(arrow);
		return (T) arrow.getBukkitEntity();
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
		FallingBlockEntity entity = FallingBlockEntity.fall(world, BlockPos.containing(location.getX(), location.getY(), location.getZ()), ((CraftBlockData) data).getState());
		entity.time = 1;

		world.addEntity(entity/*, SpawnReason.CUSTOM*/);
		return (FallingBlock) ((EntityBridge) entity).getBukkitEntity();
	}

	@Override
	public FallingBlock spawnFallingBlock(Location location, org.bukkit.Material material, byte data) throws IllegalArgumentException {
		Validate.notNull(location, "Location cannot be null");
		Validate.notNull(material, "Material cannot be null");
		Validate.isTrue(material.isBlock(), "Material must be a block");

		// TODO 1.18.1 / 1.18.2
		FallingBlockEntity entity = FallingBlockEntity.fall(world, BlockPos.containing(location.getX(), location.getY(), location.getZ()), CraftMagicNumbers.getBlock(material)
				.defaultBlockState());
		// FallingBlockEntity entity = new FallingBlockEntity(nms, location.getX(), location.getY(), location.getZ(), CraftMagicNumbers.getBlock(material).getDefaultState());
		entity.time = 1;

		world.addEntity(entity/*, SpawnReason.CUSTOM*/);
		return (FallingBlock) ((EntityBridge) entity).getBukkitEntity();
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
		/*
		getHandle().addParticle(
				// null, // Sender
				CraftParticle.createParticleParam(particle, data), // Particle
				x, y, z, // Position
				(double) count,  // Count
				offsetX, offsetY//, offsetZ // Random offset
				// extra // Speed?
				// force
		);
		*/
		
		// TODO: 1.21.8
		
        // this.getHandle().spawnParticles(this.getHandle().getPlayers(), null, CraftParticle.createParticleParam(particle, data), force, false, x, y, z, count, offsetX, offsetY, offsetZ);

	}

	@Override
	public LightningStrike strikeLightning(Location loc) {
		LightningBolt lightning = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
		lightning.snapTo(loc.getX(), loc.getY(), loc.getZ());
		// nms.strikeLightning(lightning);
		return (LightningStrike) ((EntityBridge) lightning).getBukkitEntity();
	}

	@Override
	public LightningStrike strikeLightningEffect(Location arg0) {
		// TODO Auto-generated method stub
		return strikeLightning0(arg0, true);
	}
	
	private LightningStrike strikeLightning0(Location loc, boolean isVisual) {
		Preconditions.checkArgument(loc != null, "Location cannot be null");
		LightningBolt lightning = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(this.world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
		lightning.snapTo(loc.getX(), loc.getY(), loc.getZ());
		// lightning.isEffect = isVisual;
		// this.nms.strikeLightning(lightning);
		return (LightningStrike) ((EntityBridge) lightning).getBukkitEntity();
	}

	@Override
	public boolean unloadChunk(int x, int z) {
		return unloadChunk(x, z, true);
	}

	@Override
	public boolean unloadChunk(int x, int z, boolean save) {
		return unloadChunk0(x, z, save);
	}

	public boolean unloadChunk(Chunk chunk) {
		return unloadChunk0(chunk.getX(), chunk.getZ(), true);
	}

	private boolean unloadChunk0(int x, int z, boolean save) {
	    // First check if the chunk is in use - critical safety check
	    if (isChunkInUse(x, z)) {
	        return false;
	    }
	
	    net.minecraft.world.level.chunk.LevelChunk chunk = world.getChunk(x, z);
	    if (chunk == null) {
	        return true; // Already unloaded
	    }
	
	    // Set save flag if needed
	    // chunk.mustNotSave = !save;
	    
	    // Use the request system instead of direct manipulation
	    unloadChunkRequest(x, z);
	    
	    // Wait for pending tasks to complete
	    world.getChunkSource().pollTask();
	    
	    // Verify the chunk was actually unloaded
	    return !isChunkLoaded(x, z);
	}

	@Override
	public boolean unloadChunkRequest(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public ServerLevel getHandle() {
		return world;
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
		
		// TODO:
		return world.getChunkSource().getLoadedChunksCount();
		
		// return nms.getChunkManager().fullChunks.size();
	}

	@Override
	public int getClearWeatherDuration() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Entity getEntity(UUID arg0) {
		return ((EntityBridge) world.getEntity(arg0)).getBukkitEntity();
	}

	@Override
	public int getEntityCount() {
		return (int) world.getAllEntities().spliterator().getExactSizeIfKnown();//.entitiesByUuid.size();
	}

	/*
	@Override
	public int getHighestBlockYAt(int arg0, int arg1, HeightmapType arg2) throws UnsupportedOperationException {
		return this.getHighestBlockYAt(arg0, arg1);
	}
	*/

	/**
	 * Removed 1.21.11?
	 */
	@Override
	public MoonPhase getMoonPhase() {
		
		return MoonPhase.FIRST_QUARTER;
		
		// return MoonPhase.getPhase(nms.getLunarTime());
	}

	@Override
	public int getNoTickViewDistance() {
		return 0;
	}

	@Override
	public int getPlayerCount() {
		return world.players().size();
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
		return !world.isRaining();
	}

	@Override
	public boolean isDayTime() {
		return world.isBrightOutside();
	}

	@Override
	public void setClearWeatherDuration(int dur) {
		this.world.getWeatherData().setClearWeatherTime(dur);
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

	// @Override
	public boolean doesBedWork() {
		// Removed API
		return false;
	}

	// @Override
	public boolean doesRespawnAnchorWork() {
		// Removed API
		return false;
	}

	@Override
	public Item dropItem(Location location, ItemStack item, java.util.function.Consumer<? super Item> function) {
		Preconditions.checkArgument(location != null, "Location cannot be null");
		Preconditions.checkArgument(item != null, "ItemStack cannot be null");
		ItemEntity entity = new ItemEntity(this.world, location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(item));
		Item itemEntity = (Item) ((EntityBridge) entity).getBukkitEntity();
		entity.pickupDelay = 10;
		if (function != null) {
			function.accept(itemEntity);
		}

		// this.nms.addFreshEntity(entity, SpawnReason.CUSTOM);
		this.world.addEntity(entity);
		return itemEntity;
	}

	@Override
	public @NotNull Item dropItemNaturally(@NotNull Location arg0, @NotNull ItemStack arg1,
	                                       @Nullable java.util.function.Consumer<? super Item> arg2) {
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
		 return this.world.getLevelData().getGameTime();
	}

	@Override
	public @NotNull Collection<Material> getInfiniburn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull NamespacedKey getKey() {
		// TODO Auto-generated method stub
		return CraftNamespacedKey.fromMinecraft(this.world.dimension().identifier());
		// return NamespacedKey.minecraft(this.getName());
	}

	@Override
	public int getMinHeight() {
		return this.world.getMinY();
	}

	// @Override
	public boolean hasBedrockCeiling() {
		// Removed API
		return false;
	}

	@Override
	public boolean hasRaids() {
		return this.world.environmentAttributes().getDimensionValue(EnvironmentAttributes.CAN_START_RAID);
	}

	// @Override
	public boolean hasSkylight() {
		// Removed API
		return false;
	}

	@Override
	public boolean isFixedTime() {
		return this.getHandle().dimensionType().hasFixedTime();
	}

	@Override
	public boolean isNatural() {
		throw new UnsupportedOperationException("// TODO - snapshot");
		// return this.nms.getDimension().natural();
	}

	@Override
	public boolean isPiglinSafe() {
		return !this.world.environmentAttributes().getDimensionValue(EnvironmentAttributes.PIGLINS_ZOMBIFY);
	}

	// @Override
	public boolean isUltraWarm() {
		return this.world.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)
			&& this.world.environmentAttributes().getDimensionValue(EnvironmentAttributes.FAST_LAVA)
			&& this.world
				.environmentAttributes()
				.getDimensionValue(EnvironmentAttributes.DEFAULT_DRIPSTONE_PARTICLE)
				.equals(ParticleTypes.DRIPPING_DRIPSTONE_LAVA);
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
	                            @Nullable java.util.function.Consumer<? super BlockState> arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public @NotNull Biome getBiome(@NotNull Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull BlockData getBlockData(@NotNull Location l) {
		return CraftBlockData.fromData(this.getData(l.getBlockX(), l.getBlockY(), l.getBlockZ()));
	}

	@Override
	public BlockData getBlockData(int x, int y, int z) {
		return CraftBlockData.fromData(this.getData(x, y, z));
	}

	@Override
	public @NotNull BlockState getBlockState(@NotNull Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull BlockState getBlockState(int x, int y, int z) {
		return CraftBlock.at(this.getHandle(), new BlockPos(x, y, z)).getState();
	}

	@Override
	public @NotNull Material getType(@NotNull Location location) {
		return this.getType(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	@Override
	public @NotNull Material getType(int x, int y, int z) {
		return CraftBlockType.minecraftToBukkit(this.getData(x, y, z).getBlock());
	}

	@Override
	public void setBiome(@NotNull Location arg0, @NotNull Biome arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBlockData(@NotNull Location arg0, @NotNull BlockData arg1) {
		super.setBlockData(arg0, arg1);
	}

	@Override
	public void setBlockData(int arg0, int arg1, int arg2, @NotNull BlockData arg3) {
		super.setBlockData(arg0, arg1, arg2, arg3);
	}

	@Override
	public void setType(@NotNull Location arg0, @NotNull Material arg1) {
		super.setType(arg0, arg1);
	}

	
	@Override
	public void setType(int arg0, int arg1, int arg2, @NotNull Material arg3) {
		super.setType(arg0, arg1, arg2, arg3);
	}

	@Override
	public <T extends Entity> @NotNull T spawn(@NotNull Location arg0, @NotNull Class<T> arg1, boolean arg2,
	                                           @Nullable java.util.function.Consumer<? super T> arg3) throws IllegalArgumentException {
		return super.spawn(arg0, arg1, arg2, arg3);
	}

	@Override
	public @NotNull Entity spawnEntity(@NotNull Location arg0, @NotNull EntityType arg1, boolean arg2) {
		return super.spawnEntity(arg0, arg1, arg2);
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
		return this.world.dimensionType().logicalHeight();
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
		return this.world.environmentAttributes().getDimensionValue(EnvironmentAttributes.BED_RULE).canSleep().test(this.world);
	}

	@Override
	public boolean isRespawnAnchorWorks() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void sendGameEvent(@Nullable Entity sourceEntity, @NotNull GameEvent gameEvent, @NotNull Vector position) {
		this.getHandle()
        .gameEvent(
           sourceEntity != null ? ((CraftEntity)sourceEntity).getHandle() : null,
           BuiltInRegistries.GAME_EVENT.get(CraftNamespacedKey.toMinecraft(gameEvent.getKey())).orElseThrow(),
           CraftVector_toBlockPos(position)
        );
	}
	
	public static BlockPos CraftVector_toBlockPos(Vector vector) {
	      return BlockPos.containing(vector.getX(), vector.getY(), vector.getZ());
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
	                            @Nullable Predicate<? super BlockState> arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public @NotNull Biome getComputedBiome(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public BiomeProvider vanillaBiomeProvider() {
		ServerChunkCache serverCache = this.getHandle().getChunkSource();
		net.minecraft.world.level.chunk.ChunkGenerator gen = serverCache.getGenerator();
		final BiomeSource biomeSource;

		/*
	      if (gen instanceof CustomChunkGenerator custom) {
	         biomeSource = custom.getDelegate().getBiomeSource();
	      } else {
	         biomeSource = gen.getBiomeSource();
	      }

	      if (biomeSource instanceof CustomWorldChunkManager customBiomeSource) {
	         biomeSource = customBiomeSource.vanillaBiomeSource;
	      }
		 */
		biomeSource = gen.getBiomeSource();

		final Climate.Sampler sampler = serverCache.randomState().sampler();
		final List<Biome> possibleBiomes = biomeSource.possibleBiomes().stream().map(CraftBiome::minecraftHolderToBukkit).toList();
		return new BiomeProvider() {
			public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
				return CraftBiome.minecraftHolderToBukkit(biomeSource.getNoiseBiome(x >> 2, y >> 2, z >> 2, sampler));
			}

			public List<Biome> getBiomes(WorldInfo worldInfo) {
				return possibleBiomes;
			}
		};
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
        AABB aabb = new AABB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        return !this.getHandle().noCollision(aabb);
    }

	@Override
	public StructureSearchResult locateNearestStructure(Location origin, org.bukkit.generator.structure.StructureType structureType, int radius, boolean findUnexplored) {
		List<Structure> structures = new ArrayList<>();
		for (Structure structure : RegistryAccess.registryAccess().getRegistry(RegistryKey.STRUCTURE)) {
			if (structure.getStructureType() == structureType) {
				structures.add(structure);
			}
		}

		return this.locateNearestStructure(origin, structures, radius, findUnexplored);
	}

	@Override
	public StructureSearchResult locateNearestStructure(Location origin, Structure structure, int radius, boolean findUnexplored) {
		return this.locateNearestStructure(origin, List.of(structure), radius, findUnexplored);
	}

	private StructureSearchResult locateNearestStructure(Location origin, List<Structure> structures, int radius, boolean findUnexplored) {
		// Cardboard: TODO
		return null;
		/*
		Pair<BlockPos, Holder<net.minecraft.world.level.levelgen.structure.Structure>> found = this.getHandle().getChunkSource().getGenerator().findNearestMapStructure(
				this.getHandle(),
				HolderSet.direct(CraftStructure::bukkitToMinecraftHolder, structures),
				CraftLocation.toBlockPosition(origin),
				radius,
				findUnexplored
				);
		if (found == null) {
			return null;
		}

		return new CraftStructureSearchResult(CraftStructure.minecraftHolderToBukkit(found.getSecond()), CraftLocation.toBukkit(found.getFirst(), this));
		*/
	}

	// 1.19.4

	// @Override
    public org.bukkit.Chunk getChunkAt(int x2, int z2, boolean generate) {
        if (generate) {
            return this.getChunkAt(x2, z2);
        }
        return new CraftChunk(this.getHandle(), x2, z2);
    }

	@Override
	public @NotNull Set<FeatureFlag> getFeatureFlags() {
        return CraftFeatureFlag.getFromNMS(this.getHandle().enabledFeatures()).stream().map(FeatureFlag.class::cast).collect(Collectors.toUnmodifiableSet());
	}
	
	// 1.20.2 API:

	@Override
	public <T extends Entity> @NotNull T spawn(@NotNull Location location, @NotNull Class<T> clazz,
			java.util.function.@Nullable Consumer<? super T> function, @NotNull SpawnReason reason)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
        return (T)((LivingEntity)this.spawn(location, clazz, function, reason));
	}

	@Override
	public boolean hasStructureAt(@NotNull Position position, @NotNull Structure structure) {
		// TODO Auto-generated method stub
        net.minecraft.world.level.levelgen.structure.Structure stru =
        		this.getHandle().registryAccess().lookupOrThrow(Registries.STRUCTURE)
        		.get(
        				CraftNamespacedKey.toMinecraft(structure.getStructureType().getKey())
        		).orElseThrow().value();

        return this.getHandle().structureManager().getStructureWithPieceAt(MCUtil_toBlockPos(position), stru).isValid();

	}
	
	// TODO: MCUtil.toBlockPos
    public static BlockPos MCUtil_toBlockPos(Position pos) {
        return new BlockPos(pos.blockX(), pos.blockY(), pos.blockZ());
    }

	@Override
	public @Nullable RayTraceResult rayTraceEntities(@NotNull Position start, @NotNull Vector direction,
			double maxDistance, double raySize, @Nullable Predicate<? super Entity> filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @Nullable RayTraceResult rayTraceBlocks(@NotNull Position start, @NotNull Vector direction,
			double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks,
			@Nullable Predicate<? super Block> canCollide) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @Nullable RayTraceResult rayTrace(@NotNull Position start, @NotNull Vector direction, double maxDistance,
			@NotNull FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, double raySize,
			@Nullable Predicate<? super Entity> filter, @Nullable Predicate<? super Block> canCollide) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void playNote(@NotNull Location loc, @NotNull Instrument instrument, @NotNull Note note) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BiomeSearchResult locateNearestBiome(Location origin, int radius, org.bukkit.block.Biome ... biomes) {
        return this.locateNearestBiome(origin, radius, 32, 64, biomes);
    }

	@Override
    public BiomeSearchResult locateNearestBiome(Location origin, int radius, int horizontalInterval, int verticalInterval, org.bukkit.block.Biome ... biomes) {
        BlockPos originPos = CraftLocation.toBlockPosition(origin);
        HashSet<Holder<net.minecraft.world.level.biome.Biome>> holders = new HashSet<Holder<net.minecraft.world.level.biome.Biome>>();
        for (org.bukkit.block.Biome biome : biomes) {
            holders.add(CraftBiome.bukkitToMinecraftHolder(biome));
        }

        Climate.Sampler sampler = this.getHandle().getChunkSource().randomState().sampler();
        Pair<BlockPos, Holder<net.minecraft.world.level.biome.Biome>> found = this.getHandle().getChunkSource().getGenerator().getBiomeSource().findClosestBiome3d(originPos, radius, horizontalInterval, verticalInterval, holders::contains, sampler, this.getHandle());
        if (found == null) {
            return null;
        }
        return new CraftBiomeSearchResult(CraftBiome.minecraftHolderToBukkit((Holder)found.getSecond()), new Location((World)this, (double)((BlockPos)found.getFirst()).getX(), (double)((BlockPos)found.getFirst()).getY(), (double)((BlockPos)found.getFirst()).getZ()));
    }

	@Override
	public void setBiome(int var1, int var2, int var3, Holder<net.minecraft.world.level.biome.Biome> var4) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterable<net.minecraft.world.entity.Entity> getNMSEntities() {
		
		return ((LevelBridge)this.getHandle()).cb$get_entity_lookup().getAll();
	}

	@Override
	public void addEntityToWorld(net.minecraft.world.entity.Entity entity, SpawnReason reason) {
		this.getHandle().addFreshEntity(entity);
	}

	@Override
	public void addEntityWithPassengers(net.minecraft.world.entity.Entity entity, SpawnReason reason) {
		this.getHandle().tryAddFreshEntityWithPassengers(entity);
	}

	@Override
    public <T extends Entity> T createEntity(Location location, Class<T> clazz) throws IllegalArgumentException {
        net.minecraft.world.entity.Entity entity = this.createEntity(location, clazz, true);

        if (!this.isNormalWorld()) {
            // entity.setGeneration(true);
        }

        return (T) ((EntityBridge)entity).getBukkitEntity();
    }
	
	// 1.20.4 API

	@Override
	public @NotNull FluidData getFluidData(int x, int y, int z) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull Collection<Chunk> getIntersectingChunks(@NotNull BoundingBox box) {
        List<Chunk> chunks = new ArrayList<>();

        int minX = NumberConversions.floor(box.getMinX()) >> 4;
        int maxX = NumberConversions.floor(box.getMaxX()) >> 4;
        int minZ = NumberConversions.floor(box.getMinZ()) >> 4;
        int maxZ = NumberConversions.floor(box.getMaxZ()) >> 4;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                chunks.add(this.getChunkAt(x, z, false));
            }
        }

        return chunks;
	}

	@Override
	public <T extends LivingEntity> @NotNull T spawn(@NotNull Location location, @NotNull Class<T> clazz,
			@NotNull SpawnReason reason, boolean randomizeData,
			java.util.function.@Nullable Consumer<? super T> function) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return (T)((LivingEntity)this.spawn(location, clazz, function, reason));
	}

	@Override
	public @Nullable Raid getRaid(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull Collection<GeneratedStructure> getStructures(int x, int z) {
		return this.getStructures(x, z, struct -> true);
	}

	@Override
	public @NotNull Collection<GeneratedStructure> getStructures(int x, int z, @NotNull Structure structure) {
		net.minecraft.core.Registry<net.minecraft.world.level.levelgen.structure.Structure> registry = CraftRegistry.getMinecraftRegistry(Registries.STRUCTURE);
        Identifier key = registry.getKey(CraftStructure.bukkitToMinecraft(structure));

        return this.getStructures(x, z, struct -> registry.getKey(struct).equals(key));
	}
	
    private List<GeneratedStructure> getStructures(int x, int z, Predicate<net.minecraft.world.level.levelgen.structure.Structure> predicate) {
        List<GeneratedStructure> structures = new ArrayList<>();
        for (StructureStart start : this.getHandle().structureManager().startsForStructure(new ChunkPos(x, z), predicate)) {
            structures.add(new CraftGeneratedStructure(start));
        }

        return structures;
    }

    // 1.20.6 API
    
	@Override
	public @NotNull Collection<Player> getPlayersSeeingChunk(@NotNull Chunk chunk) {
		 return this.getPlayersSeeingChunk(chunk.getX(), chunk.getZ());
	}

	@Override
	public @NotNull Collection<Player> getPlayersSeeingChunk(int x, int z) {
        if (!this.isChunkLoaded(x, z)) {
            return Collections.emptySet();
        }
        List<ServerPlayer> players = this.getHandle().getChunkSource().chunkMap.getPlayers(new ChunkPos(x, z), false);
        if (players.isEmpty()) {
            return Collections.emptySet();
        }

		return players.stream()
				.filter(Objects::nonNull)
				.map((serverPlayer -> ((CraftPlayer)((EntityBridge)serverPlayer).getBukkitEntity())))
				.collect(Collectors.toUnmodifiableSet());
	}
	
	// 1.21:

	@Override
	public boolean isVoidDamageEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVoidDamageEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getVoidDamageAmount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setVoidDamageAmount(float voidDamageAmount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getVoidDamageMinBuildHeightOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setVoidDamageMinBuildHeightOffset(double minBuildHeightOffset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean createExplosion(@Nullable Entity source, @NotNull Location loc, float power, boolean setFire,
			boolean breakBlocks, boolean excludeSourceFromDamage) {
		// TODO Auto-generated method stub
        this.world.explode(
        		(net.minecraft.world.entity.Entity)(source != null ? ((CraftEntity)source).getHandle() : null),
        		(double)loc.getX(), (double)loc.getY(), (double)loc.getZ(),
        		(float)power, (boolean)setFire, 
        		(net.minecraft.world.level.Level.ExplosionInteraction)(breakBlocks ? net.minecraft.world.level.Level.ExplosionInteraction.MOB : net.minecraft.world.level.Level.ExplosionInteraction.NONE))
        		
        		;
        return true;

	}

	@Override
	public void getChunkAtAsync(int x, int z, boolean gen, boolean urgent,
			java.util.function.@NotNull Consumer<? super Chunk> cb) {

		CompletableFuture<Chunk> cf = getChunkAtAsync(x, z, gen, urgent);
		
		cf.thenAccept(cb);
		try {
			cf.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getChunksAtAsync(int minX, int minZ, int maxX, int maxZ, boolean urgent, @NotNull Runnable cb) {
		// TODO Auto-generated method stub
		// this.getHandle().loadChunks(minX, minZ, maxX, maxZ, urgent ? Priority.HIGHER : Priority.NORMAL, chunks -> cb.run());
	}

	@Override
	public @Nullable RayTraceResult rayTrace(
			java.util.function.@NotNull Consumer<PositionedRayTraceConfigurationBuilder> builderConsumer) {
		// TODO Auto-generated method stub
		
		// PositionedRayTraceConfigurationBuilderImpl builder = new PositionedRayTraceConfigurationBuilderImpl();
		/*
		final double maxDistance = builder.maxDistance.getAsDouble();
        if (builder.targets.contains(RayTraceTarget.ENTITY)) {
            if (builder.targets.contains(RayTraceTarget.BLOCK)) {
                return this.rayTrace(builder.start, builder.direction, maxDistance, builder.fluidCollisionMode, builder.ignorePassableBlocks, builder.raySize, builder.entityFilter, builder.blockFilter);
            }
            return this.rayTraceEntities(builder.start, builder.direction, maxDistance, builder.raySize, builder.entityFilter);
        }
        return this.rayTraceBlocks(builder.start, builder.direction, maxDistance, builder.fluidCollisionMode, builder.ignorePassableBlocks, builder.blockFilter);
		*/
		return null;
	}

	@Override
	public boolean hasBonusChest() {
		return true; // this.nms.option.getGeneratorOptions().hasBonusChest();
	}

	@Override
	public @NotNull Path getWorldPath() {
		return this.world.getServer().storageSource.getDimensionPath(this.world.dimension());
	}

	@Override
	public @Nullable Location locateNearestPoi(@NotNull Location origin, @NotNull PoiType poiType, @Positive int radius,
			@NotNull Occupancy occupancy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull List<PoiSearchResult> locateAllPoiInRange(@NotNull Location origin,
			@NotNull Predicate<PoiType> poiTypePredicate, @Positive int radius, @NotNull Occupancy occupancy) {
		// TODO Auto-generated method stub
		return null;
	}
	

    // 26.2: new on World
    @Override
    public void setAllowMonsterSpawning(boolean allow) {
        // Routed through the existing spawn-flag API so both share one code path.
        // NOTE: setSpawnFlags/getAllowMonsters are unimplemented in Cardboard (pre-existing).
        this.setSpawnFlags(allow, this.getAllowAnimals());
    }

}
