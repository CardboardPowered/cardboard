package org.cardboardpowered.mixin.world;

import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.cardboardpowered.impl.block.CapturedBlockState;
import org.cardboardpowered.impl.world.WorldImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(World.class)
public abstract class MixinWorld implements IMixinWorld {

    @Shadow public WorldChunk getWorldChunk(BlockPos pos) {return null;}
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

    // protected World(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> registryEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {


    // Note: moved to use ServerWorldInitEvent
    // @Inject(method = "<init>", at = @At("TAIL"))
    // public void init(MutableWorldProperties a, RegistryKey<?> b, DimensionType d, Supplier<Boolean> e, boolean f, boolean g, long h, CallbackInfo ci){
    // public void init(MutableWorldProperties a, RegistryKey<?> b, RegistryEntry<DimensionType> registryEntry, Supplier<Profiler> profiler, boolean f, boolean g, long h, CallbackInfo ci){
    //    System.out.println("MixnWorld.init");
    // }

    @Override
    public WorldImpl getWorldImpl() {
        return bukkit;
    }

    @Override
    public void set_bukkit_world(WorldImpl world) {
        this.bukkit = world;
    }

    @Inject(at = @At("HEAD"), method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z")
    public void setBlockState1(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        // TODO 1.17ify: if (!ServerWorld.isOutOfBuildLimitVertically(blockposition)) {
            WorldChunk chunk = getWorldChunk(pos);
            boolean captured = false;
            if (this.captureBlockStates && !this.capturedBlockStates.containsKey(pos)) {
                CapturedBlockState blockstate = CapturedBlockState.getBlockState((World)(Object)this, pos, flags);
                this.capturedBlockStates.put(pos.toImmutable(), blockstate);
                captured = true;
            }
        //}
    }

    @Override
    public void setCaptureBlockStates_BF(boolean b) {
        this.captureBlockStates = b;
    }

}
