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
package org.cardboardpowered.mixin;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.event.server.ServerLoadEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.impl.scheduler.BukkitSchedulerImpl;
import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinNetworkIo;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import it.unimi.dsi.fastutil.longs.LongIterator;
import me.isaiah.common.cmixin.IMixinChunkGenerator;
import net.minecraft.block.Block;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.ForcedChunkState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
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
    //@Shadow @Final public Executor workerExecutor;
   // @Shadow @Final public WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory;

    @Shadow public Map<RegistryKey<net.minecraft.world.World>, ServerWorld> worlds;
    @Shadow public ServerResourceManager serverResourceManager;
    @Shadow public LevelStorage.Session session;
    @Shadow private long timeReference;
    @Shadow public DataCommandStorage dataCommandStorage;
 //   @Shadow @Mutable SaveProperties saveProperties;
    @Shadow private int ticks;

    @Shadow public void initScoreboard(PersistentStateManager arg0) {}

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

    @Inject(at = @At("HEAD"), method = "getServerModName", remap=false, cancellable = true)
    public void getServerModName_cardboard(CallbackInfoReturnable<String> ci) {
        if (null != Bukkit.getServer())
            ci.setReturnValue("Cardboard (PaperMC+Fabric)");
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
     * Call WorldInitEvent
     * 
     * @author Cardboard
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerWorldProperties;getWorldBorder()Lnet/minecraft/world/border/WorldBorder$Properties;"), method = "createWorlds")
    public void onBeginCreateWorld(WorldGenerationProgressListener p, CallbackInfo ci) {
        System.out.println("DEBUG: onBeginCreateWorld");
        Collection<ServerWorld> worldz = this.worlds.values();
        ServerWorld world = worldz.toArray(new ServerWorld[0])[worldz.size()-1];

        CraftServer.INSTANCE.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(((IMixinWorld)world).getWorldImpl()));
    }

    /**
     * Enable plugins
     * Call WorldLoadEvent & ServerLoadEvent
     * 
     * @author Cardboard
     */
    @SuppressWarnings({ "resource", "deprecation" })
    @Inject(at = @At("TAIL"), method = "loadWorld")
    public void afterWorldLoad(CallbackInfo ci) {
        for (ServerWorld worldserver : ((MinecraftServer)(Object)this).getWorlds()) {
            this.loadSpawn(worldserver.getChunkManager().threadedAnvilChunkStorage.worldGenerationProgressListener, worldserver);
            CraftServer.INSTANCE.getPluginManager().callEvent(new org.bukkit.event.world.WorldLoadEvent(((IMixinWorld)worldserver).getWorldImpl()));
        }

        CraftServer.INSTANCE.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD);
        CraftServer.INSTANCE.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
        ((IMixinNetworkIo)(Object)getServer().getNetworkIo()).acceptConnections();

        CraftMagicNumbers.setupUnknownModdedMaterials();
        fixBukkitWorldEdit();
        BukkitFabricMod.isAfterWorldLoad = true;
    }

    public void updateDifficulty() {
        ((MinecraftServer)(Object)this).setDifficulty(((DedicatedServer)(Object)this).getProperties().difficulty, true);
    }


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
            HashMap<String, Material> moddedMaterials = CraftMagicNumbers.getModdedMaterials();

            if (moddedMaterials.size() > 0)
                BukkitFabricMod.LOGGER.info("Adding Modded blocks/items to WorldEdit registry...");
            for (String mid : moddedMaterials.keySet()) {
                try {
                    REGISTER_ITEM.invoke(REGISTRY_ITEM, "minecraft:" + mid.toLowerCase(), ITEM_TYPE.getConstructor(String.class).newInstance(mid));
                    REGISTER_BLOCK.invoke(REGISTRY_BLOCK, "minecraft:" + mid.toLowerCase(), BLOCK_TYPE.getConstructor(String.class).newInstance(mid));
                } catch (Exception e) {
                }
            }
            if (moddedMaterials.size() > 0)
                BukkitFabricMod.LOGGER.info("Added Modded blocks/items to WorldEdit registry.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadSpawn(WorldGenerationProgressListener worldloadlistener, ServerWorld worldserver) {
        this.forceTicks = true;

        LOGGER.info("Preparing start region for world " + worldserver.getRegistryKey().getValue());
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
            ForcedChunkState forcedchunk = (ForcedChunkState) worldserver.getPersistentStateManager().get(ForcedChunkState::fromNbt, "chunks");

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
        this.updateMobSpawnOptions_1_15_2();

        this.forceTicks = false;
    }

    private void updateMobSpawnOptions_1_15_2() {
        Iterator<ServerWorld> iterator = ((MinecraftServer)(Object)this).getWorlds().iterator();

        while (iterator.hasNext()) {
            ServerWorld worldserver = (ServerWorld) iterator.next();

            worldserver.setMobSpawnOptions(((MinecraftServer)(Object)this).isMonsterSpawningEnabled(),
                    ((MinecraftServer)(Object)this).shouldSpawnAnimals());
        }

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

    @Inject(at = @At("HEAD"), method = "shutdown")
    public void doStop(CallbackInfo ci) {
        synchronized(stopLock) {
            if (hasStopped) return;
            hasStopped = true;
        }

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
            worldProperties.setSpawnPos(BlockPos.ORIGIN.up(((IMixinChunkGenerator)chunkgenerator).IgetSpawnHeight(worldserver)), 0.0F);
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
            worldProperties.setSpawnPos(start.add(8, ((IMixinChunkGenerator)chunkgenerator).IgetSpawnHeight(worldserver), 8), 0.0F);
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