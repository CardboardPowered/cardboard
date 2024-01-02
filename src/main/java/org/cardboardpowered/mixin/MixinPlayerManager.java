/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerRespawnEvent.RespawnFlag;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.world.WorldImpl;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;
import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayNetworkHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayerManager;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import com.mojang.authlib.GameProfile;

import me.isaiah.common.ICommonMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.*;

@Mixin(PlayerManager.class)
public class MixinPlayerManager implements IMixinPlayerManager {

    @Shadow
    public List<ServerPlayerEntity> players;
    
    @Shadow private MinecraftServer server;

    @Shadow
    public void sendCommandTree(ServerPlayerEntity player) {}

    @Shadow
    public void sendWorldInfo(ServerPlayerEntity player, ServerWorld world) {}

    @Shadow
    public void savePlayerData(ServerPlayerEntity player) {}

    @Shadow
    public void sendPlayerStatus(ServerPlayerEntity player) {}

    @Shadow
    public Map<UUID, ServerPlayerEntity> playerMap;

    @Override
    public ServerPlayerEntity moveToWorld(ServerPlayerEntity player, ServerWorld worldserver, boolean flag, Location location, boolean avoidSuffocation) {
        boolean flag2 = false;
        BlockPos blockposition = player.getSpawnPointPosition();
        float f = player.getSpawnAngle();
        boolean flag1 = player.isSpawnForced();
        if (location == null) {
            boolean isBedSpawn = false;
            ServerWorld worldserver1 = CraftServer.server.getWorld(player.getSpawnPointDimension());
            if (worldserver1 != null) {
                Optional<?> optional;

                if (blockposition != null)
                    optional = PlayerEntity.findRespawnPosition(worldserver1, blockposition, f, flag1, flag);
                else optional = Optional.empty();

                if (optional.isPresent()) {
                    BlockState iblockdata = worldserver1.getBlockState(blockposition);
                    boolean flag3 = iblockdata.isOf(Blocks.RESPAWN_ANCHOR);
                    Vec3d vec3d = (Vec3d) optional.get();
                    float f1;
                    if (!iblockdata.isIn(BlockTags.BEDS) && !flag3) {
                        f1 = f;
                    } else {
                        Vec3d vec3d1 = Vec3d.ofBottomCenter((Vec3i) blockposition).subtract(vec3d).normalize();
                        f1 = (float) MathHelper.wrapDegrees(MathHelper.atan2(vec3d1.z, vec3d1.x) * 57.2957763671875D - 90.0D);
                    }

                    player.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, f1, 0.0F);
                    player.setSpawnPoint(worldserver1.getRegistryKey(), blockposition, f, flag1, false);
                    flag2 = !flag && flag3;
                    isBedSpawn = true;
                    location = new Location(((IMixinWorld)worldserver1).getWorldImpl(), vec3d.x, vec3d.y, vec3d.z);
                } else if (blockposition != null)
                    player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, 0.0F));
            }

            if (location == null) {
                worldserver1 = CraftServer.server.getWorld(World.OVERWORLD);
                blockposition = player.getSpawnPointPosition();
                location = new Location(((IMixinWorld)worldserver1).getWorldImpl(), (double) ((float) blockposition.getX() + 0.5F), (double) ((float) blockposition.getY() + 0.1F), (double) ((float) blockposition.getZ() + 0.5F));
            }

            Player respawnPlayer = CraftServer.INSTANCE.getPlayer(player);
            PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(respawnPlayer, location, isBedSpawn && !flag2, flag2);
            CraftServer.INSTANCE.getPluginManager().callEvent(respawnEvent);

            if (player.isDisconnected()) return player;

            location = respawnEvent.getRespawnLocation();
        } else location.setWorld(((IMixinWorld)worldserver).getWorldImpl());
        ServerWorld worldserver1 = ((WorldImpl) location.getWorld()).getHandle();
        ServerWorld fromWorld = player.getWorld();
        player.teleport(worldserver1, location.getX(), location.getY(), location.getZ(), 0, 0);

        if (fromWorld != location.getWorld()) {
            PlayerChangedWorldEvent event = new PlayerChangedWorldEvent((Player) ((IMixinServerEntityPlayer)player).getBukkitEntity(), ((IMixinWorld)fromWorld).getWorldImpl());
            CraftServer.INSTANCE.getPluginManager().callEvent(event);
        }
        return player;
    }

    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    public void firePlayerJoinEvent(ClientConnection con, ServerPlayerEntity player, CallbackInfo ci) {
        String joinMessage = Text.translatable("multiplayer.player.joined", new Object[]{player.getDisplayName()}).getString();

        PlayerImpl plr = (PlayerImpl) CraftServer.INSTANCE.getPlayer(player);
        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(plr, joinMessage);
        BukkitEventFactory.callEvent(playerJoinEvent);
        if (!player.networkHandler.connection.isOpen()) return;

        joinMessage = playerJoinEvent.getJoinMessage();

        if (joinMessage != null && joinMessage.length() > 0) {
            for (Text line : org.bukkit.craftbukkit.util.CraftChatMessage.fromString(joinMessage)) {
                // 1.18: CraftServer.server.getPlayerManager().sendToAll(new GameMessageS2CPacket(line, MessageType.SYSTEM, Util.NIL_UUID));
                // TODO: 1.19
            	CraftServer.server.getPlayerManager().broadcast(line, entityplayer -> line, false);
            }
        }

    }

    @Inject(at = @At("HEAD"), method = "remove")
    public void firePlayerQuitEvent(ServerPlayerEntity player, CallbackInfo ci) {
        player.closeHandledScreen();

        PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(CraftServer.INSTANCE.getPlayer(player), "\u00A7e" + player.getEntityName() + " left the game");
        CraftServer.INSTANCE.getPluginManager().callEvent(playerQuitEvent);
        player.playerTick();
    }

    @Override
    public ServerPlayerEntity attemptLogin(ServerLoginNetworkHandler nethand, GameProfile profile, PlayerPublicKey key, String hostname) {
    	MutableText chatmessage;

        // Moved from processLogin
        // 1.18: UUID uuid = PlayerEntity.getUuidFromProfile(profile);
    	UUID uuid = ICommonMod.getIServer().get_uuid_from_profile(profile);
    	// UUID uuid = DynamicSerializableUuid.getUuidFromProfile(profile);
    	List<ServerPlayerEntity> list = Lists.newArrayList();

        ServerPlayerEntity entityplayer;

        for (int i = 0; i < this.players.size(); ++i) {
            entityplayer = (ServerPlayerEntity) this.players.get(i);
            if (entityplayer.getUuid().equals(uuid))
                list.add(entityplayer);
        }

        Iterator<ServerPlayerEntity> iterator = list.iterator();

        while (iterator.hasNext()) {
            entityplayer = (ServerPlayerEntity) iterator.next();
            savePlayerData(entityplayer); // Force the player's inventory to be saved
            entityplayer.networkHandler.disconnect(Text.of("multiplayer.disconnect.duplicate_login"));
        }

        SocketAddress address = nethand.connection.getAddress();

        me.isaiah.common.cmixin.IMixinPlayerManager imixin = (me.isaiah.common.cmixin.IMixinPlayerManager) (Object)this;
       // ServerPlayerEntity entity = imixin.InewPlayer(CraftServer.server, CraftServer.server.getWorld(World.OVERWORLD), profile);
        ServerPlayerEntity entity = new ServerPlayerEntity(CraftServer.server, CraftServer.server.getWorld(World.OVERWORLD), profile, key);
        
        Player player = (Player) ((IMixinServerEntityPlayer)entity).getBukkitEntity();
        PlayerLoginEvent event = new PlayerLoginEvent(player, hostname, ((java.net.InetSocketAddress) address).getAddress(), ((java.net.InetSocketAddress) nethand.connection.channel.remoteAddress()).getAddress());

        if (((PlayerManager)(Object)this).getUserBanList().contains(profile) /*&& !((PlayerManager)(Object)this).getUserBanList().get(gameprofile).isInvalid()*/) {
            chatmessage = Text.translatable("multiplayer.disconnect.banned.reason", new Object[]{"TODO REASON!"});
            //chatmessage.append(new TranslatableTextContent("multiplayer.disconnect.banned.expiration", new Object[] {"TODO EXPIRE!"}));

            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, CraftChatMessage.fromComponent(chatmessage));
        } else if (!((PlayerManager)(Object)this).isWhitelisted(profile)) {
            chatmessage = Text.translatable("multiplayer.disconnect.not_whitelisted");
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Server whitelisted!");
        } else if (((PlayerManager)(Object)this).getIpBanList().isBanned(address) /*&& !((PlayerManager)(Object)this).getIpBanList().get(socketaddress).isInvalid()*/) {
            BannedIpEntry ipbanentry = ((PlayerManager)(Object)this).getIpBanList().get(address);

            chatmessage = Text.translatable("multiplayer.disconnect.banned_ip.reason", new Object[]{ipbanentry.getReason()});
            //if (ipbanentry.getExpiryDate() != null)
            //    chatmessage.append(new TranslatableTextContent("multiplayer.disconnect.banned_ip.expiration", new Object[]{ipbanentry.getExpiryDate()}));

            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, CraftChatMessage.fromComponent(chatmessage));
        } else {
            if (this.players.size() >= ((PlayerManager)(Object)this).getMaxPlayerCount() && !((PlayerManager)(Object)this).canBypassPlayerLimit(profile))
                event.disallow(PlayerLoginEvent.Result.KICK_FULL, "Server is full");
        }

        BukkitEventFactory.callEvent(event);
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            nethand.disconnect(Text.of(event.getKickMessage()));
            return null;
        }
        return entity;
    }

    @Shadow
    public void sendScoreboard(ServerScoreboard scoreboardserver, ServerPlayerEntity entityplayer) {
    }

    @Override
    public void sendScoreboardBF(ServerScoreboard newboard, ServerPlayerEntity handle) {
        sendScoreboard(newboard, handle);
    }
    
    /*@Redirect(at = @At(value = "INVOKE", 
            target = "class=net/minecraft/server/network/ServerPlayerEntity;"),
            method = "acceptPlayer")
    public ServerPlayerEntity acceptPlayer_createPlayer(PlayerManager man, GameProfile a, PlayerPublicKey key) {
        return cardboard_player;
    }*/
    
    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public ServerPlayerEntity respawnPlayer(ServerPlayerEntity playerIn, boolean conqueredEnd) {
        playerIn.stopRiding(); // CraftBukkit
        this.players.remove(playerIn);
        
        playerIn.getWorld().removePlayer(playerIn, Entity.RemovalReason.DISCARDED);
        BlockPos blockposition = playerIn.getSpawnPointPosition();
        float f = playerIn.getSpawnAngle();
        boolean flag1 = playerIn.isSpawnForced();
        // CraftBukkit start
        // Banner start - remain original field to compat with carpet
        ServerWorld worldserver_vanilla = this.server.getWorld(playerIn.getSpawnPointDimension());
        Optional optional_vanilla;

        if (worldserver_vanilla != null && blockposition != null) {
            optional_vanilla = net.minecraft.entity.player.PlayerEntity.findRespawnPosition(worldserver_vanilla, blockposition, f, flag1, conqueredEnd);
        } else {
            optional_vanilla = Optional.empty();
        }

        ServerWorld worldserver_vanilla_1 = worldserver_vanilla != null && optional_vanilla.isPresent() ? worldserver_vanilla : this.server.getOverworld();
        entityplayer_vanilla = new ServerPlayerEntity(this.server, worldserver_vanilla_1, playerIn.getGameProfile(), playerIn.getPublicKey());
        // Banner end

        ServerPlayerEntity entityplayer1 = playerIn;
        fromWorld = ((IMixinServerEntityPlayer)playerIn).getBukkitEntity().getWorld();
        playerIn.notInAnyWorld = false;
        // CraftBukkit end

        if (null != playerIn.networkHandler) {
        
        	//entityplayer1.networkHandler = playerIn.networkHandler;
        }
        ((IMixinServerEntityPlayer)entityplayer1).copyFrom_unused(playerIn, conqueredEnd);
        entityplayer1.setId(playerIn.getId());
        entityplayer1.setMainArm(playerIn.getMainArm());

        // for (String s : playerIn.getCommandTags()) {
        //    entityplayer1.addCommandTag(s);
        // }

        boolean flag2 = false;

        // CraftBukkit start - fire PlayerRespawnEvent
        if (banner$loc == null) {
            boolean isBedSpawn = false;
            ServerWorld worldserver1 = this.server.getWorld(playerIn.getSpawnPointDimension());
            if (worldserver1 != null) {
                if (optional_vanilla.isPresent()) {
                    BlockState iblockdata = worldserver1.getBlockState(blockposition);
                    boolean flag3 = iblockdata.isOf(Blocks.RESPAWN_ANCHOR);
                    Vec3d vec3d = (Vec3d) optional_vanilla.get();
                    float f1;

                    if (!iblockdata.isIn(BlockTags.BEDS) && !flag3) {
                        f1 = f;
                    } else {
                        Vec3d vec3d1 = Vec3d.ofBottomCenter(blockposition).subtract(vec3d).normalize();

                        f1 = (float) MathHelper.wrapDegrees(MathHelper.atan2(vec3d1.z, vec3d1.x) * 57.2957763671875D - 90.0D);
                    }
                    // Banner end
                    flag2 = !conqueredEnd && flag3;
                    isBedSpawn = true;
                    banner$loc = CraftLocation.toBukkit(vec3d, ((IMixinWorld)worldserver1).getWorldImpl(), f1, 0.0F);
                } else if (blockposition != null) {
                    entityplayer1.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, 0.0F));
                    // entityplayer1.pushChangeSpawnCause(PlayerSpawnChangeEvent.Cause.RESET);
                    entityplayer1.setSpawnPoint(null, null, 0f, false, false);
                }
            }

            if (banner$loc == null) {
                worldserver1 = this.server.getWorld(World.OVERWORLD);
                // blockposition = entityplayer1.getSpawnPoint(worldserver1);
                blockposition = entityplayer1.getSpawnPointPosition();
                
                banner$loc = CraftLocation.toBukkit(blockposition, ((IMixinWorld)worldserver1).getWorldImpl()).add(0.5F, 0.1F, 0.5F);
            }

            Player respawnPlayer = (Player) ((IMixinServerEntityPlayer)entityplayer1).getBukkitEntity();
            respawnEvent = new PlayerRespawnEvent(respawnPlayer, banner$loc, isBedSpawn && !flag2, flag2);
            CraftServer.INSTANCE.getPluginManager().callEvent(respawnEvent);
            // Spigot Start
            // if (playerIn.networkHandler.isDisconnected()) {
            if (playerIn.isDisconnected()) {
            	System.out.println("PLAYER DISCONNECT");
                return playerIn;
            }
            // Spigot End

            banner$loc = respawnEvent.getRespawnLocation();
            if (!conqueredEnd) { // keep inventory here since inventory dropped at ServerPlayerEntity#onDeath
                ((IMixinServerEntityPlayer)playerIn).reset(); // SPIGOT-4785
            }
        } else {
            if (banner$worldserver == null) banner$worldserver = this.server.getWorld(playerIn.getSpawnPointDimension());
            banner$loc.setWorld(((IMixinWorld)banner$worldserver).getWorldImpl());
        }
        worldserver1 = ((WorldImpl) banner$loc.getWorld()).getHandle();
        
        entityplayer1.setPos(banner$loc.getX(), banner$loc.getY(), banner$loc.getZ());
        entityplayer1.setRotation(banner$loc.getYaw(), banner$loc.getPitch());
        
        //entityplayer1.forceSetPositionRotation(banner$loc.getX(), banner$loc.getY(), banner$loc.getZ(), banner$loc.getYaw(), banner$loc.getPitch());
        // CraftBukkit end

        while (avoidSuffocation.getAndSet(true) && !worldserver1.isSpaceEmpty(entityplayer1) && entityplayer1.getY() < (double) worldserver1.getTopY()) {
            entityplayer1.setPosition(entityplayer1.getX(), entityplayer1.getY() + 1.0D, entityplayer1.getZ());
        }

        // CraftBukkit start
        worlddata = worldserver1.getLevelProperties();
        
        int sim = CraftServer.INSTANCE.getSimulationDistance();
        int vd = CraftServer.INSTANCE.getViewDistance();
        
        entityplayer1.networkHandler.sendPacket(new PlayerRespawnS2CPacket(worldserver1.getDimensionKey(), worldserver1.getRegistryKey(),
        				BiomeAccess.hashSeed(worldserver1.getSeed()), entityplayer1.interactionManager.getGameMode(), entityplayer1.interactionManager.getPreviousGameMode(),
        				worldserver1.isDebugWorld(), worldserver1.isFlat(), conqueredEnd, entityplayer1.getLastDeathPos()));
        entityplayer1.networkHandler.sendPacket(new ChunkLoadDistanceS2CPacket((vd)));
        entityplayer1.networkHandler.sendPacket(new SimulationDistanceS2CPacket(sim));
        ((IMixinServerEntityPlayer)entityplayer1).spawnIn(worldserver1);
        entityplayer1.unsetRemoved();
        ((IMixinPlayNetworkHandler)entityplayer1.networkHandler).teleport(CraftLocation.toBukkit(entityplayer1.getPos(), ((IMixinWorld)worldserver1).getWorldImpl(), entityplayer1.getYaw(), entityplayer1.getPitch()));
        entityplayer1.setSneaking(false);
        entityplayer1.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(worldserver1.getSpawnPos(), worldserver1.getSpawnAngle()));
        entityplayer1.networkHandler.sendPacket(new DifficultyS2CPacket(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        entityplayer1.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(entityplayer1.experienceProgress, entityplayer1.totalExperience, entityplayer1.experienceLevel));
        this.sendWorldInfo(entityplayer1, worldserver1);
        this.sendCommandTree(entityplayer1);
        if (!playerIn.isDisconnected()) {
            worldserver1.onPlayerRespawned(entityplayer1);
            this.players.add(entityplayer1);
            this.playerMap.put(entityplayer1.getUuid(), entityplayer1);
        }
        // Banner start - add for carpet compat
        if (entityplayer_vanilla == null) {
            entityplayer1.onSpawn();
        }
        // Banner end
        entityplayer1.setHealth(entityplayer1.getHealth());
        if (flag2) {
            entityplayer1.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), 1.0F, 1.0F, worldserver1.getRandom().nextLong()));
        }
        // Added from changeDimension
        this.sendPlayerStatus(playerIn); // Update health, etc...
        playerIn.sendAbilitiesUpdate();
        for (StatusEffectInstance mobEffect : playerIn.getStatusEffects()) {
            playerIn.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(playerIn.getId(), mobEffect));
        }

        // Fire advancement trigger
        playerIn.worldChanged(((WorldImpl) fromWorld).getHandle());

        // Don't fire on respawn
        if (fromWorld != banner$loc.getWorld()) {
            PlayerChangedWorldEvent event = new PlayerChangedWorldEvent( (@NotNull Player) ((IMixinServerEntityPlayer)playerIn).getBukkitEntity(), fromWorld);
            Bukkit.getPluginManager().callEvent(event);
        }

        // Save player file again if they were disconnected
        // if (playerIn.networkHandler.isDisconnected()) {
        if (playerIn.isDisconnected()) {
            this.savePlayerData(playerIn);
        }
        // CraftBukkit end
        banner$loc = null;
        banner$respawnReason = null;
        banner$worldserver = null;
        
        entityplayer1.setHealth(entityplayer1.getMaxHealth());
        
        return entityplayer1;
    }
    
    private Location banner$loc = null;
    private transient RespawnFlag banner$respawnReason;
    public ServerWorld banner$worldserver = null;
    public AtomicBoolean avoidSuffocation = new AtomicBoolean(true);
    
    // Banner start - Fix mixin by apoli
    public org.bukkit.World fromWorld;
    public PlayerRespawnEvent respawnEvent;
    public ServerWorld worldserver1;
    public WorldProperties worlddata;
    public ServerPlayerEntity entityplayer_vanilla;
    // Banner end

}