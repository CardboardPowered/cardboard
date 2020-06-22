package com.fungus_soft.bukkitfabric.mixin;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fungus_soft.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.LevelInfo;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld implements IMixinWorld {

    @Inject(at = @At(value = "HEAD"), method = "init")
    public void addToBukkit(LevelInfo d, CallbackInfo ci){
        ((CraftServer)Bukkit.getServer()).addWorldToMap(getCraftWorld());
    }

}