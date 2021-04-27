package com.javazilla.bukkitfabric.mixin.block;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(NetherPortalBlock.class)
public class MixinNetherPortalBlock {

    @Inject(at = @At("HEAD"), method = "onEntityCollision")
    public void callBukkitEvent(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!entity.hasVehicle() && !entity.hasPassengers() && entity.canUsePortals()) {
            EntityPortalEnterEvent event = new EntityPortalEnterEvent(((IMixinEntity)entity).getBukkitEntity(), new org.bukkit.Location(((IMixinWorld)world).getWorldImpl(), pos.getX(), pos.getY(), pos.getZ()));
            Bukkit.getPluginManager().callEvent(event);
        }
    }

}