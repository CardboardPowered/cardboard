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
package org.cardboardpowered.mixin.server;

import net.minecraft.server.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.level.storage.LevelDataAndDimensions.WorldDataAndGenSettings;

import org.cardboardpowered.CardboardMod;
import org.cardboardpowered.asm.TransformAccess;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.cardboardpowered.bridge.IMinecraftServerStatic;
import org.cardboardpowered.bridge.server.MinecraftServerBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.commands.Commands;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ChunkLoadCounter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.*;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.scores.ScoreboardSaveData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
import org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.WorldInfo;
import org.cardboardpowered.bridge.server.network.ServerConnectionListenerBridge;
import org.cardboardpowered.bridge.server.level.ServerLevelBridge;
import org.cardboardpowered.bridge.world.level.storage.LevelData_RespawnDataBridge;
import org.cardboardpowered.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.cardboardpowered.impl.util.CardboardMagicNumbers;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.google.common.collect.ImmutableList;

import io.papermc.paper.world.PaperWorldLoader;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BooleanSupplier;

@Mixin(value=MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantBlockableEventLoop<TickTask> implements MinecraftServerBridge, IMinecraftServerStatic {
	// public final WorldLoader.DataLoadContext worldLoaderContext;
	public WorldLoader.DataLoadContext worldLoaderContext;
	
	@Override
	public WorldLoader.DataLoadContext cardboard$worldLoaderContext() {
		return worldLoaderContext;
	}
	
	@Override
	public void cardboard$worldLoaderContext(WorldLoader.DataLoadContext value) {
		this.worldLoaderContext = value;
	}
	
    @Shadow private long nextTickTimeNanos;
    @Shadow @Final @Mutable protected WorldData worldData;
    @Shadow public abstract ServerLevel overworld();

    @Shadow public abstract boolean saveAllChunks(boolean suppressLogs, boolean flush, boolean force);

	public MinecraftServerMixin(String name, boolean propagatesCrashes) {
		super(name, propagatesCrashes);
	}

    @Shadow @Final public PlayerDataStorage playerDataStorage;
    @Shadow public Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> levels;
    @Shadow public MinecraftServer.ReloadableResources resources;
    @Shadow public LevelStorageSource.LevelStorageAccess storageSource;
    @Shadow public CommandStorage commandStorage;
    @Shadow private int tickCount;

    // @Shadow public void initScoreboard(PersistentStateManager arg0) {}

    public void setDataCommandStorage(CommandStorage data) {
        this.commandStorage = data;
    }

    @Override
    public LevelStorageSource.LevelStorageAccess getSessionBF() {
        return storageSource;
    }

    public java.util.Queue<Runnable> processQueue = new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();

    private boolean forceTicks;

    @Override
    public PlayerDataStorage getSaveHandler_BF() {
        return playerDataStorage;
    }

    @Inject(at = @At("HEAD"), method = "getServerModName", remap=false, cancellable = true)
    public void getServerModName_cardboard(CallbackInfoReturnable<String> ci) {
        if (null != Bukkit.getServer())
            ci.setReturnValue("Cardboard (Paper+Fabric)");
    }

    @Override
    public Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> getWorldMap() {
        return levels;
    }

    @Override
    public void convertWorld(String name) {
        //getServer().upgradeWorld(name);
    }

    @Override
    public Queue<Runnable> getProcessQueue() {
        return processQueue;
    }

    @Override
    public Commands setCommandManager(Commands commandManager) {
        return (this.resources.managers().commands = commandManager);
    }

    // CraftBukkit start
    @Override
    public boolean cardboard$isDebugging() {
        return false;
    }

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static MinecraftServer getServer() {
        return (MinecraftServer) (Object) CraftServer.server;
    }
    // CraftBukkit end

    /**
     * Call WorldInitEvent
     * 
     * @author Cardboard
     */
    /*
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerWorldProperties;getWorldBorder()Lnet/minecraft/world/border/WorldBorder$Properties;"), method = "createWorlds")
    public void onBeginCreateWorld(WorldGenerationProgressListener p, CallbackInfo ci) {
        Collection<ServerWorld> worldz = this.worlds.values();

        for (ServerWorld world : worldz) {
            CraftServer.INSTANCE.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(((IMixinWorld)world).getCraftWorld()));
        }
    }
    */

    /**
     * Enable plugins
     * Call WorldLoadEvent & ServerLoadEvent
     * 
     * @author Cardboard
     */
    @SuppressWarnings({ "resource", "deprecation" })
    @Inject(at = @At("TAIL"), method = "loadLevel")
    public void afterWorldLoad(CallbackInfo ci) {
        for (ServerLevel worldserver : ((MinecraftServer)(Object)this).getAllLevels()) {
            if (worldserver != overworld()) {
                // TODO IMPORTANT
            	
            	// ServerWorld world, ServerWorldProperties worldProperties, boolean bonusChest, boolean debugWorld, ChunkLoadProgress loadProgress
            	//setInitialSpawn(worldserver, worldserver.serverLevelData, false, false, ((ServerLevelBridge) worldserver).cardboard$levelLoadListener()); // This breaks initial world spawn.
            	
            	// this.loadSpawn(worldserver.getChunkManager().chunkLoadingManager.worldGenerationProgressListener, worldserver);
                CraftServer.INSTANCE.getPluginManager().callEvent(new org.bukkit.event.world.WorldLoadEvent(((LevelBridge)worldserver).cardboard$getWorld()));
            }
        }

        CraftServer.INSTANCE.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD);
        CraftServer.INSTANCE.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
        ((ServerConnectionListenerBridge)(Object) getServer().getConnection()).acceptConnections();

        CardboardMagicNumbers.setupUnknownModdedMaterials();
        fixBukkitWorldEdit();
        CardboardMod.isAfterWorldLoad = true;
        
        // if (null != CraftServer.INSTANCE.pluginRemapper) {
        //	CraftServer.INSTANCE.pluginRemapper.pluginsEnabled();
        
    }

    /*
    @Redirect(method = "createWorlds", at = @At(value = "NEW", args = "class=net/minecraft/server/world/ServerWorld", ordinal = 1))
    private ServerWorld cardboard$spiltListener(MinecraftServer server, Executor dispatcher,
                                             LevelStorage.Session levelStorageAccess,
                                             ServerWorldProperties serverLevelData, RegistryKey dimension,
                                             DimensionOptions levelStem, WorldGenerationProgressListener progressListener,
                                             boolean isDebug, long biomeZoomSeed, List customSpawners, boolean tickTime,
                                             RandomSequencesState randomSequences) {
        WorldGenerationProgressListener listener = this.worldGenerationProgressListenerFactory.create(11);
        return new ServerWorld(server, dispatcher, levelStorageAccess, serverLevelData,
                dimension, levelStem, listener, isDebug, biomeZoomSeed, customSpawners, tickTime, randomSequences);
    }
    
    @Inject(method = "createWorlds",
            at = @At(value = "INVOKE",
            remap = false,
            target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void cardboard$initWorld(WorldGenerationProgressListener worldGenerationProgressListener,
                                     CallbackInfo ci, ServerWorldProperties serverWorldProperties,
                                     boolean wat, Registry registry, GeneratorOptions generatorOptions, long bl, long l,
                                     List list,  DimensionOptions dimensionOptions,
                                     ServerWorld serverWorld) {
        cardboard$initLevel(serverWorld);
    }

    @Inject(method = "createWorlds",
            at = @At(value = "INVOKE",
                    remap = false,
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void cardboard$initWorld0(WorldGenerationProgressListener worldGenerationProgressListener,
            CallbackInfo ci, ServerWorldProperties serverWorldProperties,
            boolean wat, Registry registry, GeneratorOptions generatorOptions, long bl, long l,
            List list,  DimensionOptions dimensionOptions,
            ServerWorld serverWorld) {
        cardboard$initLevel(serverWorld);
        cardboard$initializedLevel(serverWorld, serverWorldProperties, saveProperties, generatorOptions);
    }
    */

    @Override
    public void addLevel(ServerLevel level) {
        this.levels.put(level.dimension(), level);
    }

    @Override
    public void removeLevel(ServerLevel level) {
        ServerLevelEvents.UNLOAD.invoker().onLevelUnload(((MinecraftServer) (Object) this), level);
        this.levels.remove(level.dimension());
    }

    public void updateDifficulty() {
        ((MinecraftServer)(Object)this).setDifficulty(((ServerInterface)(Object)this).getProperties().difficulty.get(), true);
    }

    /**
     * WorldEdit does not like hybrid servers.
     */
    private void fixBukkitWorldEdit() {
        try {
            if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit"))
                return;

            ClassLoader cl = Bukkit.getPluginManager().getPlugin("WorldEdit").getClass().getClassLoader();
            Class<?> ITEM_TYPE = Class.forName("com.sk89q.worldedit.world.item.ItemType", true, cl);
            Class<?> BLOCK_TYPE = Class.forName("com.sk89q.worldedit.world.block.BlockType", true, cl);

            Object REGISTRY_ITEM = ITEM_TYPE.getDeclaredField("REGISTRY").get(null);
            Method REGISTER_ITEM = null;
            for (Method m : REGISTRY_ITEM.getClass().getMethods()) {
                if (m.getName().equalsIgnoreCase("register")) {
                    REGISTER_ITEM = m;
                    break;
                }
            }

            Object REGISTRY_BLOCK = BLOCK_TYPE.getDeclaredField("REGISTRY").get(null);
            Method REGISTER_BLOCK = null;
            for (Method m : REGISTRY_BLOCK.getClass().getMethods()) {
                if (m.getName().equalsIgnoreCase("register")) {
                    REGISTER_BLOCK = m;
                    break;
                }
            }
            HashMap<String, Material> moddedMaterials = CardboardMagicNumbers.getModdedMaterials();

            if (moddedMaterials.size() > 0)
                CardboardMod.LOGGER.info("Adding Modded blocks/items to WorldEdit registry...");
            for (String mid : moddedMaterials.keySet()) {
                try {
                    REGISTER_ITEM.invoke(REGISTRY_ITEM, "minecraft:" + mid.toLowerCase(), ITEM_TYPE.getConstructor(String.class).newInstance(mid));
                    REGISTER_BLOCK.invoke(REGISTRY_BLOCK, "minecraft:" + mid.toLowerCase(), BLOCK_TYPE.getConstructor(String.class).newInstance(mid));
                } catch (Exception e) {
                }
            }
            if (moddedMaterials.size() > 0) {
                CardboardMod.LOGGER.info("Added " + moddedMaterials.size() + " Modded blocks/items to WorldEdit registry.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    @Override
    public void loadSpawn(WorldGenerationProgressListener worldloadlistener, ServerWorld worldserver) {
        this.forceTicks = true;

        CardboardMod.LOGGER.info("Preparing start region for world " + worldserver.getRegistryKey().getValue());
        BlockPos blockposition = worldserver.getSpawnPos();

        worldloadlistener.start(new ChunkPos(blockposition));
        ServerChunkManager chunkproviderserver = worldserver.getChunkManager();

        //chunkproviderserver.getLightingProvider().setTaskBatchSize(500);
        this.tickStartTimeNanos = Util.getMeasuringTimeMs();
        chunkproviderserver.addTicket(ChunkTicketType.START, new ChunkPos(blockposition), 11); // , Unit.INSTANCE);

        while (chunkproviderserver.getTotalChunksLoadedCount() != 441)
            this.executeModerately();

        this.executeModerately();

        if (true) {
            ServerWorld worldserver1 = worldserver;
            ForcedChunkState forcedchunk = ((IMixinPersistentStateManager)worldserver.getPersistentStateManager()).Iget();

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
        //chunkproviderserver.getLightingProvider().setTaskBatchSize(5);
        this.updateMobSpawnOptions();

        this.forceTicks = false;
    }*/
    
    /*
    @Override
    public void loadSpawn(WorldGenerationProgressListener a, ServerWorld b) {
    	prepareLevels(a, b);
    }
    */
    
    /**
     * Prepare Levels 1.21.9
     */
    @Override
    public void cardboard$prepareLevel(ServerLevel serverLevel) {
    	this.forceTicks = true;
    	ChunkLoadCounter chunkLoadCounter = new ChunkLoadCounter();
    	chunkLoadCounter.track(serverLevel, () -> {
    		TicketStorage ticketStorage = serverLevel.getDataStorage().get(TicketStorage.TYPE);
    		if (ticketStorage != null) {
    			ticketStorage.activateAllDeactivatedTickets();
    		}
    	});

    	ServerLevelBridge world = (ServerLevelBridge) serverLevel;

    	world.cardboard$levelLoadListener().start(LevelLoadListener.Stage.LOAD_INITIAL_CHUNKS, chunkLoadCounter.totalChunks());

    	do {
    		world.cardboard$levelLoadListener()
    		.update(LevelLoadListener.Stage.LOAD_INITIAL_CHUNKS, chunkLoadCounter.readyChunks(), chunkLoadCounter.totalChunks());
    		this.executeModerately();
    	} while (chunkLoadCounter.pendingChunks() > 0);

    	world.cardboard$levelLoadListener().finish(LevelLoadListener.Stage.LOAD_INITIAL_CHUNKS);
    	serverLevel.setSpawnSettings(serverLevel.serverLevelData.getDifficulty() != Difficulty.PEACEFUL && serverLevel.getGameRules().get(GameRules.SPAWN_MONSTERS));
    	this.updateEffectiveRespawnData();
    	this.forceTicks = false;
    	new WorldLoadEvent(((LevelBridge)serverLevel).cardboard$getWorld()).callEvent();
    }

    @Shadow
    public void updateEffectiveRespawnData() {
    	// Shadowed
    }
    
    /*
    public void prepareLevels(WorldGenerationProgressListener listener, ServerWorld serverLevel) {
        int i2;
        this.forceTicks = true;
        CardboardMod.LOGGER.info("Preparing start region for dim: " + serverLevel.getRegistryKey().getValue());
        BlockPos sharedSpawnPos = serverLevel.getSpawnPos();
        listener.start(new ChunkPos(sharedSpawnPos));
        ServerChunkManager chunkSource = serverLevel.getChunkManager();
        this.tickStartTimeNanos = Util.getMeasuringTimeNano();
        serverLevel.setSpawnPos(sharedSpawnPos, serverLevel.getSpawnAngle());
        int _int = serverLevel.getGameRules().getInt(GameRules.SPAWN_CHUNK_RADIUS);
        int n = i2 = _int > 0 ? MathHelper.square(WorldGenerationProgressListener.getStartRegionSize(_int)) : 0;
        while (chunkSource.getTotalChunksLoadedCount() < i2) {
            this.executeModerately();
        }
        this.executeModerately();
        ServerWorld serverLevel1 = serverLevel;
        ChunkTicketManager ticketStorage = serverLevel1.getPersistentStateManager().get(ChunkTicketManager.STATE_TYPE);
        if (ticketStorage != null) {
            ticketStorage.promoteToRealTickets();
        }
        this.executeModerately();
        listener.stop();
        this.updateMobSpawnOptions();
        // serverLevel.setMobSpawnOptions(serverLevel.getDifficulty() != Difficulty.PEACEFUL && ((MinecraftDedicatedServer)(Object)this).propertiesLoader.getPropertiesHandler().spawnMonsters);
        this.forceTicks = false;
    }
    */
    
    @Shadow
    private void updateMobSpawningFlags() {
    	
    }

    @Deprecated
    private void updateMobSpawnOptions_1_15_2() {
        /*
    	Iterator<ServerWorld> iterator = ((MinecraftServer)(Object)this).getWorlds().iterator();

        while (iterator.hasNext()) {
            ServerWorld worldserver = (ServerWorld) iterator.next();

            worldserver.setMobSpawnOptions(((MinecraftServer)(Object)this).isMonsterSpawningEnabled(),
                    ((MinecraftServer)(Object)this).shouldSpawnAnimals());
        }
        */
    	this.updateMobSpawningFlags();
    }

    private void executeModerately() {
        this.runAllTasks();
        java.util.concurrent.locks.LockSupport.parkNanos("executing tasks", 1000L);
    }

    @Inject(at = @At("HEAD"), method = "haveTime", cancellable = true)
    public void shouldKeepTicking_BF(CallbackInfoReturnable<Boolean> ci) {
        boolean bl = this.forceTicks;
        if (bl) ci.setReturnValue(bl);
    }

    @Inject(at = @At("HEAD"), method = "tickChildren")
    public void doBukkitRunnables(BooleanSupplier b, CallbackInfo ci) {
        ((CraftScheduler)CraftServer.INSTANCE.getScheduler()).mainThreadHeartbeat(tickCount);
        while (!processQueue.isEmpty())
            processQueue.remove().run();
    }

    @Override
    public void cardboard_runOnMainThread(Runnable r) {
        System.out.print("runOnMainThread");
        processQueue.add(r);
    }

    private boolean hasStopped = false;
    private final Object stopLock = new Object();
    public final boolean hasStopped() {
        synchronized (stopLock) {
            return hasStopped;
        }
    }

    @Inject(at = @At("HEAD"), method = "stopServer")
    public void doStop(CallbackInfo ci) {
        synchronized(stopLock) {
            if (hasStopped) return;
            hasStopped = true;
        }

        if (null != CraftServer.INSTANCE)
            CraftServer.INSTANCE.getPluginManager().disablePlugins();
    }

    // public void initWorld(ServerWorld worldserver, ServerWorldProperties worldProperties, SaveProperties saveData, GeneratorOptions generatorsettings) {
    public void initWorld(ServerLevel serverLevel, WorldDataAndGenSettings serverLevelData, WorldOptions worldOptions) {
        cardboard$initLevel(serverLevel);
        cardboard$initializedLevel(serverLevel, serverLevelData, worldOptions);
    }

    private void cardboard$initLevel(ServerLevel serverWorld) {
        if (((CraftServer) Bukkit.getServer()).scoreboardManager == null) {
            ((CraftServer) Bukkit.getServer()).scoreboardManager = new CraftScoreboardManager((MinecraftServer) (Object) this, serverWorld.getScoreboard());
        }
        // Bukkit.getPluginManager().callEvent(new WorldInitEvent(((IMixinWorld) serverWorld).getCraftWorld()));
    }

    private void cardboard$initializedLevel(ServerLevel overworld, WorldDataAndGenSettings worldDataAndGenSettings, WorldOptions generatorsettings) {
        boolean flag = false;
        // TODO Bukkit generators
        // WorldBorder worldborder = worldserver.getWorldBorder();

        // worldborder.load(worldProperties.getWorldBorder().get());

        
        final net.minecraft.world.level.storage.ServerLevelData levelData = overworld.serverLevelData;
        // final WorldOptions worldOptions = overworld.worldGenSettings.options();
        

        this.paper$initWorldBorder(levelData, overworld);
        
        Bukkit.getPluginManager().callEvent(new WorldInitEvent(((LevelBridge) overworld).cardboard$getWorld()));
        
        if (!levelData.isInitialized()) {
            try {
            	// TODO IMPORTANT
                setInitialSpawn(overworld, levelData, generatorsettings.generateBonusChest(), flag, ((ServerLevelBridge) overworld).cardboard$levelLoadListener());
                levelData.setInitialized(true);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception initializing level");
                throw new ReportedException(crashreport);
            }

            levelData.setInitialized(true);
        }
        
        GlobalPos globalPos = ((MinecraftServer) (Object) this).selectLevelLoadFocusPos();
        ((ServerLevelBridge) overworld).cardboard$levelLoadListener().updateFocus(globalPos.dimension(), ChunkPos.containing(globalPos.pos()));
        /*
        if (worldProperties.getCustomBossEvents() != null) {
           this.getBossBarManager().readNbt(serverLevelData.getCustomBossEvents(), this.getRegistryManager());
        }
        */
    }
    
    private void paper$initWorldBorder(ServerLevelData worldProperties, ServerLevel serverLevel) {
        /*
    	Optional<WorldBorder.Settings> legacyWorldBorderSettings = worldProperties.getLegacyWorldBorderSettings();
        if (legacyWorldBorderSettings.isPresent()) {
           WorldBorder.Settings settings = legacyWorldBorderSettings.get();
           DimensionDataStorage dataStorage1 = serverLevel.getDataStorage();
           if (dataStorage1.get(WorldBorder.TYPE) == null) {
              double coordinateScale = serverLevel.dimensionType().coordinateScale();
              WorldBorder.Settings settings1 = new WorldBorder.Settings(
                 settings.centerX() / coordinateScale,
                 settings.centerZ() / coordinateScale,
                 settings.damagePerBlock(),
                 settings.safeZone(),
                 settings.warningBlocks(),
                 settings.warningTime(),
                 settings.size(),
                 settings.lerpTime(),
                 settings.lerpTarget()
              );
              WorldBorder worldBorder = new WorldBorder(settings1);
              worldBorder.applyInitialSettings(serverLevel.getGameTime());
              dataStorage1.set(WorldBorder.TYPE, worldBorder);
           }

           worldProperties.setLegacyWorldBorderSettings(Optional.empty());
        }
        */

        // TODO
        // serverLevel.getWorldBorder().world = serverLevel;
        serverLevel.getWorldBorder().setAbsoluteMaxSize(this.getServer().getAbsoluteMaxWorldSize());
        this.getServer().getPlayerList().addWorldborderListener(serverLevel);
     }

    
    @Override
    public void createLevel(
    	      LevelStem levelStem, PaperWorldLoader.WorldLoadingInfoAndData loadingInfo, LevelDataAndDimensions.WorldDataAndGenSettings serverLevelData
    	   ) {
    	
    	MinecraftServer server = (MinecraftServer) (Object) this;
    	
    	final WorldOptions worldOptions = serverLevelData.genSettings().options();
    	final ResourceKey<Level> dimensionKey = loadingInfo.info().dimensionKey();
    	final SavedDataStorage savedDataStorage = new SavedDataStorage(this.storageSource.getDimensionPath(dimensionKey).resolve(LevelResource.DATA.id()), server.getFixerUpper(), server.registryAccess());
    	savedDataStorage.set(WorldGenSettings.TYPE, new WorldGenSettings(serverLevelData.genSettings().options(), serverLevelData.genSettings().dimensions()));


        long seed = worldOptions.seed();
        long l = BiomeManager.obfuscateSeed(seed);
        List<CustomSpawner> list = ImmutableList.of(
           new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(savedDataStorage) // Paper - save to world data
        );
        
        final org.bukkit.generator.ChunkGenerator chunkGenerator = CraftServer.INSTANCE.getGenerator(loadingInfo.data().bukkitName());
        org.bukkit.generator.BiomeProvider biomeProvider = CraftServer.INSTANCE.getBiomeProvider(loadingInfo.data().bukkitName());

        final org.bukkit.generator.WorldInfo worldInfo = new org.bukkit.craftbukkit.generator.CraftWorldInfo(
        		loadingInfo.data().bukkitName(),
        		org.bukkit.craftbukkit.util.CraftNamespacedKey.fromMinecraft(loadingInfo.info().dimensionKey().identifier()),
        		serverLevelData.genSettings().options().seed(),
        		serverLevelData.data().enabledFeatures(),
        		loadingInfo.info().environment(),
        		levelStem.type().value(),
        		levelStem.generator(),
        		server.registryAccess(),
        		loadingInfo.data().uuid()
        );

        if (biomeProvider == null && chunkGenerator != null) {
        	biomeProvider = chunkGenerator.getDefaultBiomeProvider(worldInfo);
        }
        
        /**
         * 
         	public ServerLevel(final MinecraftServer server, final Executor executor, final LevelStorageAccess levelStorage,
			final ServerLevelData levelData, final ResourceKey<Level> dimension, final LevelStem levelStem,
			final boolean isDebug, final long biomeZoomSeed, final List<CustomSpawner> customSpawners,
			final boolean tickTime) {
         */

        // loadingInfo.data().levelOverrides()
        
        ServerLevel serverLevel;
        if (loadingInfo.info().stemKey() == LevelStem.OVERWORLD) {
           serverLevel = new ServerLevel(
              server,
              server.executor,
              this.storageSource,
              loadingInfo.data().levelOverrides(),
              dimensionKey,
              levelStem,
              serverLevelData.data().isDebugWorld(),
              l,
              list,
              true//,
              // loadingInfo.info().stemKey(),
              //loadingInfo.info().environment(),
              //null
              /* ,
              Environment.getEnvironment(loadingInfo.dimension()),
              chunkGenerator,
              biomeProvider */
           );
           this.worldData = serverLevelData.data();
           // Singleplayer worlds keep the game mode stored in their level data.
           if ((Object)this instanceof DedicatedServer dedicated) {
               this.worldData.setGameType(dedicated.getProperties().gameMode.get());
           }
           // DimensionDataStorage dataStorage = serverLevel.getDataStorage();

           this.getServer().getScoreboard().load(((ScoreboardSaveData)savedDataStorage.computeIfAbsent(ScoreboardSaveData.TYPE)).getData());
           
           this.commandStorage = new CommandStorage(savedDataStorage);
           CraftServer.INSTANCE.scoreboardManager = new CraftScoreboardManager(server, serverLevel.getScoreboard());
           
           // server.stopwatches = this.savedDataStorage.computeIfAbsent(Stopwatches.TYPE);
        } else {
           List<CustomSpawner> spawners;
           
           // Note: add useDimensionTypeForCustomSpawners (default = false)
           
           if (false && levelStem.type().is(BuiltinDimensionTypes.OVERWORLD)) {
              spawners = list;
           } else {
              spawners = Collections.emptyList();
           }

           serverLevel = new ServerLevel(
              server,
              server.executor,
              this.storageSource,
              loadingInfo.data().levelOverrides(),
              dimensionKey,
              levelStem,
              this.worldData.isDebugWorld(),
              l,
              spawners,
              true
           );
        }

        this.addLevel(serverLevel);
        this.initWorld(serverLevel, serverLevelData, worldOptions);
    }

    /*
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;getChunkGenerator()Lnet/minecraft/world/gen/chunk/ChunkGenerator;"), method = "setupSpawn")
    private static void setupSpawn_BukkitGenerators(ServerWorld world, ServerWorldProperties swp, boolean bonusChest, boolean debugWorld, CallbackInfo ci) {
        // TODO Bukkit Generators
    }
    */

    /*
    @Shadow
    private static void setupSpawn(ServerWorld world, ServerWorldProperties swp, boolean bonusChest, boolean debugWorld) {
    }
    */
    
    @Shadow
    private static void setInitialSpawn( ServerLevel world, ServerLevelData worldProperties, boolean bonusChest, boolean debugWorld, LevelLoadListener loadProgress) {
    	// Shadowed
    }

    @Shadow
    public abstract ServerLevel findRespawnDimension();

    @Shadow
    public abstract WorldData getWorldData();

    @Shadow
    public abstract @Nullable ServerLevel getLevel(ResourceKey<Level> resourceKey);

    @Shadow
    private LevelData.RespawnData effectiveRespawnData;

    // Paper start - per world respawn data - read "server global" respawn data from overworld dimension reference
    @Inject(method = "updateEffectiveRespawnData", at = @At("HEAD"), cancellable = true)
    public void updateEffectiveRespawnDataPaper(CallbackInfo ci) {
        ServerLevel serverLevel = this.findRespawnDimension();
        LevelData.RespawnData respawnData = serverLevel.serverLevelData.getRespawnData();
        respawnData = ((LevelData_RespawnDataBridge)(Object)respawnData).cardboard$withLevel(serverLevel.dimension());
        // Paper end - per world respawn data - read "server global" respawn data from overworld dimension reference
        this.effectiveRespawnData = serverLevel.getWorldBorderAdjustedRespawnData(respawnData);
        ci.cancel();
    }

    @Inject(method = "findRespawnDimension", at = @At("HEAD"), cancellable = true)
    public void findRespawnDimensionPaper(CallbackInfoReturnable<ServerLevel> cir) {
        ResourceKey<Level> resourceKey = ((PrimaryLevelDataBridge)((net.minecraft.world.level.storage.PrimaryLevelData) this.getWorldData().overworldData())).cardboard$getRespawnDimension(); // Paper - per world respawn data - read "server global" respawn data from overworld dimension reference
        ServerLevel level = this.getLevel(resourceKey);
        cir.setReturnValue(level != null ? level : this.overworld());
    }
}
