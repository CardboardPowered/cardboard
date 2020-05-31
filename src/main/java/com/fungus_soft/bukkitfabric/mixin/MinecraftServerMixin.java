package com.fungus_soft.bukkitfabric.mixin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.server.ServerLoadEvent.LoadType;
import org.bukkit.plugin.PluginLoadOrder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fungus_soft.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.fungus_soft.bukkitfabric.interfaces.IMixinServerWorld;
import com.google.gson.JsonElement;

import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.profiler.DisableableProfiler;
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
    private static final Logger LOGGER = LogManager.getLogger();

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

    public java.util.Queue<Runnable> processQueue = new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();


    @Inject(at = @At(value = "TAIL"), method = "loadWorld")
    private void finish(String worldName, String serverName, long seed, LevelGeneratorType generatorType, JsonElement generatorSettings, CallbackInfo callbackInfo) {
        CraftServer s = ((CraftServer)Bukkit.getServer());

        s.enablePlugins(PluginLoadOrder.POSTWORLD);
        s.getPluginManager().callEvent(new ServerLoadEvent(LoadType.STARTUP));
    }

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

        if (bukkit.getGenerator() != null)
            bukkit.getPopulators().addAll(bukkit.getGenerator().getDefaultPopulators(bukkit));

        if (!prop.isInitialized()) {
            try {
                world.init(info);
                if (prop.getGeneratorType() == LevelGeneratorType.DEBUG_ALL_BLOCK_STATES) {
                    // TODO Bukkit4Fabric: we should be fine with not adding this method, as who would run a server with a debug world
                    // this.a(worlddata);
                }

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
     * This ports "0044-Highly-Optimized-Tick-Loop.patch"
     *
     * @author Bukkit4Fabric - https://curseforge.com/minecraft/mc-mods/bukkit
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

                        LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", i, j);
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
                    // Spigot end

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
            LOGGER.error(crashReport.writeToFile(file) ? ("This crash report has been saved to: " + file.getAbsolutePath()) : "We were unable to save this crash report to disk.");
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

}