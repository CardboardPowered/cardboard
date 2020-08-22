package org.bukkit.craftbukkit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.BlockChangeDelegate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Difficulty;
import org.bukkit.Effect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameRule;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.DragonBattle;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.metadata.BlockMetadataStore;
import org.bukkit.craftbukkit.util.WorldUUID;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.SpawnChangeEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Consumer;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.javazilla.bukkitfabric.Utils;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorldChunk;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.level.ServerWorldProperties;

@SuppressWarnings("deprecation")
public class CraftWorld implements World {

    public static final int CUSTOM_DIMENSION_OFFSET = 10;
    private final BlockMetadataStore blockMetadata = new BlockMetadataStore(this);

    private ServerWorld nms;
    public CraftWorld(ServerWorld world) {
        this.nms = world;
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        Set<String> result = new HashSet<String>();

        for (Player player : getPlayers())
            result.addAll(player.getListeningPluginChannels());

        return result;
    }

    @Override
    public void sendPluginMessage(Plugin plugin, String channel, byte[] message) {
        for (Player player : getPlayers())
            player.sendPluginMessage(plugin, channel, message);
    }

    @Override
    public List<MetadataValue> getMetadata(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasMetadata(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeMetadata(String arg0, Plugin arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setMetadata(String arg0, MetadataValue arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean addPluginChunkTicket(int arg0, int arg1, Plugin arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canGenerateStructures() {
        // FIXME BROKEN!!!
        return false;//nms.getLevelProperties().hasStructures();
    }

    @Override
    public boolean createExplosion(Location arg0, float arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean createExplosion(Location arg0, float arg1, boolean arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean createExplosion(double arg0, double arg1, double arg2, float arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean createExplosion(Location arg0, float arg1, boolean arg2, boolean arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean createExplosion(double arg0, double arg1, double arg2, float arg3, boolean arg4) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean createExplosion(Location arg0, float arg1, boolean arg2, boolean arg3, Entity arg4) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean createExplosion(double arg0, double arg1, double arg2, float arg3, boolean arg4, boolean arg5) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean createExplosion(double arg0, double arg1, double arg2, float arg3, boolean arg4, boolean arg5, Entity arg6) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Item dropItem(Location loc, ItemStack arg1) {
        ItemEntity entity = new ItemEntity(nms, loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(arg1));
        entity.pickupDelay = 10;
        nms.addEntity(entity);
        return (org.bukkit.entity.Item) (((IMixinEntity)entity).getBukkitEntity());
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

    @Override
    public boolean generateTree(Location arg0, TreeType arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean generateTree(Location arg0, TreeType arg1, BlockChangeDelegate arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getAllowAnimals() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getAllowMonsters() {
        // TODO Auto-generated method stub
        return false;
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
        return CraftBlock.biomeBaseToBiome(getHandle().getRegistryManager().get(Registry.BIOME_KEY), nms.getBiomeForNoiseGen(arg0 >> 2, arg1 >> 2, arg2 >> 2));
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
        return ((IMixinWorldChunk)nms.getChunkManager().getWorldChunk(x, z, true)).getBukkitChunk();
    }

    @Override
    public Difficulty getDifficulty() {
        return Utils.fromFabric(nms.getDifficulty());
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
        List<Entity> list = new ArrayList<Entity>();

        for (Object object : nms.entitiesById.values()) {
            if (object instanceof net.minecraft.entity.Entity) {
                net.minecraft.entity.Entity mc = (net.minecraft.entity.Entity) object;
                Entity bukkit = ((IMixinEntity)mc).getBukkitEntity();

                // Assuming that bukkitEntity isn't null
                if (bukkit != null && bukkit.isValid())
                    list.add(bukkit);
            }
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... arg0) {
        return (Collection<T>) getEntitiesByClasses(arg0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> arg0) {
        Collection<T> list = new ArrayList<T>();

        for (Object entity: nms.entitiesById.values()) {
            if (entity instanceof net.minecraft.entity.Entity) {
                Entity bukkitEntity = ((IMixinEntity)(net.minecraft.entity.Entity) entity).getBukkitEntity();

                if (bukkitEntity == null)
                    continue;

                Class<?> bukkitClass = bukkitEntity.getClass();

                if (arg0.isAssignableFrom(bukkitClass) && bukkitEntity.isValid())
                    list.add((T) bukkitEntity);
            }
        }

        return list;
    }

    @Override
    public Collection<Entity> getEntitiesByClasses(Class<?>... arg0) {
        Collection<Entity> list = new ArrayList<Entity>();

        for (Object entity: nms.entitiesById.values()) {
            if (entity instanceof net.minecraft.entity.Entity) {
                Entity bukkitEntity = ((IMixinEntity)(net.minecraft.entity.Entity) entity).getBukkitEntity();

                if (bukkitEntity == null)
                    continue;

                Class<?> bukkitClass = bukkitEntity.getClass();

                for (Class<?> clazz : arg0) {
                    if (clazz.isAssignableFrom(bukkitClass)) {
                        if (bukkitEntity.isValid())
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
        // TODO Auto-generated method stub
        return Environment.NORMAL;
    }

    @Override
    public Collection<Chunk> getForceLoadedChunks() {
        Set<Chunk> chunks = new HashSet<>();

        for (long coord : nms.getForcedChunks())
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
        if (arg0 == null)
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
        if (gamerules != null) {
            return gamerules;
        }

        Map<String, GameRules.Key<?>> gamerules = new HashMap<>();
        GameRules.accept(new GameRules.Visitor() {
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> gamerules_gamerulekey, GameRules.Type<T> gamerules_gameruledefinition) {
                gamerules.put(gamerules_gamerulekey.getName(), gamerules_gamerulekey);
            }
        });

        return CraftWorld.gamerules = gamerules;
    }

    private <T> T convert(GameRule<T> rule, GameRules.Rule<?> value) {
        if (value == null)
            return null;

        if (value instanceof GameRules.BooleanRule) {
            return rule.getType().cast(((GameRules.BooleanRule) value).get());
        } else if (value instanceof GameRules.IntRule) {
            return rule.getType().cast(value.getCommandResult());
        } else throw new IllegalArgumentException("Invalid GameRule type (" + value + ") for GameRule " + rule.getName());
    }

    private static Map<String, GameRules.Type<?>> gameruleDefinitions;
    public static synchronized Map<String, GameRules.Type<?>> getGameRuleDefinitions() {
        if (gameruleDefinitions != null)
            return gameruleDefinitions;

        Map<String, GameRules.Type<?>> gameruleDefinitions = new HashMap<>();
        GameRules.accept(new GameRules.Visitor() {
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> gamerules_gamerulekey, GameRules.Type<T> gamerules_gameruledefinition) {
                gameruleDefinitions.put(gamerules_gamerulekey.getName(), gamerules_gameruledefinition);
            }
        });

        return CraftWorld.gameruleDefinitions = gameruleDefinitions;
    }

    @Override
    public String[] getGameRules() {
        return getGameRulesNMS().keySet().toArray(new String[getGameRulesNMS().size()]);
    }

    @Override
    public ChunkGenerator getGenerator() {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Block getHighestBlockAt(int arg0, int arg1, HeightMap arg2) {
        // TODO Auto-generated method stub
        return null;
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
    public int getHighestBlockYAt(Location arg0, HeightMap arg1) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHighestBlockYAt(int arg0, int arg1, HeightMap arg2) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getHumidity(int x, int z) {
        return getHumidity(x, 0, z);
    }

    @Override
    public double getHumidity(int x, int y, int z) {
        return nms.getBiomeForNoiseGen(x >> 2, y >> 2, z >> 2).getDownfall();
    }

    @Override
    public boolean getKeepSpawnInMemory() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<LivingEntity> getLivingEntities() {
        List<LivingEntity> list = new ArrayList<LivingEntity>();

        for (Object o : nms.entitiesById.values()) {
            if (o instanceof net.minecraft.entity.Entity) {
                net.minecraft.entity.Entity mcEnt = (net.minecraft.entity.Entity) o;
                Entity bukkitEntity = ((IMixinEntity)mcEnt).getBukkitEntity();

                // Assuming that bukkitEntity isn't null
                if (bukkitEntity != null && bukkitEntity instanceof LivingEntity && bukkitEntity.isValid())
                    list.add((LivingEntity) bukkitEntity);
            }
        }

        return list;
    }

    @Override
    public Chunk[] getLoadedChunks() {
        // TODO Auto-generated method stub
        return null;
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
        return ((ServerWorldProperties) nms.getLevelProperties()).getLevelName();
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

        for (net.minecraft.entity.Entity entity : entityList) {
            Entity bukkitEntity = ((IMixinEntity)entity).getBukkitEntity();
            if (filter == null || filter.test(bukkitEntity))
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
        List<Player> list = new ArrayList<Player>(nms.getPlayers().size());

        for (PlayerEntity human : nms.getPlayers())
            if (human instanceof ServerPlayerEntity)
                list.add((Player) ((IMixinServerEntityPlayer)human).getBukkitEntity());

        return list;
    }

    @Override
    public Map<Plugin, Collection<Chunk>> getPluginChunkTickets() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Plugin> getPluginChunkTickets(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<BlockPopulator> getPopulators() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Raid> getRaids() {
        // TODO Auto-generated method stub
        return null;
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
    public double getTemperature(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getTemperature(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getThunderDuration() {
        // TODO Auto-generated method stub
        return 0;
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
        return WorldUUID.getUUID(getWorldFolder());
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getWeatherDuration() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public WorldBorder getWorldBorder() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public File getWorldFolder() {
        // FIXME BROKEN
        return new File(getName());//((ServerWorld)nms).getSaveHandler().getWorldDir();
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
        } catch (IOException ex) {
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
        net.minecraft.world.chunk.Chunk chunk = nms.getChunkManager().getChunk(x, z, generate ? ChunkStatus.FULL : ChunkStatus.EMPTY, true);

        if (chunk instanceof ReadOnlyChunk)
            chunk = nms.getChunkManager().getChunk(x, z, ChunkStatus.FULL, true);

        if (chunk instanceof net.minecraft.world.chunk.WorldChunk) {
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
    public Location locateNearestStructure(Location arg0, StructureType arg1, int arg2, boolean arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void playEffect(Location arg0, Effect arg1, int arg2) {
        // TODO Auto-generated method stub
    }

    @Override
    public <T> void playEffect(Location arg0, Effect arg1, T arg2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void playEffect(Location arg0, Effect arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub
    }

    @Override
    public <T> void playEffect(Location arg0, Effect arg1, T arg2, int arg3) {
        // TODO Auto-generated method stub
    }

    @Override
    public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub
    }

    @Override
    public void playSound(Location arg0, String arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub
    }

    @Override
    public void playSound(Location arg0, Sound arg1, SoundCategory arg2, float arg3, float arg4) {
        // TODO Auto-generated method stub
    }

    @Override
    public void playSound(Location arg0, String arg1, SoundCategory arg2, float arg3, float arg4) {
        // TODO Auto-generated method stub
    }

    @Override
    public RayTraceResult rayTrace(Location arg0, Vector arg1, double arg2, FluidCollisionMode arg3, boolean arg4, double arg5, Predicate<Entity> arg6) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location arg0, Vector arg1, double arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location arg0, Vector arg1, double arg2, FluidCollisionMode arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location arg0, Vector arg1, double arg2, FluidCollisionMode arg3, boolean arg4) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RayTraceResult rayTraceEntities(Location arg0, Vector arg1, double arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RayTraceResult rayTraceEntities(Location arg0, Vector arg1, double arg2, double arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RayTraceResult rayTraceEntities(Location arg0, Vector arg1, double arg2, Predicate<Entity> arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RayTraceResult rayTraceEntities(Location arg0, Vector arg1, double arg2, double arg3, Predicate<Entity> arg4) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean refreshChunk(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean regenerateChunk(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return false;
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
        for (int y = 0; y < getMaxHeight(); y++)
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
        //nms.getLevelProperties().setDifficulty(net.minecraft.world.Difficulty.byOrdinal(diff.ordinal()));
    }

    @Override
    public void setFullTime(long arg0) {
        // TODO Auto-generated method stub
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
        //nms.getLevelProperties().setHardcore(arg0);
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
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void setStorm(boolean arg0) {
        nms.getLevelProperties().setRaining(arg0);
    }

    @Override
    public void setThunderDuration(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setThundering(boolean arg0) {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return null;
    }

    public <T extends Entity> T addEntity(net.minecraft.entity.Entity entity, SpawnReason reason) throws IllegalArgumentException {
        return addEntity(entity, reason, null);
    }

    public <T extends Entity> T addEntity(net.minecraft.entity.Entity entity, SpawnReason reason, Consumer<T> function) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Arrow spawnArrow(Location arg0, Vector arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends AbstractArrow> T spawnArrow(Location arg0, Vector arg1, float arg2, float arg3, Class<T> arg4) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity spawnEntity(Location loc, EntityType entityType) {
        return spawn(loc, entityType.getEntityClass());
    }

    @Override
    public FallingBlock spawnFallingBlock(Location arg0, MaterialData arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FallingBlock spawnFallingBlock(Location arg0, BlockData arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FallingBlock spawnFallingBlock(Location arg0, Material arg1, byte arg2) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
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
        if (data != null && !particle.getDataType().isInstance(data))
            throw new IllegalArgumentException("data should be " + particle.getDataType() + " got " + data.getClass());
        // TODO Bukkit4Fabric: method
        getHandle().addParticle(
                //null, // Sender
                CraftParticle.toNMS(particle, data), // Particle
                x, y, z, // Position
                (double)count,  // Count
                offsetX, offsetY//, offsetZ // Random offset
                //extra // Speed?
                //force
        );

    }
    @Override
    public LightningStrike strikeLightning(Location arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LightningStrike strikeLightningEffect(Location arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean unloadChunk(Chunk arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean unloadChunk(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean unloadChunk(int arg0, int arg1, boolean arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean unloadChunkRequest(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public net.minecraft.world.World getHandle() {
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

    public BlockMetadataStore getBlockMetadata() {
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
        } catch (Exception e) {
            return false;
        }
    }

}