package com.fungus_soft.bukkitfabric.mixin;

import java.util.concurrent.Executor;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fungus_soft.bukkitfabric.interfaces.IMixinBukkitGetter;
import com.fungus_soft.bukkitfabric.interfaces.IMixinServerWorld;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements IMixinBukkitGetter, IMixinServerWorld {

    private CraftWorld bukkit;

    public ServerWorldMixin() {
        this.bukkit = new CraftWorld((ServerWorld) (Object) this);
    }

    @Inject(at = @At(value = "RETURN"), method = "<init>")
    public void addToBukkit(MinecraftServer a, Executor b, WorldSaveHandler c, LevelProperties d, DimensionType e, Profiler f, WorldGenerationProgressListener g, CallbackInfo h) {
        ((CraftServer)Bukkit.getServer()).addWorldToMap((ServerWorld) (Object) (this));
    }

    @Override
    public CraftWorld getBukkitObject() {
        if (null == bukkit)
            this.bukkit = new CraftWorld((ServerWorld) (Object) this);
        return bukkit;
    }

}