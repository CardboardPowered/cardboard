package com.javazilla.bukkitfabric.mixin;

import java.util.function.Supplier;

import org.bukkit.craftbukkit.CraftWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

@Mixin(World.class)
public class MixinWorld implements IMixinWorld {

    private CraftWorld bukkit;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(MutableWorldProperties a, RegistryKey<?> b, DimensionType d, Supplier<Boolean> e, boolean f, boolean g, long h, CallbackInfo ci){
        this.bukkit = new CraftWorld(((ServerWorld)(Object)this));
    }

    @Override
    public CraftWorld getCraftWorld() {
        return bukkit;
    }

}