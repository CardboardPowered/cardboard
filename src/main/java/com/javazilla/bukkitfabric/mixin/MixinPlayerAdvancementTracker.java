package com.javazilla.bukkitfabric.mixin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.interfaces.IMixinAdvancement;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerAdvancementTracker.class)
public class MixinPlayerAdvancementTracker {

    @Shadow
    public ServerPlayerEntity owner;

    @SuppressWarnings("rawtypes")
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementRewards;apply(Lnet/minecraft/server/network/ServerPlayerEntity;)V"), method = "grantCriterion")
    public void fireBukkitEvent(Advancement advancement, String s, CallbackInfoReturnable ci) {
        Bukkit.getServer().getPluginManager().callEvent(new org.bukkit.event.player.PlayerAdvancementDoneEvent((Player) ((IMixinEntity)this.owner).getBukkitEntity(), ((IMixinAdvancement)advancement).getBukkitAdvancement())); // Bukkit
    }

}