package org.cardboardpowered.mixin.world;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.cardboardpowered.impl.block.CapturedBlockState;
import org.cardboardpowered.impl.world.WorldImpl;

import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ServerWorldProperties;

@Mixin(World.class)
public class MixinWorld implements IMixinWorld {

    private WorldImpl bukkit;

    public boolean captureBlockStates = false;
    public boolean captureTreeGeneration = false;
    public Map<BlockPos, CapturedBlockState> capturedBlockStates = new HashMap<>();

    @Override
    public Map<BlockPos, CapturedBlockState> getCapturedBlockStates_BF() {
        return capturedBlockStates;
    }

    @Override
    public boolean isCaptureBlockStates_BF() {
        return captureBlockStates;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(MutableWorldProperties a, RegistryKey<?> b, DimensionType d, Supplier<Boolean> e, boolean f, boolean g, long h, CallbackInfo ci){
        if (!((Object)this instanceof ServerWorld)) {
            System.out.println("CLIENT WORLD!");
            return;
        }

        ServerWorld nms = ((ServerWorld)(Object)this);
        String name = ((ServerWorldProperties) nms.getLevelProperties()).getLevelName();

        File fi = new File(name + "_the_end");
        File van = new File(new File(name), "DIM1");

        if (fi.exists()) {
            File dim = new File(fi, "DIM1");
            if (dim.exists()) {
                BukkitFabricMod.LOGGER.info("------ Migration of world file: " + name + "_the_end !");
                BukkitFabricMod.LOGGER.info("Cardboard is currently migrating the world back to the vanilla format!");
                BukkitFabricMod.LOGGER.info("Do to the differences between Spigot & Fabric world folders, we require migration.");
                if (dim.renameTo(van)) {
                    BukkitFabricMod.LOGGER.info("---- Migration of old bukkit format folder complete ----");
                } else {
                    BukkitFabricMod.LOGGER.info("---- Migration of old bukkit format folder FAILED! ----");
                    BukkitFabricMod.LOGGER.info("Please follow these instructions: https://s.cardboardpowered.org/world-migration-info");
                }
                fi.delete();
            }
        }
        
        File fi2 = new File(name + "_nether");
        File van2 = new File(new File(name), "DIM-1");

        if (fi2.exists()) {
            File dim = new File(fi2, "DIM-1");
            if (dim.exists()) {
                BukkitFabricMod.LOGGER.info("------ Migration of world file: " + fi2.getName() + " !");
                BukkitFabricMod.LOGGER.info("Cardboard is currently migrating the world back to the vanilla format!");
                BukkitFabricMod.LOGGER.info("Do to the differences between Spigot & Fabric world folders, we require migration.");
                if (dim.renameTo(van2)) {
                    BukkitFabricMod.LOGGER.info("---- Migration of old bukkit format folder complete ----");
                } else {
                    BukkitFabricMod.LOGGER.info("---- Migration of old bukkit format folder FAILED! ----");
                    BukkitFabricMod.LOGGER.info("Please follow these instructions: https://s.cardboardpowered.org/world-migration-info");
                }
                fi.delete();
            }
        }

        if (CraftServer.INSTANCE.worlds.containsKey(name)) {
            if (nms.getRegistryKey() == World.NETHER) {
                name = name + "_nether";
                // Keep empty directory to fool plugins, ex. Multiverse.
                fi2.mkdirs();
            }
            if (nms.getRegistryKey() == World.END) {
                name = name + "_the_end";
                fi.mkdirs();
            }

            this.bukkit = new WorldImpl(name, nms);
            CraftServer.INSTANCE.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(((IMixinWorld)nms).getWorldImpl()));
        } else {
            this.bukkit = new WorldImpl(name, nms);
        }
        System.out.println("WORLD NAME: " + name);
        ((CraftServer)Bukkit.getServer()).addWorldToMap(getWorldImpl());
    }

    @Override
    public WorldImpl getWorldImpl() {
        return bukkit;
    }

    @Inject(at = @At("HEAD"), method = "setBlockState", cancellable = true)
    public void setBlockState1(BlockPos blockposition, BlockState iblockdata, int i, CallbackInfoReturnable<Boolean> ci) {
        // TODO 1.17ify: if (!ServerWorld.isOutOfBuildLimitVertically(blockposition)) {
            WorldChunk chunk = ((ServerWorld)(Object)this).getWorldChunk(blockposition);
            boolean captured = false;
            if (this.captureBlockStates && !this.capturedBlockStates.containsKey(blockposition)) {
                CapturedBlockState blockstate = CapturedBlockState.getBlockState((World)(Object)this, blockposition, i);
                this.capturedBlockStates.put(blockposition.toImmutable(), blockstate);
                captured = true;
            }
        //}
    }

    @Override
    public void setCaptureBlockStates_BF(boolean b) {
        this.captureBlockStates = b;
    }

}