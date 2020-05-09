package com.fungus_soft.bukkitfabric.bukkitimpl;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.BlockChangeDelegate;
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
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class FakeWorld implements World {

    private ServerWorld nms;
    public FakeWorld(ServerWorld nms) {
        this.nms = nms;
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return false;
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
    public Item dropItem(Location arg0, ItemStack arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Item dropItemNaturally(Location arg0, ItemStack arg1) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Biome getBiome(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Block getBlockAt(Location arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Block getBlockAt(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Chunk getChunkAt(Location arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Chunk getChunkAt(Block arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Chunk getChunkAt(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Difficulty getDifficulty() {
        return Utils.fromFabric(nms.getDifficulty());
    }

    @Override
    public ChunkSnapshot getEmptyChunkSnapshot(int arg0, int arg1, boolean arg2, boolean arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DragonBattle getEnderDragonBattle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Entity> getEntities() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Entity> getEntitiesByClasses(Class<?>... arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Environment getEnvironment() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Chunk> getForceLoadedChunks() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getFullTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> T getGameRuleDefault(GameRule<T> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getGameRuleValue(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getGameRuleValue(GameRule<T> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getGameRules() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ChunkGenerator getGenerator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Block getHighestBlockAt(Location arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Block getHighestBlockAt(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHighestBlockYAt(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return 0;
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
    public double getHumidity(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getHumidity(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getKeepSpawnInMemory() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<LivingEntity> getLivingEntities() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Chunk[] getLoadedChunks() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMaxHeight() {
        // TODO Auto-generated method stub
        return 255;
    }

    @Override
    public int getMonsterSpawnLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Entity> getNearbyEntities(BoundingBox arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Entity> getNearbyEntities(BoundingBox arg0, Predicate<Entity> arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location arg0, double arg1, double arg2, double arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location arg0, double arg1, double arg2, double arg3, Predicate<Entity> arg4) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getPVP() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<Player> getPlayers() {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorldType getWorldType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasStorm() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAutoSave() {
        return !nms.isSavingDisabled();
    }

    @Override
    public boolean isChunkForceLoaded(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChunkGenerated(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChunkInUse(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChunkLoaded(Chunk arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChunkLoaded(int x, int y) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isGameRule(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isHardcore() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isThundering() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void loadChunk(Chunk arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void loadChunk(int arg0, int arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean loadChunk(int arg0, int arg1, boolean arg2) {
        // TODO Auto-generated method stub
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
    public RayTraceResult rayTrace(Location arg0, Vector arg1, double arg2, FluidCollisionMode arg3, boolean arg4,
            double arg5, Predicate<Entity> arg6) {
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
    public RayTraceResult rayTraceBlocks(Location arg0, Vector arg1, double arg2, FluidCollisionMode arg3,
            boolean arg4) {
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
    public RayTraceResult rayTraceEntities(Location arg0, Vector arg1, double arg2, double arg3,
            Predicate<Entity> arg4) {
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
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
    }

    @Override
    public void setBiome(int arg0, int arg1, Biome arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBiome(int arg0, int arg1, int arg2, Biome arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setChunkForceLoaded(int arg0, int arg1, boolean arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDifficulty(Difficulty diff) {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

    }

    @Override
    public void setSpawnFlags(boolean arg0, boolean arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean setSpawnLocation(Location arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean setSpawnLocation(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setStorm(boolean arg0) {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

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
    public <T extends Entity> T spawn(Location arg0, Class<T> arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Entity> T spawn(Location arg0, Class<T> arg1, Consumer<T> arg2) throws IllegalArgumentException {
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
    public Entity spawnEntity(Location arg0, EntityType arg1) {
        // TODO Auto-generated method stub
        return null;
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
    public void spawnParticle(Particle arg0, Location arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, T arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, T arg5) {
        // TODO Auto-generated method stub

    }

    @Override
    public void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
            T arg6) {
        // TODO Auto-generated method stub

    }

    @Override
    public void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
            double arg6) {
        // TODO Auto-generated method stub

    }

    @Override
    public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6,
            double arg7) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
            double arg6, T arg7) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5,
            double arg6, double arg7, T arg8) {
        // TODO Auto-generated method stub

    }

    @Override
    public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6,
            double arg7, double arg8) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
            double arg6, T arg7, boolean arg8) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5,
            double arg6, double arg7, double arg8, T arg9) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5,
            double arg6, double arg7, double arg8, T arg9, boolean arg10) {
        // TODO Auto-generated method stub

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

}
