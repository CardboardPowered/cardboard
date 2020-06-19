package com.fungus_soft.bukkitfabric.mixin;

import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.fungus_soft.bukkitfabric.interfaces.IMixinThreadedAnvilChunkStorage;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorageMixin implements IMixinThreadedAnvilChunkStorage {

    @Shadow
    public WorldGenerationProgressListener worldGenerationProgressListener;

    @Override
    public WorldGenerationProgressListener getWorldGenerationProgressListener() {
        return worldGenerationProgressListener;
    }

}