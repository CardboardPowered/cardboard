package org.cardboardpowered.mixin.world;

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
        if (CraftServer.INSTANCE.worlds.containsKey(name)) {
            if (nms.getRegistryKey() == World.NETHER) name = name + "_nether";
            if (nms.getRegistryKey() == World.END)    name = name + "_the_end";
        }
        this.bukkit = new WorldImpl(name, nms);
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