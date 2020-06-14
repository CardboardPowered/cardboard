package com.fungus_soft.bukkitfabric.mixin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.server.ServerLoadEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import com.fungus_soft.bukkitfabric.interfaces.IMixinLevelProperties;
import com.fungus_soft.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.fungus_soft.bukkitfabric.interfaces.IMixinNetworkIo;
import com.fungus_soft.bukkitfabric.interfaces.IMixinServerWorld;
import com.fungus_soft.bukkitfabric.interfaces.IMixinThreadExecutor;
import com.fungus_soft.bukkitfabric.interfaces.IMixinThreadedAnvilChunkStorage;
import com.google.gson.JsonElement;

import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.SharedConstants;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.SecondaryServerWorld;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.DisableableProfiler;
import net.minecraft.world.ForcedChunkState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;

@Mixin(value=MinecraftServer.class, priority=999) // priority=999 because Mixin does not like to inject into overwitten methods if both have same priority
public abstract class MinecraftServerMixin implements IMixinMinecraftServer {

    private static int currentTick = (int) (System.currentTimeMillis() / 50);
    private static final int SAMPLE_INTERVAL = 100;
    public final double[] recentTps = new double[3];

    @Shadow
    private Map<DimensionType, ServerWorld> worlds;

    @Shadow
    private WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory;

    @Shadow
    public void upgradeWorld(String name) {
    }

    @Shadow
    public CommandManager commandManager;

    @Shadow
    public boolean setupServer() {
        return false;
    }

    @Shadow
    private long timeReference = Util.getMeasuringTimeMs();

    @Shadow
    private ServerMetadata metadata;

    @Shadow
    private long field_4557; // lastOverloadTime

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    private boolean profilerStartQueued;

    @Shadow
    protected void tick(BooleanSupplier shouldKeepTicking) {
    }

    @Shadow
    private boolean shouldKeepTicking() {
        return false;
    }

    @Shadow
    private boolean field_19249;

    @Shadow
    private long field_19248;

    @Shadow
    private DisableableProfiler profiler;

    @Shadow
    protected void method_16208() {
    }

    @Shadow
    protected void setCrashReport(CrashReport crashReport) {
    }

    @Shadow
    private volatile boolean loading;

    @Shadow
    private boolean stopped;

    @Shadow
    protected void shutdown() {
    }

    @Shadow
    @Final
    public Executor workerExecutor;

    @Shadow
    public void initScoreboard(PersistentStateManager arg0) {}

    @Shadow
    protected synchronized void setLoadingStage(Text loadingStage) {}

    @Shadow
    public void loadWorldDataPacks(File worldDir, LevelProperties levelProperties) {}

    @Shadow
    public DataCommandStorage dataCommandStorage;

    public java.util.Queue<Runnable> processQueue = new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();

    private boolean forceTicks;

    @Overwrite
    public String getServerModName() {
        return "Fabric + Bukkit4Fabric";
    }

    @Override
    public Map<DimensionType, ServerWorld> getWorldMap() {
        return worlds;
    }

    @Override
    public void convertWorld(String name) {
        upgradeWorld(name);
    }

    @Override
    public WorldGenerationProgressListenerFactory getWorldGenerationProgressListenerFactory() {
        return worldGenerationProgressListenerFactory;
    }

    @Override
    public Queue<Runnable> getProcessQueue() {
        return processQueue;
    }

    @Override
    public CommandManager setCommandManager(CommandManager commandManager) {
        return (this.commandManager = commandManager);
    }

    @Override
    public void initWorld(ServerWorld world, LevelProperties prop, LevelInfo info) {
        World bukkit = ((IMixinServerWorld)world).getCraftWorld();
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
    }

    public MinecraftServer getServer() {
        return (MinecraftServer) (Object) this;
    }

    private static double calcTps(double avg, double exp, double tps) {
        return (avg * exp) + (tps * (1 - exp));
    }

    /**
     * Optimized Tick Loop for Fabric
     */
    @Overwrite
    public void run() {
        try {
            if (this.setupServer()) {
                this.timeReference = Util.getMeasuringTimeMs();
                this.metadata.setDescription(new LiteralText(getServer().getServerMotd()));
                this.metadata.setVersion(new ServerMetadata.Version(SharedConstants.getGameVersion().getName(), SharedConstants.getGameVersion().getProtocolVersion()));
                getServer().setFavicon(this.metadata);
 
                Arrays.fill(recentTps, 20);
                long curTime, tickSection = Util.getMeasuringTimeMs(), tickCount = 1;
                while (getServer().isRunning()) {
                    long i = (curTime = Util.getMeasuringTimeMs()) - this.timeReference;

                    if (i > 5000L && this.timeReference - this.field_4557 >= 30000L) { // CraftBukkit
                        long j = i / 50L;

                        LOGGER.warn("Can't keep up! Is the server overloaded? Running " + i + "ms or " + j + " ticks behind");
                        this.timeReference += j * 50L;
                        this.field_4557 = this.timeReference;
                    }

                    if ( tickCount++ % SAMPLE_INTERVAL == 0 ) {
                        double currentTps = 1E3 / ( curTime - tickSection ) * SAMPLE_INTERVAL;
                        recentTps[0] = calcTps(recentTps[0], 0.92, currentTps);
                        recentTps[1] = calcTps(recentTps[1], 0.9835, currentTps);
                        recentTps[2] = calcTps(recentTps[2], 0.9945, currentTps);
                        tickSection = curTime;
                    }

                    currentTick = (int) (System.currentTimeMillis() / 50); // CraftBukkit
                    this.timeReference += 50L;
                    if (this.profilerStartQueued) {
                        this.profilerStartQueued = false;
                        this.profiler.getController().enable();
                    }
                    this.profiler.startTick();
                    this.profiler.push("tick");
                    this.tick(this::shouldKeepTicking);
                    this.profiler.swap("nextTickWait");
                    this.field_19249 = true;
                    this.field_19248 = Math.max(Util.getMeasuringTimeMs() + 50L, this.timeReference);
                    this.method_16208();
                    this.profiler.pop();
                    this.profiler.endTick();
                    this.loading = true;
                }
            } else this.setCrashReport(null);
        } catch (Throwable throwable) {
            LOGGER.error("Encountered an unexpected exception", throwable);
            CrashReport crashReport = getServer().populateCrashReport((throwable instanceof CrashException) ? ((CrashException)throwable).getReport() : new CrashReport("Exception in server tick loop", throwable));

            File file = new File(new File(getServer().getRunDirectory(), "crash-reports"), "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.txt");
            LOGGER.error(crashReport.writeToFile(file) ? ("This crash report has been saved to: " + file.getAbsolutePath()) : "We were unable to save this crash report");
            this.setCrashReport(crashReport);
        } finally {
            try {
                this.stopped = true;
                this.shutdown();
            } catch (Throwable throwable) {
                LOGGER.error("Exception stopping the server", throwable);
            } finally {System.exit(1);}
        }
    }

    @SuppressWarnings("deprecation")
    @Overwrite
    public void loadWorld(String s, String s1, long i, LevelGeneratorType worldtype, JsonElement jsonelement) {
        setLoadingStage(new TranslatableText("menu.loadingLevel", new Object[0]));

        int worldCount = 3;
        for (int j = 0; j < worldCount; ++j) {
            ServerWorld world;
            LevelProperties worlddata;
            byte dimension = 0;

            if (j == 1) {
                if (Bukkit.getAllowNether())
                    dimension = -1;
                else continue;
            }

            if (j == 2) {
                if (Bukkit.getAllowEnd())
                    dimension = 1;
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
                WorldGenerationProgressListener worldloadlistener = worldGenerationProgressListenerFactory.create(11);

                world = new ServerWorld(getServer(), workerExecutor, worldnbtstorage, worlddata, DimensionType.OVERWORLD, profiler, worldloadlistener/*, org.bukkit.World.Environment.getEnvironment(dimension), gen*/); // TODO

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
                WorldGenerationProgressListener worldloadlistener = worldGenerationProgressListenerFactory.create(11);
                // TODO
                world = new SecondaryServerWorld(worlds.get(DimensionType.OVERWORLD), getServer(), workerExecutor, worldnbtstorage, DimensionType.byRawId(dimension), profiler, worldloadlistener/*, worlddata, org.bukkit.World.Environment.getEnvironment(dimension), gen*/);
            }

            this.initWorld(world, worlddata, worldsettings);
            Bukkit.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(((IMixinServerWorld)world).getCraftWorld()));

            worlds.put(world.getDimension().getType(), world);
            getServer().getPlayerManager().setMainWorld(world);

            if (worlddata.getCustomBossEvents() != null)
                getServer().getBossBarManager().fromTag(worlddata.getCustomBossEvents());
        }
        getServer().setDifficulty(getServer().getDefaultDifficulty(), true);

        for (ServerWorld worldserver : getServer().getWorlds()) {
            prepareStartRegion(((IMixinThreadedAnvilChunkStorage)(Object)worldserver.getChunkManager().threadedAnvilChunkStorage).getWorldGenerationProgressListener(), worldserver);
            Bukkit.getPluginManager().callEvent(new org.bukkit.event.world.WorldLoadEvent(((IMixinServerWorld)worldserver).getCraftWorld()));
        }

        CraftServer bukkit = ((CraftServer)Bukkit.getServer());

        bukkit.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD);
        bukkit.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
        ((IMixinNetworkIo)(Object)getServer().getNetworkIo()).acceptConnections();
    }

    public void prepareStartRegion(WorldGenerationProgressListener worldloadlistener, ServerWorld worldserver) {
        if (!(((IMixinServerWorld)worldserver).getCraftWorld()).getKeepSpawnInMemory())
            return;

        setLoadingStage(new TranslatableText("menu.generatingTerrain", new Object[0]));
        this.forceTicks = true;

        LOGGER.info("Preparing start region for dimension '{}'/{}", worldserver.getLevelProperties().getLevelName(), worldserver.getDimension().getType()); // CraftBukkit
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
            DimensionType dimensionmanager = worldserver.getDimension().getType();
            ForcedChunkState forcedchunk = (ForcedChunkState) worldserver.getPersistentStateManager().get(ForcedChunkState::new, "chunks");

            if (forcedchunk != null) {
                ServerWorld worldserver1 = getServer().getWorld(dimensionmanager);
                LongIterator longiterator = forcedchunk.getChunks().iterator();

                while (longiterator.hasNext()) {
                    System.out.println("ABCDEFG ------------");
                    long i = longiterator.nextLong();
                    ChunkPos chunkcoordintpair = new ChunkPos(i);
                    worldserver1.getChunkManager().setChunkForced(chunkcoordintpair, true);
                }
            }
        }
        this.executeModerately();
        worldloadlistener.stop();
        chunkproviderserver.getLightingProvider().setTaskBatchSize(5);

        this.forceTicks = false;
    }

    private void executeModerately() {
        ((IMixinThreadExecutor)(Object)this).runTasks();
        java.util.concurrent.locks.LockSupport.parkNanos("executing tasks", 1000L);
    }

}