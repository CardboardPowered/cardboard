package com.fungus_soft.bukkitfabric.interfaces;

import net.minecraft.server.WorldGenerationProgressListener;

public interface IMixinThreadedAnvilChunkStorage {

    public WorldGenerationProgressListener getWorldGenerationProgressListener();

}