/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.javazilla.bukkitfabric.mixin;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayerManager;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;

@Mixin(PlayerManager.class)
public class MixinPlayerManager implements IMixinPlayerManager {

    @Shadow
    public List<ServerPlayerEntity> players;

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
    public ServerPlayerEntity moveToWorld(ServerPlayerEntity entityplayer, ServerWorld worldserver, boolean flag, Location location, boolean avoidSuffocation) {
        entityplayer.stopRiding(); // CraftBukkit
        this.players.remove(entityplayer);
        entityplayer.getServerWorld().removePlayer(entityplayer, RemovalReason.CHANGED_DIMENSION);

        BlockPos blockposition = entityplayer.getSpawnPointPosition();
        float f = entityplayer.getSpawnAngle();
        boolean flag1 = entityplayer.isSpawnPointSet();

        org.bukkit.World fromWorld = ((IMixinServerEntityPlayer)entityplayer).getBukkitEntity().getWorld();
        entityplayer.notInAnyWorld = false;
        // CraftBukkit end

        Iterator<String> iterator = entityplayer.getScoreboardTags().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();
            entityplayer.addScoreboardTag(s);
        }

        boolean flag2 = false;

        // CraftBukkit start - fire PlayerRespawnEvent
        if (location == null) {
            boolean isBedSpawn = false;
            ServerWorld worldserver1 = CraftServer.server.getWorld(entityplayer.getSpawnPointDimension());
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

                    if (!iblockdata.isIn((Tag<Block>) BlockTags.BEDS) && !flag3) {
                        f1 = f;
                    } else {
                        Vec3d vec3d1 = Vec3d.ofBottomCenter((Vec3i) blockposition).subtract(vec3d).normalize();
                        f1 = (float) MathHelper.wrapDegrees(MathHelper.atan2(vec3d1.z, vec3d1.x) * 57.2957763671875D - 90.0D);
                    }

                    entityplayer.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, f1, 0.0F);
                    entityplayer.setSpawnPoint(worldserver1.getRegistryKey(), blockposition, f, flag1, false);
                    flag2 = !flag && flag3;
                    isBedSpawn = true;
                    location = new Location(((IMixinWorld)worldserver1).getWorldImpl(), vec3d.x, vec3d.y, vec3d.z);
                } else if (blockposition != null)
                    entityplayer.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, 0.0F));
            }

            if (location == null) {
                worldserver1 = CraftServer.server.getWorld(World.OVERWORLD);
                blockposition = entityplayer.getSpawnPointPosition();
                location = new Location(((IMixinWorld)worldserver1).getWorldImpl(), (double) ((float) blockposition.getX() + 0.5F), (double) ((float) blockposition.getY() + 0.1F), (double) ((float) blockposition.getZ() + 0.5F));
            }

            Player respawnPlayer = CraftServer.INSTANCE.getPlayer(entityplayer);
            PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(respawnPlayer, location, isBedSpawn && !flag2, flag2);
            CraftServer.INSTANCE.getPluginManager().callEvent(respawnEvent);

            if (entityplayer.isDisconnected())
                return entityplayer;

            location = respawnEvent.getRespawnLocation();
        } else location.setWorld(((IMixinWorld)worldserver).getWorldImpl());

        while (avoidSuffocation && !worldserver.isSpaceEmpty(entityplayer) && entityplayer.getY() < 256.0D)
            entityplayer.updatePosition(entityplayer.getX(), entityplayer.getY() + 1.0D, entityplayer.getZ());

        WorldProperties worlddata = worldserver.getLevelProperties();
        entityplayer.networkHandler.sendPacket(new PlayerRespawnS2CPacket(worldserver.getDimension(), worldserver.getRegistryKey(), BiomeAccess.hashSeed(worldserver.getSeed()), entityplayer.interactionManager.getGameMode(), entityplayer.interactionManager.getPreviousGameMode(), worldserver.isDebugWorld(), worldserver.isFlat(), flag));
        entityplayer.setWorld(worldserver);
        // TODO 1.17 entityplayer.removed = false;
        entityplayer.teleport(worldserver, location.getX(), location.getY(), location.getZ(), entityplayer.yaw, entityplayer.pitch);
        entityplayer.setSneaking(false);

        entityplayer.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(worldserver.getSpawnPos(), worldserver.getSpawnAngle()));
        entityplayer.networkHandler.sendPacket(new DifficultyS2CPacket(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        entityplayer.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(entityplayer.experienceProgress, entityplayer.totalExperience, entityplayer.experienceLevel));
        this.sendWorldInfo(entityplayer, worldserver);
        this.sendCommandTree(entityplayer);
        if (!entityplayer.isDisconnected()) {
            worldserver.onPlayerRespawned(entityplayer);
            this.players.add(entityplayer);
        }

        entityplayer.setHealth(entityplayer.getHealth());
        if (flag2)
            entityplayer.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), 1.0F, 1.0F));

        sendPlayerStatus(entityplayer);
        entityplayer.sendAbilitiesUpdate();
        for (Object o1 : entityplayer.getStatusEffects()) {
            StatusEffectInstance mobEffect = (StatusEffectInstance) o1;
            entityplayer.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(entityplayer.getEntityId(), mobEffect));
        }

        if (fromWorld != location.getWorld()) {
            PlayerChangedWorldEvent event = new PlayerChangedWorldEvent((Player) ((IMixinServerEntityPlayer)entityplayer).getBukkitEntity(), fromWorld);
            CraftServer.INSTANCE.getPluginManager().callEvent(event);
        }

        if (entityplayer.isDisconnected())
            this.savePlayerData(entityplayer);

        return entityplayer;
    }

    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    public void firePlayerJoinEvent(ClientConnection networkmanager, ServerPlayerEntity player, CallbackInfo ci) {
        String joinMessage = CraftChatMessage.fromComponent(new TranslatableText("multiplayer.player.joined", new Object[]{player.getDisplayName()}));

        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(CraftServer.INSTANCE.getPlayer(player), joinMessage);
        Bukkit.getServer().getPluginManager().callEvent(playerJoinEvent);
        if (!player.networkHandler.connection.isOpen()) return;

        joinMessage = playerJoinEvent.getJoinMessage();

        if (joinMessage != null && joinMessage.length() > 0)
            for (Text line : org.bukkit.craftbukkit.util.CraftChatMessage.fromString(joinMessage))
                CraftServer.server.getPlayerManager().sendToAll(new GameMessageS2CPacket(line, MessageType.SYSTEM, Util.NIL_UUID));
    }

    @Inject(at = @At("HEAD"), method = "remove")
    public void firePlayerQuitEvent(ServerPlayerEntity player, CallbackInfo ci) {
        player.closeHandledScreen();

        PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(CraftServer.INSTANCE.getPlayer(player), "\u00A7e" + player.getEntityName() + " left the game");
        CraftServer.INSTANCE.getPluginManager().callEvent(playerQuitEvent);
        player.playerTick();
    }

    @Inject(at = @At("HEAD"), method = "broadcastChatMessage", cancellable = true)
    public void broadcastChatMessage(Text message, MessageType type, UUID senderUuid, CallbackInfo ci) {
        String s = CraftChatMessage.toJSON(message);
        if (s.startsWith("{\"color\":\"yellow\",\"translate\":\"multiplayer.player.joined\"")) {
            // Cancel vanilla's join message. We do this so we don't have to overwrite onPlayerConnect
            ci.cancel();
            return;
        }
    }

    @Override
    public ServerPlayerEntity attemptLogin(ServerLoginNetworkHandler loginlistener, GameProfile gameprofile, String hostname) {
        TranslatableText chatmessage;

        // Moved from processLogin
        UUID uuid = PlayerEntity.getUuidFromProfile(gameprofile);
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
            savePlayerData(entityplayer); // CraftBukkit - Force the player's inventory to be saved
            entityplayer.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.duplicate_login", new Object[0]));
        }

        // Instead of kicking then returning, we need to store the kick reason
        // in the event, check with plugins to see if it's ok, and THEN kick
        // depending on the outcome.
        SocketAddress socketaddress = loginlistener.connection.getAddress();

        ServerPlayerEntity entity = new ServerPlayerEntity(CraftServer.server, CraftServer.server.getWorld(World.OVERWORLD), gameprofile, new ServerPlayerInteractionManager(CraftServer.server.getWorld(World.OVERWORLD)));
        Player player = (Player) ((IMixinServerEntityPlayer)entity).getBukkitEntity();
        PlayerLoginEvent event = new PlayerLoginEvent(player, hostname, ((java.net.InetSocketAddress) socketaddress).getAddress(), ((java.net.InetSocketAddress) loginlistener.connection.channel.remoteAddress()).getAddress());

        if (((PlayerManager)(Object)this).getUserBanList().contains(gameprofile) /*&& !((PlayerManager)(Object)this).getUserBanList().get(gameprofile).isInvalid()*/) {
            chatmessage = new TranslatableText("multiplayer.disconnect.banned.reason", new Object[]{"TODO REASON!"});
            chatmessage.append(new TranslatableText("multiplayer.disconnect.banned.expiration", new Object[] {"TODO EXPIRE!"}));

            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, CraftChatMessage.fromComponent(chatmessage));
        } else if (!((PlayerManager)(Object)this).isWhitelisted(gameprofile)) {
            chatmessage = new TranslatableText("multiplayer.disconnect.not_whitelisted");
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Server whitelisted!");
        } else if (((PlayerManager)(Object)this).getIpBanList().isBanned(socketaddress) /*&& !((PlayerManager)(Object)this).getIpBanList().get(socketaddress).isInvalid()*/) {
            BannedIpEntry ipbanentry = ((PlayerManager)(Object)this).getIpBanList().get(socketaddress);

            chatmessage = new TranslatableText("multiplayer.disconnect.banned_ip.reason", new Object[]{ipbanentry.getReason()});
            if (ipbanentry.getExpiryDate() != null)
                chatmessage.append(new TranslatableText("multiplayer.disconnect.banned_ip.expiration", new Object[]{ipbanentry.getExpiryDate()}));

            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, CraftChatMessage.fromComponent(chatmessage));
        } else {
            if (this.players.size() >= ((PlayerManager)(Object)this).getMaxPlayerCount() && !((PlayerManager)(Object)this).canBypassPlayerLimit(gameprofile))
                event.disallow(PlayerLoginEvent.Result.KICK_FULL, "Server is full"); // Spigot
        }

        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            loginlistener.disconnect(new LiteralText(event.getKickMessage()));
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

}