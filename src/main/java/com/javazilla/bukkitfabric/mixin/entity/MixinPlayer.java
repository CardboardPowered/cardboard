/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.javazilla.bukkitfabric.mixin.entity;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.MainHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinCommandOutput;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import com.mojang.authlib.GameProfile;

import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Arm;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public class MixinPlayer extends MixinEntity implements IMixinCommandOutput, IMixinServerEntityPlayer  {

    private CraftPlayer bukkit;

    @Shadow
    public int screenHandlerSyncId;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(MinecraftServer server, ServerWorld world, GameProfile profile, ServerPlayerInteractionManager interactionManager, CallbackInfo ci) {
        this.bukkit = new CraftPlayer((ServerPlayerEntity) (Object) this);
        CraftServer.INSTANCE.playerView.add(this.bukkit);
    }

    @Override
    public CommandSender getBukkitSender(ServerCommandSource serverCommandSource) {
        return bukkit;
    }

    @Override
    public Entity getBukkitEntity() {
        return bukkit;
    }

    @Override
    public void reset() {
        // TODO Bukkit4Fabric: Auto-generated method stub
    }

    @Override
    public BlockPos getSpawnPoint(World world) {
        return ((ServerWorld)world).getSpawnPos();
    }

    @Inject(at = @At("TAIL"), method = "onDisconnect")
    public void onDisconnect(CallbackInfo ci) {
        CraftServer.INSTANCE.playerView.remove(this.bukkit);
    }

    /**
     * @author BukkitFabric
     * @reason Bukkit Multiworld Teleport
     */
    @Overwrite
    public void teleport(ServerWorld worldserver, double d0, double d1, double d2, float f, float f1) {
        this.getBukkitEntity().teleport(new Location(((IMixinWorld)worldserver).getCraftWorld(), d0, d1, d2, f, f1), org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    @SuppressWarnings("deprecation")
    @Inject(at = @At("HEAD"), method = "setGameMode", cancellable = true)
    public void setGameMode(net.minecraft.world.GameMode gm, CallbackInfo ci) {
        if (gm == ((ServerPlayerEntity)(Object)this).interactionManager.getGameMode())
            ci.cancel();

        PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent((Player) getBukkitEntity(), GameMode.getByValue(gm.getId()));
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        if (event.isCancelled())
            ci.cancel();
    }

    public String locale = "en_us"; // CraftBukkit - add, lowercase

    @Inject(at = @At("HEAD"), method = "setClientSettings")
    public void setClientSettings(ClientSettingsC2SPacket packetplayinsettings, CallbackInfo ci) {
        if (((ServerPlayerEntity) (Object) this).getMainArm() != packetplayinsettings.getMainArm()) {
            PlayerChangedMainHandEvent event = new PlayerChangedMainHandEvent((Player) getBukkitEntity(), ((ServerPlayerEntity) (Object) this).getMainArm() == Arm.LEFT ? MainHand.LEFT : MainHand.RIGHT);
            CraftServer.INSTANCE.getPluginManager().callEvent(event);
        }
    }

    @Shadow
    public void closeHandledScreen() {
    }

    @Override
    public int nextContainerCounter() {
        this.screenHandlerSyncId = this.screenHandlerSyncId % 100 + 1;
        return screenHandlerSyncId; // CraftBukkit
    }

}