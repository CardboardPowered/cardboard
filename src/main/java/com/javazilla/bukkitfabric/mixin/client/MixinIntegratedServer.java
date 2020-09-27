package com.javazilla.bukkitfabric.mixin.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.PluginLoadOrder;
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
import com.javazilla.bukkitfabric.BukkitLogger;
import com.javazilla.bukkitfabric.interfaces.IMixinLevelProperties;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import com.javazilla.bukkitfabric.mixin.MixinMinecraftServer;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.dedicated.DedicatedPlayerManager;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.util.UserCache;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.util.registry.DynamicRegistryManager.Impl;
import net.minecraft.village.ZombieSiegeManager;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.WanderingTraderManager;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
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
import net.minecraft.world.level.storage.LevelStorage.Session;
import net.minecraft.server.dedicated.PendingServerCommand;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.server.integrated.IntegratedPlayerManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;

@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer extends MixinMinecraftServer {

    //public MinecraftDedicatedServer dedicated;

    public MixinIntegratedServer(String string) {
        super(string);
    }

    @Inject(at = @At(value = "TAIL"), method = "<init>", cancellable = true)
    private void init(Thread serverThread, MinecraftClient client, Impl registryManager, Session session, ResourcePackManager resourcePackManager, ServerResourceManager serverResourceManager, SaveProperties saveProperties, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        System.out.println("TEST TEST TEST TEST TEST TEST TEST TEST TEST");
        Path path = Paths.get("server.properties", new String[0]);
        ServerPropertiesLoader serverPropertiesLoader = new ServerPropertiesLoader(registryManager, path);
        serverPropertiesLoader.store();
        //dedicated = new MinecraftDedicatedServer(serverThread, registryManager, session, resourcePackManager, serverResourceManager, saveProperties, serverPropertiesLoader, Schemas.getFixer(), minecraftSessionService, gameProfileRepository, userCache, worldGenerationProgressListenerFactory);
        CraftServer.server = (IntegratedServer)(Object)this;

        //dedicated.createGui();

        ci.cancel();
    }


    @Inject(at = @At(value = "HEAD"), method = "setupServer()Z")
    private void initVar(CallbackInfoReturnable<Boolean> callbackInfo) {
        //Runtime.getRuntime().addShutdownHook(new org.bukkit.craftbukkit.util.ServerShutdownThread((IntegratedServer)(Object)this));
    }

    @Inject(at = @At(value = "HEAD"), method = "setupServer()Z", cancellable = true) // TODO keep ordinal updated
    private void init(CallbackInfoReturnable<Boolean> ci) {
        BukkitLogger.getLogger().info("  ____          _     _     _  _    ");
        BukkitLogger.getLogger().info(" |  _ \\        | |   | |   (_)| |   ");
        BukkitLogger.getLogger().info(" | |_) | _   _ | | __| | __ _ | |_  ");
        BukkitLogger.getLogger().info(" |  _ < | | | || |/ /| |/ /| || __| ");
        BukkitLogger.getLogger().info(" | |_) || |_| ||   < |   < | || |_  ");
        BukkitLogger.getLogger().info(" |____/  \\__,_||_|\\_\\|_|\\_\\|_| \\__| ");
        BukkitLogger.getLogger().info("Experimental Client Support");

        ((IntegratedServer)(Object)this).setPlayerManager(new IntegratedPlayerManager((IntegratedServer)(Object)this, registryManager, saveHandler));
        Bukkit.setServer(new CraftServer(CraftServer.server));

        Bukkit.getLogger().info("Loading Bukkit plugins...");
        File pluginsDir = new File("plugins");
        pluginsDir.mkdir();

        CraftServer s = ((CraftServer)Bukkit.getServer());
        if (CraftServer.server == null) CraftServer.server = (IntegratedServer)(Object)this;

        s.loadPlugins();
        s.enablePlugins(PluginLoadOrder.STARTUP);
        
        Bukkit.getLogger().info("");
        //ci.cancel();
    }

    /**
     * @reason Bukkit's Custom Multiworld handling
     * @author Bukkit4Fabric
     */
    @SuppressWarnings({ "deprecation", "resource", "unchecked", "rawtypes", "unused" })
    @Override
    public void loadWorld() {
        System.out.println("TEST TEST@ TEST@ TEST@");
        int worldCount = 3;

        for (int worldId = 0; worldId < worldCount; ++worldId) {
            System.out.println("A");
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
            System.out.println("B");

            String worldType = org.bukkit.World.Environment.getEnvironment(dimension).toString().toLowerCase();
            String s = this.session.getDirectoryName();
            String name = (dimension == 0) ? s : s + "_" + worldType;
            LevelStorage.Session worldSession;
            System.out.println("C");
            if (dimension == 0) {
                worldSession = this.session;
            } else {
                String dim = "DIM" + dimension;

                System.out.println("D");
                try {
                    worldSession = LevelStorage.create(CraftServer.INSTANCE.getWorldContainer().toPath()).createSession(name);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                System.out.println("E");
                MinecraftServer.convertLevel(worldSession); // Run conversion now
                System.out.println("F");
            }

            org.bukkit.generator.ChunkGenerator gen = CraftServer.INSTANCE.getGenerator(name);

            DynamicRegistryManager.Impl iregistrycustom_dimension = this.registryManager;
            System.out.println("G");
            RegistryOps<Tag> registryreadops = RegistryOps.of((DynamicOps) NbtOps.INSTANCE, this.serverResourceManager.getResourceManager(), iregistrycustom_dimension);
            worlddata = (LevelProperties) worldSession.readLevelProperties((DynamicOps) registryreadops, CraftServer.method_29735(CraftServer.server.dataPackManager));
            System.out.println("H");
            if (worlddata == null) {
                System.out.println("I");
                LevelInfo worldsettings;
                GeneratorOptions generatorsettings;

                SaveProperties dedicatedserverproperties = ((MinecraftServer)(Object) this).getSaveProperties();
                System.out.println("J");

                worldsettings = new LevelInfo(dedicatedserverproperties.getLevelName(), dedicatedserverproperties.getGameMode(), dedicatedserverproperties.isHardcore(), dedicatedserverproperties.getDifficulty(), false, new GameRules(), CraftServer.method_29735(CraftServer.server.dataPackManager));
                generatorsettings = dedicatedserverproperties.getGeneratorOptions();
                System.out.println("K");

                worlddata = new LevelProperties(worldsettings, generatorsettings, Lifecycle.stable());
                System.out.println("L");
            }
            ((IMixinLevelProperties)worlddata).checkName(name); // CraftBukkit - Migration did not rewrite the level.dat; This forces 1.8 to take the last loaded world as respawn (in this case the end)
            System.out.println("M");
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
            System.out.println("N");

            if (worlddimension == null) {
                dimensionmanager = (DimensionType) this.registryManager.getDimensionTypes().getOrThrow(DimensionType.OVERWORLD_REGISTRY_KEY);
                chunkgenerator = GeneratorOptions.createOverworldGenerator(this.registryManager.get(Registry.BIOME_KEY), this.registryManager.get(Registry.NOISE_SETTINGS_WORLDGEN), (new Random()).nextLong());
            } else {
                dimensionmanager = worlddimension.getDimensionType();
                chunkgenerator = worlddimension.getChunkGenerator();
            }
            System.out.println("O");

            RegistryKey<World> worldKey = RegistryKey.of(Registry.DIMENSION, dimensionKey.getValue());
            System.out.println((null == worldKey) + " NULL");

            if (worldId == 0) {
                //this.saveProperties = worlddata;
                //this.saveProperties.setGameMode(((MinecraftServer)(Object) this).getSaveProperties().getGameMode()); // From DedicatedServer.init
                System.out.println("P");

                WorldGenerationProgressListener worldloadlistener = this.worldGenerationProgressListenerFactory.create(11);

                world = new ServerWorld((MinecraftServer)(Object)this, this.workerExecutor, worldSession, iworlddataserver, worldKey, dimensionmanager, worldloadlistener, chunkgenerator, flag, j, list, true/*, org.bukkit.World.Environment.getEnvironment(dimension), gen*/);
                PersistentStateManager worldpersistentdata = world.getPersistentStateManager();
                this.initScoreboard(worldpersistentdata);
                System.out.println("Q");
                // TODO this.server.scoreboardManager = new org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager(this, world.getScoreboard());
                setDataCommandStorage(new DataCommandStorage(worldpersistentdata));
            } else {
                WorldGenerationProgressListener worldloadlistener = this.worldGenerationProgressListenerFactory.create(11);
                world = new ServerWorld((MinecraftServer)(Object)this, this.workerExecutor, worldSession, iworlddataserver, worldKey, dimensionmanager, worldloadlistener, chunkgenerator, flag, j, ImmutableList.of(), true/*, org.bukkit.World.Environment.getEnvironment(dimension), gen*/);
            }
            System.out.println("R");

            worlddata.addServerBrand(this.getServerModName(), true);
            this.initWorld(world, worlddata, saveProperties, worlddata.getGeneratorOptions());
            CraftServer.INSTANCE.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(((IMixinWorld)world).getCraftWorld()));

            this.worlds.put(world.getRegistryKey(), world);
            ((MinecraftServer)(Object)this).getPlayerManager().setMainWorld(world);

            if (worlddata.getCustomBossEvents() != null)
                ((MinecraftServer)(Object)this).getBossBarManager().fromTag(worlddata.getCustomBossEvents());
        }
        this.method_27731();
        for (ServerWorld worldserver : ((MinecraftServer)(Object)this).getWorlds()) {
            this.loadSpawn(worldserver.getChunkManager().threadedAnvilChunkStorage.worldGenerationProgressListener, worldserver);
            CraftServer.INSTANCE.getPluginManager().callEvent(new org.bukkit.event.world.WorldLoadEvent(((IMixinWorld)worldserver).getCraftWorld()));
        }

        CraftServer.INSTANCE.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD);
        CraftServer.INSTANCE.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
       // ((IMixinNetworkIo)(Object)getServer().getNetworkIo()).acceptConnections();
    }

    @Inject(at = @At("TAIL"), method = "shutdown")
    public void killProcess(CallbackInfo ci) {
        //dedicated.exit();
        BukkitLogger.getLogger().info("Goodbye!");
        System.exit(0);
    }

}