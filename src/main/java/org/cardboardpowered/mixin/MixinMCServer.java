package org.cardboardpowered.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.server.MinecraftServer;
// import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.WorldSaveHandler;

@Mixin(value=MinecraftServer.class)
public class MixinMCServer {

    // TODO: 1.18.2 @Shadow @Final public DynamicRegistryManager.Impl registryManager;
    @Shadow @Final public WorldSaveHandler saveHandler;

}