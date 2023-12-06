package org.cardboardpowered.mixin.world;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Maps;
import com.javazilla.bukkitfabric.interfaces.IMixinChunk;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.gen.chunk.BlendingData;

@Mixin(Chunk.class)
public abstract class MixinChunk implements IMixinChunk {

    public Registry<Biome> biomeRegistry;
	
    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(ChunkPos chunkPos, UpgradeData upgradeData, HeightLimitView levelHeightAccessor, Registry<Biome>  registry, long l, ChunkSection[] levelChunkSections, BlendingData blendingData, CallbackInfo ci) {
        this.biomeRegistry = registry;
    }
    
	@Shadow
	public final Map<BlockPos, BlockEntity> blockEntities = Maps.newHashMap();

	@Override
	public Map<BlockPos, BlockEntity> cardboard_getBlockEntities() {
		return blockEntities;
	}
	
    @Override
    public Registry<Biome> bridge$biomeRegistry() {
        return biomeRegistry;
    }

}
