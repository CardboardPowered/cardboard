/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.javazilla.bukkitfabric.mixin;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.server.ServerLoadEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;
import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.impl.scheduler.BukkitSchedulerImpl;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinLevelProperties;
import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinNetworkIo;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;

import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.block.Block;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.village.ZombieSiegeManager;
import net.minecraft.world.ForcedChunkState;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.WanderingTraderManager;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.CatSpawner;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.PhantomSpawner;
import net.minecraft.world.gen.PillagerSpawner;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;

@Mixin(value=MinecraftServer.class)
public abstract class MixinMinecraftServer extends ReentrantThreadExecutor<ServerTask> implements IMixinMinecraftServer {

    public MixinMinecraftServer(String string) {
        super(string);
    }

    @Shadow @Final public DynamicRegistryManager.Impl registryManager;
    @Shadow @Final public WorldSaveHandler saveHandler;
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final public Executor workerExecutor;
    @Shadow @Final public WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory;

    @Shadow public Map<RegistryKey<net.minecraft.world.World>, ServerWorld> worlds;
    @Shadow public ServerResourceManager serverResourceManager;
    @Shadow public LevelStorage.Session session;
    @Shadow private long timeReference;
    @Shadow public DataCommandStorage dataCommandStorage;
    @Shadow public SaveProperties saveProperties;
    @Shadow private int ticks;

    @Shadow public void initScoreboard(PersistentStateManager arg0) {}
    @Shadow public void method_27731() {}
    @Shadow public void updateMobSpawnOptions() {}
    @Shadow public void setToDebugWorldProperties(SaveProperties saveProperties2) {}

    public void setDataCommandStorage(DataCommandStorage data) {
        this.dataCommandStorage = data;
    }

    @Override
    public LevelStorage.Session getSessionBF() {
        return session;
    }

    public java.util.Queue<Runnable> processQueue = new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();

    private boolean forceTicks;

    @Override
    public WorldSaveHandler getSaveHandler_BF() {
        return saveHandler;
    }

    /**
     * @reason Bukkit
     * @author Bukkit4Fabric
     */
    @Overwrite
    public String getServerModName() {
        return "Fabric,Bukkit";
    }

    @Override
    public Map<RegistryKey<net.minecraft.world.World>, ServerWorld> getWorldMap() {
        return worlds;
    }

    @Override
    public void convertWorld(String name) {
        //getServer().upgradeWorld(name);
    }

    @Override
    public WorldGenerationProgressListenerFactory getWorldGenerationProgressListenerFactory() {
        return CraftServer.server.worldGenerationProgressListenerFactory;
    }

    @Override
    public Queue<Runnable> getProcessQueue() {
        return processQueue;
    }

    @Override
    public CommandManager setCommandManager(CommandManager commandManager) {
        return (this.serverResourceManager.commandManager = commandManager);
    }

    public MinecraftServer getServer() {
        return (MinecraftServer) (Object) this;
    }

    /**
     * @reason Bukkit's Custom Multiworld handling
     * @author Bukkit4Fabric
     */
    @SuppressWarnings({ "deprecation", "resource", "unchecked", "rawtypes", "unused" })
    @Overwrite
    public void loadWorld() {
        int worldCount = 3;

        for (int worldId = 0; worldId < worldCount; ++worldId) {
            ServerWorld world;
            LevelProperties worlddata;
            byte dimension = 0;
            RegistryKey<DimensionOptions> dimensionKey = DimensionOptions.OVERWORLD;

            if (worldId == 1) {
                if (Bukkit.getAllowNether()) {
                    dimension = -1;
                    dimensionKey = DimensionOptions.NETHER;
                } else continue;
            }

            if (worldId == 2) {
                if (Bukkit.getAllowEnd()) {
                    dimension = 1;
                    dimensionKey = DimensionOptions.END;
                } else continue;
            }

            String worldType = org.bukkit.World.Environment.getEnvironment(dimension).toString().toLowerCase();
            String s = this.session.getDirectoryName();
            String name = (dimension == 0) ? s : s + "_" + worldType;
            LevelStorage.Session worldSession;
            if (dimension == 0) {
                worldSession = this.session;
            } else {
                String dim = "DIM" + dimension;

                File newWorld = new File(new File(name), dim);
                File oldWorld = new File(new File(s), dim);
                File oldLevelDat = new File(new File(s), "level.dat"); // The data folders exist on first run as they are created in the PersistentCollection constructor above, but the level.dat won't

                if (!newWorld.isDirectory() && oldWorld.isDirectory() && oldLevelDat.isFile()) {
                    BukkitFabricMod.LOGGER.info("---- Migration of old " + worldType + " folder required ----");
                    BukkitFabricMod.LOGGER.info("Unfortunately due to the way that Minecraft implemented multiworld support in 1.6, Bukkit requires that you move your " + worldType + " folder to a new location in order to operate correctly.");
                    BukkitFabricMod.LOGGER.info("We will move this folder for you, but it will mean that you need to move it back should you wish to stop using Bukkit in the future.");
                    BukkitFabricMod.LOGGER.info("Attempting to move " + oldWorld + " to " + newWorld + "...");

                    if (newWorld.exists()) {
                        BukkitFabricMod.LOGGER.warning("A file or folder already exists at " + newWorld + "!");
                        BukkitFabricMod.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                    } else if (newWorld.getParentFile().mkdirs()) {
                        if (oldWorld.renameTo(newWorld)) {
                            BukkitFabricMod.LOGGER.info("Success! To restore " + worldType + " in the future, simply move " + newWorld + " to " + oldWorld);
                            // Migrate world data too.
                            try {
                                com.google.common.io.Files.copy(oldLevelDat, new File(new File(name), "level.dat"));
                                org.apache.commons.io.FileUtils.copyDirectory(new File(new File(s), "data"), new File(new File(name), "data"));
                            } catch (IOException exception) {
                                BukkitFabricMod.LOGGER.warning("Unable to migrate world data.");
                            }
                            BukkitFabricMod.LOGGER.info("---- Migration of old " + worldType + " folder complete ----");
                        } else {
                            BukkitFabricMod.LOGGER.warning("Could not move folder " + oldWorld + " to " + newWorld + "!");
                            BukkitFabricMod.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                        }
                    } else {
                        BukkitFabricMod.LOGGER.warning("Could not create path for " + newWorld + "!");
                        BukkitFabricMod.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                    }
                }

                try {
                    worldSession = LevelStorage.create(CraftServer.INSTANCE.getWorldContainer().toPath()).createSession(name);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                MinecraftServer.convertLevel(worldSession); // Run conversion now
            }

            org.bukkit.generator.ChunkGenerator gen = CraftServer.INSTANCE.getGenerator(name);

            DynamicRegistryManager.Impl iregistrycustom_dimension = this.registryManager;

            RegistryOps<Tag> registryreadops = RegistryOps.of((DynamicOps) NbtOps.INSTANCE, this.serverResourceManager.getResourceManager(), iregistrycustom_dimension);
            worlddata = (LevelProperties) worldSession.readLevelProperties((DynamicOps) registryreadops, CraftServer.method_29735(CraftServer.server.dataPackManager));
            if (worlddata == null) {
                LevelInfo worldsettings;
                GeneratorOptions generatorsettings;

                SaveProperties dedicatedserverproperties = ((MinecraftServer)(Object) this).getSaveProperties();

                worldsettings = new LevelInfo(dedicatedserverproperties.getLevelName(), dedicatedserverproperties.getGameMode(), dedicatedserverproperties.isHardcore(), dedicatedserverproperties.getDifficulty(), false, new GameRules(), CraftServer.method_29735(CraftServer.server.dataPackManager));
                generatorsettings = dedicatedserverproperties.getGeneratorOptions();

                worlddata = new LevelProperties(worldsettings, generatorsettings, Lifecycle.stable());
            }
            ((IMixinLevelProperties)worlddata).checkName(name); // CraftBukkit - Migration did not rewrite the level.dat; This forces 1.8 to take the last loaded world as respawn (in this case the end)

            ServerWorldProperties iworlddataserver = worlddata;
            GeneratorOptions generatorsettings = worlddata.getGeneratorOptions();
            boolean flag = generatorsettings.isDebugWorld();
            long i = generatorsettings.getSeed();
            long j = BiomeAccess.hashSeed(i);
            List<Spawner> list = ImmutableList.of(new PhantomSpawner(), new PillagerSpawner(), new CatSpawner(), new ZombieSiegeManager(), new WanderingTraderManager(iworlddataserver));
            SimpleRegistry<DimensionOptions> registrymaterials = generatorsettings.getDimensions();
            DimensionOptions worlddimension = (DimensionOptions) registrymaterials.get(dimensionKey);
            DimensionType dimensionmanager;
            ChunkGenerator chunkgenerator;

            if (worlddimension == null) {
                dimensionmanager = (DimensionType) this.registryManager.getDimensionTypes().getOrThrow(DimensionType.OVERWORLD_REGISTRY_KEY);
                chunkgenerator = GeneratorOptions.createOverworldGenerator(this.registryManager.get(Registry.BIOME_KEY), this.registryManager.get(Registry.NOISE_SETTINGS_WORLDGEN), (new Random()).nextLong());
            } else {
                dimensionmanager = worlddimension.getDimensionType();
                chunkgenerator = worlddimension.getChunkGenerator();
            }

            RegistryKey<World> worldKey = RegistryKey.of(Registry.DIMENSION, dimensionKey.getValue());

            if (worldId == 0) {
                this.saveProperties = worlddata;
                this.saveProperties.setGameMode(((MinecraftServer)(Object) this).getSaveProperties().getGameMode()); // From DedicatedServer.init

                WorldGenerationProgressListener worldloadlistener = this.worldGenerationProgressListenerFactory.create(11);

                world = new ServerWorld((MinecraftServer)(Object)this, this.workerExecutor, worldSession, iworlddataserver, worldKey, dimensionmanager, worldloadlistener, chunkgenerator, flag, j, list, true/*, org.bukkit.World.Environment.getEnvironment(dimension), gen*/);
                PersistentStateManager worldpersistentdata = world.getPersistentStateManager();
                this.initScoreboard(worldpersistentdata);
                // TODO this.server.scoreboardManager = new org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager(this, world.getScoreboard());
                this.dataCommandStorage = new DataCommandStorage(worldpersistentdata);
            } else {
                WorldGenerationProgressListener worldloadlistener = this.worldGenerationProgressListenerFactory.create(11);
                world = new ServerWorld((MinecraftServer)(Object)this, this.workerExecutor, worldSession, iworlddataserver, worldKey, dimensionmanager, worldloadlistener, chunkgenerator, flag, j, ImmutableList.of(), true/*, org.bukkit.World.Environment.getEnvironment(dimension), gen*/);
            }

            worlddata.addServerBrand(this.getServerModName(), true);
            this.initWorld(world, worlddata, saveProperties, worlddata.getGeneratorOptions());
            CraftServer.INSTANCE.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(((IMixinWorld)world).getWorldImpl()));

            this.worlds.put(world.getRegistryKey(), world);
            ((MinecraftServer)(Object)this).getPlayerManager().setMainWorld(world);

            if (worlddata.getCustomBossEvents() != null)
                ((MinecraftServer)(Object)this).getBossBarManager().fromTag(worlddata.getCustomBossEvents());
        }
        this.method_27731();
        for (ServerWorld worldserver : ((MinecraftServer)(Object)this).getWorlds()) {
            this.loadSpawn(worldserver.getChunkManager().threadedAnvilChunkStorage.worldGenerationProgressListener, worldserver);
            CraftServer.INSTANCE.getPluginManager().callEvent(new org.bukkit.event.world.WorldLoadEvent(((IMixinWorld)worldserver).getWorldImpl()));
        }

        CraftServer.INSTANCE.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD);
        CraftServer.INSTANCE.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
        ((IMixinNetworkIo)(Object)getServer().getNetworkIo()).acceptConnections();

        BukkitFabricMod.isAfterWorldLoad = true;
    }

    @Override
    public void loadSpawn(WorldGenerationProgressListener worldloadlistener, ServerWorld worldserver) {
        this.forceTicks = true;

        LOGGER.info("Preparing start region for dimension {}", worldserver.getRegistryKey().getValue());
        BlockPos blockposition = worldserver.getSpawnPos();

        worldloadlistener.start(new ChunkPos(blockposition));
        ServerChunkManager chunkproviderserver = worldserver.getChunkManager();

        chunkproviderserver.getLightingProvider().setTaskBatchSize(500);
        this.timeReference = Util.getMeasuringTimeMs();
        chunkproviderserver.addTicket(ChunkTicketType.START, new ChunkPos(blockposition), 11, Unit.INSTANCE);

        while (chunkproviderserver.getTotalChunksLoadedCount() != 441)
            this.executeModerately();

        this.executeModerately();

        if (true) {
            ServerWorld worldserver1 = worldserver;
            ForcedChunkState forcedchunk = (ForcedChunkState) worldserver.getPersistentStateManager().get(ForcedChunkState::new, "chunks");

            if (forcedchunk != null) {
                LongIterator longiterator = forcedchunk.getChunks().iterator();

                while (longiterator.hasNext()) {
                    long i = longiterator.nextLong();
                    ChunkPos chunkcoordintpair = new ChunkPos(i);
                    worldserver1.getChunkManager().setChunkForced(chunkcoordintpair, true);
                }
            }
        }

        this.executeModerately();
        worldloadlistener.stop();
        chunkproviderserver.getLightingProvider().setTaskBatchSize(5);
        this.updateMobSpawnOptions();

        this.forceTicks = false;
    }

    private void executeModerately() {
        this.runTasks();
        java.util.concurrent.locks.LockSupport.parkNanos("executing tasks", 1000L);
    }

    @Inject(at = @At("HEAD"), method = "shouldKeepTicking", cancellable = true)
    public void shouldKeepTicking_BF(CallbackInfoReturnable<Boolean> ci) {
        boolean bl = this.forceTicks;
        if (bl) ci.setReturnValue(bl);
    }

    @Inject(at = @At("HEAD"), method = "tickWorlds")
    public void doBukkitRunnables(BooleanSupplier b, CallbackInfo ci) {
        ((BukkitSchedulerImpl)CraftServer.INSTANCE.getScheduler()).mainThreadHeartbeat(ticks);
        while (!processQueue.isEmpty())
            processQueue.remove().run();
    }

    @Inject(at = @At("HEAD"), method = "shutdown")
    public void doStop(CallbackInfo ci) {
        if (null != CraftServer.INSTANCE)
            CraftServer.INSTANCE.getPluginManager().disablePlugins();
    }

    public void initWorld(ServerWorld worldserver, ServerWorldProperties worldProperties, SaveProperties saveData, GeneratorOptions generatorsettings) {
        boolean flag = generatorsettings.isDebugWorld();
        // TODO Bukkit generators
        WorldBorder worldborder = worldserver.getWorldBorder();

        worldborder.load(worldProperties.getWorldBorder());
        if (!worldProperties.isInitialized()) {
            try {
                setupSpawn(worldserver, worldProperties, generatorsettings.hasBonusChest(), flag, true);
                worldProperties.setInitialized(true);
                if (flag)
                    this.setToDebugWorldProperties(this.saveProperties);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.create(throwable, "Exception initializing level");
                throw new CrashException(crashreport);
            }

            worldProperties.setInitialized(true);
        }
    }

    private static void setupSpawn(ServerWorld worldserver, ServerWorldProperties worldProperties, boolean flag, boolean flag1, boolean flag2) {
        ChunkGenerator chunkgenerator = worldserver.getChunkManager().getChunkGenerator();

        if (!flag2) {
            worldProperties.setSpawnPos(BlockPos.ORIGIN.up(chunkgenerator.getSpawnHeight()), 0.0F);
        } else if (flag1) {
            worldProperties.setSpawnPos(BlockPos.ORIGIN.up(), 0.0F);
        } else {
            BiomeSource worldchunkmanager = chunkgenerator.getBiomeSource();
            Random random = new Random(worldserver.getSeed());
            BlockPos blockposition = worldchunkmanager.locateBiome(0, worldserver.getSeaLevel(), 0, 256, (biomebase) -> {
                return biomebase.getSpawnSettings().isPlayerSpawnFriendly();
            }, random);
            ChunkPos chunkcoordintpair = blockposition == null ? new ChunkPos(0, 0) : new ChunkPos(blockposition);

            // TODO Bukkit Generators

            if (blockposition == null)
                LOGGER.warn("Unable to find spawn biome");

            boolean flag3 = false;
            Iterator<Block> iterator = BlockTags.VALID_SPAWN.values().iterator();

            while (iterator.hasNext()) {
                Block block = (Block) iterator.next();

                if (worldchunkmanager.getTopMaterials().contains(block.getDefaultState())) {
                    flag3 = true;
                    break;
                }
            }

            BlockPos start = new BlockPos(chunkcoordintpair.getStartX(), 1, chunkcoordintpair.getStartZ());
            worldProperties.setSpawnPos(start.add(8, chunkgenerator.getSpawnHeight(), 8), 0.0F);
            int i = 0, j = 0, k = 0, l = -1;

            for (int i1 = 0; i1 < 1024; ++i1) {
                if (i > -16 && i <= 16 && j > -16 && j <= 16) {
                    BlockPos blockposition1 = SpawnLocating.findServerSpawnPoint(worldserver, new ChunkPos(chunkcoordintpair.x + i, chunkcoordintpair.z + j), flag3);

                    if (blockposition1 != null) {
                        worldProperties.setSpawnPos(blockposition1, 0.0F);
                        break;
                    }
                }

                if (i == j || i < 0 && i == -j || i > 0 && i == 1 - j) {
                    k = -l;
                    l = k;
                }

                i += k;
                j += l;
            }
        }
    }

}