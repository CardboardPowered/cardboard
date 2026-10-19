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
package org.cardboardpowered.mixin.server.players;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.*;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.item.crafting.RecipeManager;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.server.level.ServerLevelBridge;
import org.cardboardpowered.bridge.server.network.ServerGamePacketListenerImplBridge;
import org.cardboardpowered.bridge.server.players.PlayerListBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;

import java.net.SocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerRespawnEvent.RespawnReason;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;
import org.cardboardpowered.extras.PlayerList_LoginResult;
import org.cardboardpowered.extras.ServerPlayer_RespawnResult;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.cardboardpowered.impl.world.CraftWorld;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin implements PlayerListBridge {
    @Final
    @Shadow
    public List<ServerPlayer> players;
    
    @Final
    @Shadow private MinecraftServer server;

    @Shadow
    protected void save(ServerPlayer player) {}

    @Shadow
    public void sendAllPlayerInfo(ServerPlayer player) {}
    
    @Final
    @Shadow
    private UserBanList bans;

    @Unique private CraftPlayer plr;

    @Inject(method = "placeNewPlayer", at = @At("HEAD"), cancellable = true)
    public void onConnect(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
        this.plr = (CraftPlayer) CraftServer.INSTANCE.getPlayer(player);

        // PlayerLoginEvent was never fired (the Paper login hack is still a TODO), so plugins that attach
        // state there got nothing: LuckPerms injects its Permissible in this event, and without it saved
        // permissions never applied. Fired here because this is the first point the CraftPlayer exists.
        // The hostname is not kept past the login phase, so it is passed empty.
        if (this.plr == null) return;
        java.net.InetAddress address = connection.getRemoteAddress() instanceof java.net.InetSocketAddress inet
                ? inet.getAddress() : java.net.InetAddress.getLoopbackAddress();
        PlayerLoginEvent loginEvent = new PlayerLoginEvent(this.plr, "", address);
        CraftEventFactory.callEvent(loginEvent);

        if (loginEvent.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            net.kyori.adventure.text.Component reason = loginEvent.kickMessage();
            connection.disconnect(reason == null
                    ? Component.translatable("multiplayer.disconnect.generic")
                    : io.papermc.paper.adventure.PaperAdventure.asVanilla(reason));
            this.plr = null;
            ci.cancel();
        }
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    public void firePlayerJoinEvent(PlayerList instance, Component message, boolean overlay) {
        CraftPlayer plr;

        if(this.plr == null) {
            instance.broadcastSystemMessage(message, overlay);
            return;
        } else {
            plr = this.plr;
            this.plr = null;
        }

        String key = "multiplayer.player.joined";
        Component name = plr.getHandle().getDisplayName();

        String joinMessage = ChatFormatting.YELLOW + Component.translatable(key, name).getString();

        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(plr, joinMessage);
        CraftEventFactory.callEvent(playerJoinEvent);
        ServerGamePacketListenerImplBridge ims = (ServerGamePacketListenerImplBridge)plr.getHandle().connection;

        if (!ims.cb_get_connection().isConnected()) {
            return;
        }

        joinMessage = playerJoinEvent.getJoinMessage();

        if (joinMessage != null && !joinMessage.isEmpty()) {
            for (Component line : CraftChatMessage.fromString(joinMessage)) {
                broadcastSystemMessage(line, entityplayer -> line, false);
            }
        }

    }

    @Inject(at = @At("HEAD"), method = "remove")
    public void firePlayerQuitEvent(ServerPlayer player, CallbackInfo ci) {
        player.closeContainer();

        PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(CraftServer.INSTANCE.getPlayer(player), "\u00A7e" + player.getDisplayName().getString() + " left the game");
        CraftServer.INSTANCE.getPluginManager().callEvent(playerQuitEvent);
        player.doTick();
    }
    
    private static final Logger cb$LOGGER = LogUtils.getLogger();
    
    /**
     * todo: update our login code to use SpawnPrepareTask instead of our attemptLogin
     */
    private Location cardboard$getPlayerSpawn(NameAndId player) {
    	Optional<ValueInput> optional;
    	ResourceKey<Level> resourceKey = null;
    	boolean[] invalidPlayerWorld = new boolean[]{false};

    	try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(cb$LOGGER)) {
    		optional = this.server
    				.getPlayerList()
    				.loadPlayerData(player)
    				.map(nbt -> TagValueInput.create(scopedCollector, this.server.registryAccess(), nbt));

    		bukkitData:
    			if (optional.isPresent()) {
    				ValueInput playerData = optional.get();
    				Optional<Long> worldUUIDMost = playerData.getLong("WorldUUIDMost");
    				Optional<Long> worldUUIDLeast = playerData.getLong("WorldUUIDLeast");
    				Optional<String> worldName = playerData.getString("world");
    				org.bukkit.World bWorld;
    				if (worldUUIDMost.isPresent() && worldUUIDLeast.isPresent()) {
    					bWorld = Bukkit.getServer().getWorld(new UUID(worldUUIDMost.get(), worldUUIDLeast.get()));
    				} else {
    					if (!worldName.isPresent()) {
    						break bukkitData;
    					}

    					bWorld = Bukkit.getServer().getWorld(worldName.get());
    				}

    				if (bWorld != null) {
    					resourceKey = ((CraftWorld)bWorld).getHandle().dimension();
    				} else {
    					resourceKey = Level.OVERWORLD;
    					invalidPlayerWorld[0] = true;
    				}
    			}

    		ServerPlayer.SavedPosition savedPosition = optional.<ServerPlayer.SavedPosition>flatMap(view -> view.read(ServerPlayer.SavedPosition.MAP_CODEC))
    				.orElse(ServerPlayer.SavedPosition.EMPTY);
    		LevelData.RespawnData respawnData = this.server.getWorldData().overworldData().getRespawnData();

    		if (resourceKey == null) {
    			resourceKey = savedPosition.dimension().orElse(null);
    		}

    		ServerLevel vanillaDefaultLevel = this.server.getLevel(respawnData.dimension());
    		if (vanillaDefaultLevel == null) {
    			vanillaDefaultLevel = this.server.overworld();
    		}

    		ServerLevel serverLevel1;
    		if (resourceKey == null) {
    			serverLevel1 = vanillaDefaultLevel;
    		} else {
    			serverLevel1 = this.server.getLevel(resourceKey);
    			if (serverLevel1 == null) {
    				cb$LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", resourceKey);
    				serverLevel1 = vanillaDefaultLevel;
    			}
    		}

    		ServerLevel serverLevel = serverLevel1;
    		CompletableFuture<Vec3> completableFuture = savedPosition.position()
    				.map(CompletableFuture::completedFuture)
    				.orElseGet(() -> PlayerSpawnFinder.findSpawn(serverLevel, respawnData.pos()));
    		Vec2 vec2 = savedPosition.rotation().orElse(new Vec2(respawnData.yaw(), respawnData.pitch()));
    		// this.stage = new PrepareSpawnTask.LoadPlayerChunks(serverLevel, completableFuture, vec2);
    		
    		// CraftLocation.toBukkit(null, serverLevel, 0, 0)
    		
    		try {
				Vec3 d3 = completableFuture.get();
				Location loc = CraftLocation.toBukkit(d3, serverLevel, vec2.x, vec2.y);
				return loc;
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				return null;
			}
    		
    	}
    }

    @Shadow
    protected void updateEntireScoreboard(ServerScoreboard scoreboardserver, ServerPlayer entityplayer) {
    }

    @Shadow public abstract void broadcastSystemMessage(Component message, Function<ServerPlayer, Component> playerMessageFactory, boolean overlay);

    @Shadow
    @Final
    private IpBanList ipBans;

    @Shadow
    @Final
    private static SimpleDateFormat BAN_DATE_FORMAT;

    @Shadow
    public abstract int getMaxPlayers();

    @Shadow
    public abstract boolean canBypassPlayerLimit(NameAndId nameAndId);

    @Shadow
    @Final
    private ServerOpList ops;

    @Shadow
    public abstract boolean isUsingWhitelist();

    @Shadow
    @Final
    private UserWhiteList whitelist;

    @Shadow
    protected abstract Path locateStatsFile(GameProfile gameProfile);

    @Override
    public void sendScoreboardBF(ServerScoreboard newboard, ServerPlayer handle) {
        updateEntireScoreboard(newboard, handle);
    }

    @Inject(method = "respawn", at = @At("HEAD"), cancellable = true)
    private void cardboard$onRespawnHead(ServerPlayer player, boolean keepInventory, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir, @Share("respawnResult") LocalRef<ServerPlayer_RespawnResult> resultRef, @Share("fromLevel") LocalRef<ServerLevel> fromLevelRef, @Share("respawnReason") LocalRef<PlayerRespawnEvent.RespawnReason> respawnReasonRef) {
        fromLevelRef.set(player.level());
        PlayerRespawnEvent.RespawnReason respawnReason = RespawnReason.DEATH;
        if (Objects.requireNonNull(removalReason) == RemovalReason.CHANGED_DIMENSION) {
            respawnReason = RespawnReason.END_PORTAL;
        }

        ServerPlayer_RespawnResult result = ((ServerPlayerBridge) player).cardboard$findRespawnPositionAndUseSpawnBlock0(
                !keepInventory,
                TeleportTransition.DO_NOTHING,
                respawnReason
        );

        if (result == null) { // disconnected player during the respawn event
            cir.setReturnValue(player);
            return;
        }

        resultRef.set(result);
        respawnReasonRef.set(respawnReason);
    }

    @WrapOperation(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;"))
    private TeleportTransition cardboard$respawnUseCalculatedTransition(ServerPlayer instance, boolean bl, TeleportTransition.PostTeleportTransition postTeleportTransition, Operation<TeleportTransition> original, @Share("respawnResult") LocalRef<ServerPlayer_RespawnResult> resultRef) {
        return resultRef.get().transition();
    }

    /// I think these commented respawn things are unnecessary, but I kept them in case they were needed.
    /*@WrapOperation(method = "respawn", at = @At(value = "NEW", target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"))
    private ServerPlayer cardboard$respawnReusePlayerInstance(MinecraftServer server, ServerLevel level, GameProfile profile, ClientInformation clientInformation, Operation<ServerPlayer> original, @Local(argsOnly = true) ServerPlayer originalPlayer) {
        originalPlayer.setLevel(level);
        return originalPlayer;
    }*/

    /*&@WrapOperation(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;copyRespawnPosition(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void cardboard$respawnDisableCopyRespawnPosition(ServerPlayer instance, ServerPlayer target, Operation<Void> original) {
        // Paper - Once we not reuse the player entity, this can be flipped again but without the events being fired
    }*/

    /*@Inject(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;snapTo(DDDFF)V", shift = At.Shift.BEFORE))
    private void cardboard$beforeSnapTo(ServerPlayer player, boolean keepInventory, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
        // Paper start - Once we not reuse the player entity we can remove this.
        if (!keepInventory) ((ServerPlayerBridge)player).reset();
        ((ServerPlayerBridge)player).spawnIn(player.level());
        player.unsetRemoved();
        player.setShiftKeyDown(false);
        // Paper end
    }*/

    @Inject(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;snapTo(DDDFF)V", shift = At.Shift.AFTER))
    private void cardboard$respawnAfterSnapTo(ServerPlayer player, boolean keepInventory, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
        Vec3 vec3 = player.position();
        player.connection.resetPosition(); // Paper - Fix SPIGOT-1903, MC-98153
        // TODO
        //player.level().getChunkSource().addTicketWithRadius(net.minecraft.server.level.TicketType.POST_TELEPORT, new net.minecraft.world.level.ChunkPos(net.minecraft.util.Mth.floor(vec3.x()) >> 4, net.minecraft.util.Mth.floor(vec3.z()) >> 4), 1); // Paper - post teleport ticket type
    }

    /*@Inject(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void cardboard$clearRespawnIfBlocked(ServerPlayer player, boolean keepInventory, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
        ((ServerPlayerBridge)player).cardboard$setRespawnPosition(null, false, PlayerSetSpawnEvent.Cause.PLAYER_RESPAWN); // CraftBukkit - SPIGOT-5988: Clear respawn location when obstructed
    }*/

    @WrapOperation(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;teleport(DDDFF)V"))
    private void cardboard$respawnUseInternalTeleport(ServerGamePacketListenerImpl connection, double x, double y, double z, float yRot, float xRot, Operation<Void> original) {
        ((ServerGamePacketListenerImplBridge)connection).cardboard$internalTeleport(x, y, z, yRot, xRot);
    }

    @Inject(method = "respawn", at = @At("RETURN"))
    private void cardboard$onRespawnReturn(ServerPlayer player, boolean keepInventory, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir, @Share("respawnResult") LocalRef<ServerPlayer_RespawnResult> resultRef, @Share("fromLevel") LocalRef<ServerLevel> fromLevelRef, @Share("respawnReason") LocalRef<PlayerRespawnEvent.RespawnReason> respawnReasonRef) {
        ServerPlayer_RespawnResult result = resultRef.get();
        Level fromLevel = fromLevelRef.get();
        ServerLevel currentLevel = player.level();
        TeleportTransition teleportTransition = result.transition();
        RespawnReason respawnReason = respawnReasonRef.get();

        // Paper start - Once we not reuse the player entity we can remove this.
        // But we have to resend the player info as it's not marked as dirty
        //this.sendAllPlayerInfo(player); // Update health
        //player.onUpdateAbilities(); // Update inventory, etc
        // Paper end

        // Paper start
        // Save player file again if they were disconnected
        if (((ServerGamePacketListenerImplBridge)player.connection).isDisconnected()) {
            this.save(player);
        }

        // It's possible for respawn to be in a diff dimension
        if (fromLevel != currentLevel) {
            new org.bukkit.event.player.PlayerChangedWorldEvent((Player) ((EntityBridge)player).getBukkitEntity(), ((LevelBridge)fromLevel).cardboard$getWorld()).callEvent();
            player.triggerDimensionChangeTriggers(currentLevel);
        }

        // Call post respawn event
        new com.destroystokyo.paper.event.player.PlayerPostRespawnEvent(
                (Player) ((EntityBridge)player).getBukkitEntity(),
                org.bukkit.craftbukkit.util.CraftLocation.toBukkit(teleportTransition.position(), currentLevel, teleportTransition.yRot(), teleportTransition.xRot()),
                result.isBedSpawn(),
                result.isAnchorSpawn(),
                teleportTransition.missingRespawnBlock(),
                respawnReason
        ).callEvent();
        // Paper end
    }

    @Override
    public PlayerList_LoginResult cardboard$canPlayerLogin(SocketAddress socketAddress, NameAndId nameAndId) { // Paper - PlayerLoginEvent
        PlayerList_LoginResult whitelistEventResult; // Paper
        // Paper start - Fix MC-158900
        UserBanListEntry userBanListEntry;
        if (this.bans.isBanned(nameAndId) && (userBanListEntry = this.bans.get(nameAndId)) != null) {
            // Paper end - Fix MC-158900
            MutableComponent mutableComponent = Component.translatable("multiplayer.disconnect.banned.reason", userBanListEntry.getReasonMessage());
            if (userBanListEntry.getExpires() != null) {
                mutableComponent.append(
                        Component.translatable("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(userBanListEntry.getExpires()))
                );
            }

            return new PlayerList_LoginResult(mutableComponent, org.bukkit.event.player.PlayerLoginEvent.Result.KICK_BANNED); // Paper - PlayerLoginEvent
            // Paper start - whitelist event
        } else if ((whitelistEventResult = this.isWhiteListedLogin(nameAndId)).result() == org.bukkit.event.player.PlayerLoginEvent.Result.KICK_WHITELIST) {
            return whitelistEventResult;
            // Paper end
        } else if (this.ipBans.isBanned(socketAddress)) {
            IpBanListEntry ipBanListEntry = this.ipBans.get(socketAddress);
            MutableComponent mutableComponent = Component.translatable("multiplayer.disconnect.banned_ip.reason", ipBanListEntry.getReasonMessage());
            if (ipBanListEntry.getExpires() != null) {
                mutableComponent.append(
                        Component.translatable("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(ipBanListEntry.getExpires()))
                );
            }

            return new PlayerList_LoginResult(mutableComponent, org.bukkit.event.player.PlayerLoginEvent.Result.KICK_BANNED); // Paper - PlayerLoginEvent
        } else {
            return this.canBypassFullServerLogin(nameAndId, new PlayerList_LoginResult(Component.translatable("multiplayer.disconnect.server_full"), org.bukkit.event.player.PlayerLoginEvent.Result.KICK_FULL)); // Paper - PlayerServerFullCheckEvent
        }
    }

    // Paper start - whitelist verify event / login event
    @Unique
    public PlayerList_LoginResult canBypassFullServerLogin(final NameAndId nameAndId, final PlayerList_LoginResult currentResult) {
        final boolean shouldKick = this.players.size() >= this.getMaxPlayers() && !this.canBypassPlayerLimit(nameAndId);
        final io.papermc.paper.event.player.PlayerServerFullCheckEvent fullCheckEvent = new io.papermc.paper.event.player.PlayerServerFullCheckEvent(
                new com.destroystokyo.paper.profile.CraftPlayerProfile(nameAndId),
                io.papermc.paper.adventure.PaperAdventure.asAdventure(currentResult.message()),
                shouldKick
        );

        fullCheckEvent.callEvent();
        if (fullCheckEvent.isAllowed()) {
            return PlayerList_LoginResult.ALLOW;
        } else {
            return new PlayerList_LoginResult(
                    io.papermc.paper.adventure.PaperAdventure.asVanilla(fullCheckEvent.kickMessage()), currentResult.result()
            );
        }
    }

    @Unique
    public PlayerList_LoginResult isWhiteListedLogin(NameAndId nameAndId) {
        boolean isOp = this.ops.contains(nameAndId);
        boolean isWhitelisted = !this.isUsingWhitelist() || isOp || this.whitelist.contains(nameAndId);

        final net.kyori.adventure.text.Component configuredMessage = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(org.spigotmc.SpigotConfig.whitelistMessage);
        final com.destroystokyo.paper.event.profile.ProfileWhitelistVerifyEvent event
                = new com.destroystokyo.paper.event.profile.ProfileWhitelistVerifyEvent(new com.destroystokyo.paper.profile.CraftPlayerProfile(nameAndId), this.isUsingWhitelist(), isWhitelisted, isOp, configuredMessage);
        event.callEvent();
        if (!event.isWhitelisted()) {
            return new PlayerList_LoginResult(io.papermc.paper.adventure.PaperAdventure.asVanilla(event.kickMessage() == null ? configuredMessage : event.kickMessage()), org.bukkit.event.player.PlayerLoginEvent.Result.KICK_WHITELIST);
        }

        return PlayerList_LoginResult.ALLOW;
    }
    // Paper end

    // CraftBukkit start
    @Override
    public ServerStatsCounter cardboard$getPlayerStats(ServerPlayer player) {
        GameProfile gameProfile = player.getGameProfile();
        ServerStatsCounter playerStatsCounter = player.getStats();
        if (playerStatsCounter == null) {
            return this.cardboard$getPlayerStats(gameProfile);
        } else {
            return playerStatsCounter;
        }
    }

    @Override
    public ServerStatsCounter cardboard$getPlayerStats(GameProfile gameProfile) {
        Path path = this.locateStatsFile(gameProfile);
        return new ServerStatsCounter(this.server, path);
    }
    // CraftBukkit end

    @Override
    public void cardboard$reloadRecipes() {
        RecipeManager recipeManager = this.server.getRecipeManager();
        ClientboundUpdateRecipesPacket clientboundUpdateRecipesPacket = new ClientboundUpdateRecipesPacket(
                recipeManager.getSynchronizedItemProperties(), recipeManager.getSynchronizedStonecutterRecipes()
        );

        for (ServerPlayer serverPlayer : this.players) {
            serverPlayer.connection.send(clientboundUpdateRecipesPacket);
            serverPlayer.getRecipeBook().sendInitialRecipeBook(serverPlayer);
        }
    }
}
