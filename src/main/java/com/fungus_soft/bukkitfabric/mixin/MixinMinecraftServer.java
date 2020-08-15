package com.fungus_soft.bukkitfabric.mixin;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executor;
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

import com.fungus_soft.bukkitfabric.interfaces.IMixinMinecraftServer;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.WorldSaveHandler;

@Mixin(value=MinecraftServer.class)
public abstract class MixinMinecraftServer implements IMixinMinecraftServer {

    @Shadow
    @Final
    public DynamicRegistryManager.Impl registryManager;

    @Shadow
    @Final
    public WorldSaveHandler saveHandler;

    @Shadow
    private Map<RegistryKey<net.minecraft.world.World>, ServerWorld> worlds;

    @Shadow
    public ServerResourceManager serverResourceManager;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    public Executor workerExecutor;

    @Shadow
    public void initScoreboard(PersistentStateManager arg0) {}

    @Shadow
    public DataCommandStorage dataCommandStorage;

    public java.util.Queue<Runnable> processQueue = new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();

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

    /*@Override
    public void initWorld(ServerWorld world, LevelProperties prop, LevelInfo info) {

        World bukkit = ((IMixinWorld)world).getCraftWorld();
        world.getWorldBorder().load(prop);

        if (null != bukkit.getGenerator())
            bukkit.getPopulators().addAll(bukkit.getGenerator().getDefaultPopulators(bukkit));

        if (!prop.isInitialized()) {
            try {
                world.init(info);
                prop.setInitialized(true);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            prop.setInitialized(true);
        }
    }*/

    public MinecraftServer getServer() {
        return (MinecraftServer) (Object) this;
    }


    /*@SuppressWarnings("deprecation")
    @Overwrite
    public void loadWorld(String s, String s1, long i, LevelGeneratorType worldtype, JsonElement jsonelement) {
        setLoadingStage(new TranslatableText("menu.loadingLevel", new Object[0]));

        int worldCount = 3;
        for (int j = 0; j < worldCount; ++j) {
            ServerWorld world;
            LevelProperties worlddata;
            byte dimension = 0;

            if (j == 1) {
                if (Bukkit.getAllowNether()) dimension = -1;
                else continue;
            }

            if (j == 2) {
                if (Bukkit.getAllowEnd()) dimension = 1;
                else continue;
            }

            String worldType = org.bukkit.World.Environment.getEnvironment(dimension).toString().toLowerCase();
            String name = (dimension == 0) ? s : s + "_" + worldType;
            this.convertWorld(name); // Run conversion now

            org.bukkit.generator.ChunkGenerator gen = ((CraftServer)Bukkit.getServer()).getGenerator(name);
            LevelInfo worldsettings = new LevelInfo(i, getServer().getDefaultGameMode(), getServer().shouldGenerateStructures(), getServer().isHardcore(), worldtype);
            worldsettings.setGeneratorOptions(jsonelement);

            if (j == 0) {
                WorldSaveHandler worldnbtstorage = new WorldSaveHandler(Bukkit.getWorldContainer(), s1, getServer(), getServer().getDataFixer());
                worlddata = worldnbtstorage.readProperties();
                if (worlddata == null)
                    worlddata = new LevelProperties(worldsettings, s1);

                ((IMixinLevelProperties)(Object)worlddata).checkName(s1);
                loadWorldDataPacks(worldnbtstorage.getWorldDir(), worlddata);  
                WorldGenerationProgressListener worldloadlistener = CraftServer.server.worldGenerationProgressListenerFactory.create(11);

                world = new ServerWorld(getServer(), workerExecutor, worldnbtstorage, worlddata, DimensionType.OVERWORLD, profiler, worldloadlistener/*, org.bukkit.World.Environment.getEnvironment(dimension), gen); // TODO

                PersistentStateManager worldpersistentdata = world.getPersistentStateManager();
                initScoreboard(worldpersistentdata);
                // TODO ((CraftServer)Bukkit.getServer()).scoreboardManager = new org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager(this, world.getScoreboard());
                dataCommandStorage = new DataCommandStorage(worldpersistentdata);
            } else {
                String dim = "DIM" + dimension;

                File newWorld = new File(new File(name), dim);
                File oldWorld = new File(new File(s), dim);
                File oldLevelDat = new File(new File(s), "level.dat");

                if (!newWorld.isDirectory() && oldWorld.isDirectory() && oldLevelDat.isFile()) {
                    LOGGER.info("---- Migration of old " + worldType + " folder required ----");
                    LOGGER.info("Unfortunately due to the way that Minecraft implemented multiworld support, Bukkit requires that you move your " + worldType + " folder to a new location in order to operate correctly.");
                    LOGGER.info("We will move this folder for you, but it will mean that you need to move it back should you wish to stop using Bukkit in the future.");
                    LOGGER.info("Attempting to move " + oldWorld + " to " + newWorld + "...");

                    if (newWorld.exists()) {
                        LOGGER.warn("A file or folder already exists at " + newWorld + "!");
                        LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                    } else if (newWorld.getParentFile().mkdirs()) {
                        if (oldWorld.renameTo(newWorld)) {
                            LOGGER.info("Success! To restore " + worldType + " in the future, simply move " + newWorld + " to " + oldWorld);
                            // Migrate world data too.
                            try {
                                com.google.common.io.Files.copy(oldLevelDat, new File(new File(name), "level.dat"));
                                org.apache.commons.io.FileUtils.copyDirectory(new File(new File(s), "data"), new File(new File(name), "data"));
                            } catch (IOException exception) {
                                LOGGER.warn("Unable to migrate world data.");
                            }
                            LOGGER.info("---- Migration of old " + worldType + " folder complete ----");
                        } else {
                            LOGGER.warn("Could not move folder " + oldWorld + " to " + newWorld + "!");
                            LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                        }
                    } else {
                        LOGGER.warn("Could not create path for " + newWorld + "!");
                        LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                    }
                }

                WorldSaveHandler worldnbtstorage = new WorldSaveHandler(Bukkit.getWorldContainer(), name, getServer(), getServer().getDataFixer());
                // world =, b0 to dimension, s1 to name, added Environment and gen
                worlddata = worldnbtstorage.readProperties();
                if (worlddata == null)
                    worlddata = new LevelProperties(worldsettings, name);

               ((IMixinLevelProperties)(Object)worlddata).checkName(name);
                WorldGenerationProgressListener worldloadlistener = CraftServer.server.worldGenerationProgressListenerFactory.create(11);
                // TODO
                world = new SecondaryServerWorld(worlds.get(DimensionType.OVERWORLD), getServer(), workerExecutor, worldnbtstorage, DimensionType.byRawId(dimension), profiler, worldloadlistener/*, worlddata, org.bukkit.World.Environment.getEnvironment(dimension), gen);
            }

            this.initWorld(world, worlddata, worldsettings);
            Bukkit.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(((IMixinWorld)world).getCraftWorld()));

            worlds.put(world.getDimension().getType(), world);
            getServer().getPlayerManager().setMainWorld(world);

            if (worlddata.getCustomBossEvents() != null)
                getServer().getBossBarManager().fromTag(worlddata.getCustomBossEvents());
        }
        getServer().setDifficulty(getServer().getDefaultDifficulty(), true);

        for (ServerWorld worldserver : getServer().getWorlds()) {
            prepareStartRegion(worldserver.getChunkManager().threadedAnvilChunkStorage.worldGenerationProgressListener, worldserver);
            Bukkit.getPluginManager().callEvent(new org.bukkit.event.world.WorldLoadEvent(((IMixinWorld)worldserver).getCraftWorld()));
        }

        CraftServer bukkit = ((CraftServer)Bukkit.getServer());

        bukkit.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD);
        bukkit.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
        ((IMixinNetworkIo)(Object)getServer().getNetworkIo()).acceptConnections();
    }

    public void prepareStartRegion(WorldGenerationProgressListener worldloadlistener, ServerWorld worldserver) {
        if (!(((IMixinWorld)worldserver).getCraftWorld()).getKeepSpawnInMemory())
            return;

        setLoadingStage(new TranslatableText("menu.generatingTerrain", new Object[0]));
        this.forceTicks = true;

        LOGGER.info("Preparing start region for dimension '{}'/{}", worldserver.getLevelName(), worldserver.getDimension()); // CraftBukkit
        BlockPos blockposition = worldserver.getSpawnPos();

        worldloadlistener.start(new ChunkPos(blockposition));
        ServerChunkManager chunkproviderserver = worldserver.getChunkManager();

        chunkproviderserver.getLightingProvider().setTaskBatchSize(500);
        this.timeReference = Util.getMeasuringTimeMs();
        chunkproviderserver.addTicket(ChunkTicketType.START, new ChunkPos(blockposition), 11, Unit.INSTANCE);

        // TODO Bukkit4Fabric: this never stops looping!
        while (chunkproviderserver.getTotalChunksLoadedCount() != 441) {
            //LOGGER.info(chunkproviderserver.getLoadedChunkCount());
            this.executeModerately();
        }

        executeModerately();

        if (true) {
            DimensionType dimensionmanager = worldserver.getDimension();
            ForcedChunkState forcedchunk = (ForcedChunkState) worldserver.getPersistentStateManager().get(ForcedChunkState::new, "chunks");

            if (forcedchunk != null) {
                LongIterator longiterator = forcedchunk.getChunks().iterator();

                while (longiterator.hasNext()) {
                    System.out.println("ABCDEFG ------------");
                    long i = longiterator.nextLong();
                    ChunkPos chunkcoordintpair = new ChunkPos(i);
                    worldserver.getChunkManager().setChunkForced(chunkcoordintpair, true);
                }
            }
        }
        this.executeModerately();
        worldloadlistener.stop();
        chunkproviderserver.getLightingProvider().setTaskBatchSize(5);

        this.forceTicks = false;
    }*/

    @Inject(at = @At("TAIL"), method = "loadWorld")
    public void afterWorldLoad(CallbackInfo ci) {
        CraftServer bukkit = ((CraftServer)Bukkit.getServer());

        bukkit.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD);
        bukkit.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
    }

}