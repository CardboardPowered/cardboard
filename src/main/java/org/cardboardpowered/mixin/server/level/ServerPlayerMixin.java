/**
 * Cardboard - Spigot/Paper API for Fabric
 * Copyright (C) 2020-2025 Cardboard contributors
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
package org.cardboardpowered.mixin.server.level;

import java.util.Optional;
import java.util.OptionalInt;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.MainHand;
import org.cardboardpowered.CardboardConfig;
import org.cardboardpowered.CardboardMod;
import org.cardboardpowered.bridge.server.network.ServerGamePacketListenerImplBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;
import org.cardboardpowered.extras.ServerPlayer_RespawnPosAngle;
import org.cardboardpowered.extras.ServerPlayer_RespawnResult;
import org.cardboardpowered.impl.world.CraftWorld;
import org.cardboardpowered.bridge.commands.CommandSourceBridge;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.inventory.AbstractContainerMenuBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.cardboardpowered.mixin.world.entity.player.PlayerMixin;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.impl.screenhandler.Networking;
*/

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.impl.menu.Networking;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayer.RespawnConfig;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = ServerPlayer.class, priority = 999)
public abstract class ServerPlayerMixin extends PlayerMixin implements CommandSourceBridge, ServerPlayerBridge {

	@Shadow
	private CommandSource commandSource;

    public CommandSource cb$get_command_output() {
		return commandSource;
	}

	public void cb$set_command_output(CommandSource out) {
		this.commandSource = out;
	}

	public void cb$set_bukkit_command_output(CommandSource out) {
		// this.commandOutput = out;
	
		this.commandSource = new CommandSource() {

			@Override
			public void sendSystemMessage(Component message) {
				out.sendSystemMessage(message);
			}

			@Override
			public boolean shouldInformAdmins() {
				return out.shouldInformAdmins();
			}

			@Override
			public boolean acceptsSuccess() {
				return out.acceptsSuccess();
			}

			@Override
			public boolean acceptsFailure() {
				// TODO Auto-generated method stub
				return false;
			}
			
			// @Override
            public CommandSender getBukkitSender(CommandSourceStack wrapper) {
                return ( (EntityBridge)  ((ServerPlayer) (Object) this) ) .getBukkitEntity();
            }
			
		};
		
	}

    public Connection connectionBF;

    @Shadow
    public int containerCounter;

    // CraftBukkit start
    @Override
    public org.bukkit.command.CommandSender getBukkitSender(CommandSourceStack wrapper) {
        return this.getBukkitEntity();
    }
    // CraftBukkit end

    @Override
    public org.bukkit.craftbukkit.entity.CraftHumanEntity getBukkitEntity() {
        return (org.bukkit.craftbukkit.entity.CraftHumanEntity) super.getBukkitEntity();
    }

    @Override
    public BlockPos getSpawnPoint(Level world) {
        return ((ServerLevel)world).getRespawnData().pos();
    }

    @Inject(at = @At("TAIL"), method = "disconnect")
    public void onDisconnect(CallbackInfo ci) {
        // CraftServer.INSTANCE.playerView.remove(this.bukkit);
    }
    
    private ServerLevel cb$from;
    
    @Inject(cancellable = true, at = @At(
    		value = "INVOKE",
    		target = "Lnet/minecraft/world/level/portal/TeleportTransition;newLevel()Lnet/minecraft/server/level/ServerLevel;"
    ), method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;")
    public void cardboard$do_teleport_event(TeleportTransition target, CallbackInfoReturnable<ServerPlayer> ci) {
    	if (CardboardConfig.DEBUG_PLAYER) {
    		CardboardMod.LOGGER.info("DEBUG: ServerPlayerEntity.cardboard$do_teleport_event called");
    	}

    	ServerPlayer thiz = (ServerPlayer) (Object) this;
    	cb$from = thiz.level(); // Cardboard - store from world

    	Location exit = CraftLocation.toBukkit(target.position(), ((LevelBridge)target.newLevel()).cardboard$getWorld());

    	PlayerTeleportEvent tpEvent = new PlayerTeleportEvent(
                (Player) this.getBukkitEntity(),
    			this.getBukkitEntity().getLocation(),
    			exit,
    			PlayerTeleportEvent.TeleportCause.UNKNOWN
    	);
        Bukkit.getPluginManager().callEvent(tpEvent);

        Location newExit = tpEvent.getTo();

        if (tpEvent.isCancelled() || null == newExit) {

        	if (CardboardConfig.DEBUG_PLAYER) {
        		CardboardMod.LOGGER.info("DEBUG: Teleport: EventCanceled?=" + tpEvent.isCancelled() + ", newExit=" + newExit);
        	}

            ci.setReturnValue(null);
            return;
        }

        if (!newExit.equals(exit)) {
        	// Set our new TeleportTarget
        	target.newLevel = ((CraftWorld)newExit.getWorld()).getHandle();
        	target.position = CraftLocation.toVec3(newExit);
        	target.deltaMovement = Vec3.ZERO;
        	target.yRot = newExit.getYaw();
        	target.xRot = newExit.getPitch();

        	if (CardboardConfig.DEBUG_PLAYER) {
        		CardboardMod.LOGGER.info("DEBUG: Teleport: Target=" + target);
        	}

        	/*
            target = new TeleportTarget(
            		((CraftWorld)newExit.getWorld()).getHandle(),
            		CraftLocation.toVec3D(newExit),
            		Vec3d.ZERO,
            		newExit.getYaw(),
            		newExit.getPitch(),
            		// target.missingRespawnBlock(),
            		// target.asPassenger(),
            		// Set.of(),
            		target.postTeleportTransition() // ,
            		// target.cause()
            );
            */
        }
    }

    @Inject(at = @At(
    		value = "RETURN"
    ), method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;")
    public void cardboard$do_world_change(TeleportTransition target, CallbackInfoReturnable<ServerPlayer> e) {
    	ServerPlayer thiz = (ServerPlayer) (Object) this;

    	if (thiz.isRemoved()) {
    		return;
    	}

    	ServerLevel serverWorld = target.newLevel();
		ResourceKey<Level> registryKey = cb$from.dimension();

		if (serverWorld.dimension() == registryKey) {
			return;
		}

		PlayerChangedWorldEvent changeEvent = new PlayerChangedWorldEvent((Player)this.getBukkitEntity(), ((LevelBridge)cb$from).cardboard$getWorld());
        CraftServer.INSTANCE.getPluginManager().callEvent(changeEvent);
    }

    /*
    @Inject(at = @At("HEAD"), method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V", cancellable = true)
    public void teleport1(ServerWorld worldserver, double x, double y, double z, float f, float f1, CallbackInfo ci) {
        PlayerTeleportEvent event = new PlayerTeleportEvent(this.getBukkitEntity(), this.getBukkitEntity().getLocation(), new Location(((IMixinWorld)worldserver).getCraftWorld(), x,y,z,f,f1), PlayerTeleportEvent.TeleportCause.UNKNOWN);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
    */

    // World Standard
    public String locale_BF = "en_us";

    @Inject(at = @At("HEAD"), method = "updateOptions")
    public void onUpdateOptions(ClientInformation options, CallbackInfo ci) {
        if(getMainArm() != options.mainHand()) {
            PlayerChangedMainHandEvent event = new PlayerChangedMainHandEvent((Player) getBukkitEntity(), ((ServerPlayer) (Object) this).getMainArm() == HumanoidArm.LEFT ? MainHand.LEFT : MainHand.RIGHT);
            CraftServer.INSTANCE.getPluginManager().callEvent(event);
        }

        if(!this.language.equals(options.language())) {
            PlayerLocaleChangeEvent event = new PlayerLocaleChangeEvent((Player) getBukkitEntity(), options.language());
            CraftServer.INSTANCE.getPluginManager().callEvent(event);
        }
    }

    @Shadow
    public void closeContainer() {
    }

    @Override
    public int cardboard$nextContainerCounter() {
        this.containerCounter = this.containerCounter % 100 + 1;
        return containerCounter; // CraftBukkit
    }

    /**/
    @Unique
    private final ThreadLocal<AbstractContainerMenu> fabric_openedScreenHandler = new ThreadLocal<>();

    private void fabric_replaceVanillaScreenPacket_include(ServerGamePacketListenerImpl networkHandler, Packet<?> packet, MenuProvider factory) {
        if (factory instanceof ExtendedMenuProvider) {
            AbstractContainerMenu handler = fabric_openedScreenHandler.get();

            if (handler.getType() instanceof ExtendedMenuType) { // TODO: 1.20.5: check ExtendedScreenHandlerType<?>
                Networking.sendOpenPacket((ServerPlayer) (Object) this, (ExtendedMenuProvider) factory, handler, containerCounter);
            } else {
                Identifier id = BuiltInRegistries.MENU.getKey(handler.getType());
                throw new IllegalArgumentException("[Fabric] Non-extended screen handler " + id + " must not be opened with an ExtendedScreenHandlerFactory!");
            }
        } else {
            // Use vanilla logic for non-extended screen handlers
            networkHandler.send(packet);
        }
    }

    @Inject(method = "openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;", at = @At("RETURN"))
    private void fabric_clearStoredScreenHandler_include(MenuProvider factory, CallbackInfoReturnable<OptionalInt> info) {
        fabric_openedScreenHandler.remove();
    }

    /**
     * @reason Inventory Open Event
     * @author Cardboard
     */
    @Inject(at = @At("HEAD"), method = "openMenu", cancellable = true)
    public void openHandledScreen_c(MenuProvider factory, CallbackInfoReturnable<OptionalInt> ci) {
        if (factory == null) {
            ci.setReturnValue(OptionalInt.empty());
        } else {
            this.cardboard$nextContainerCounter();
            AbstractContainerMenu container = factory.createMenu(this.containerCounter, ((ServerPlayer)(Object)this).inventory, ((ServerPlayer)(Object)this));

            if (container != null) {
                ((AbstractContainerMenuBridge)container).setTitle(factory.getDisplayName());

                boolean cancelled = false;
                final AbstractContainerMenu created = container;
                final com.mojang.datafixers.util.Pair<net.kyori.adventure.text.Component, AbstractContainerMenu> result = org.bukkit.craftbukkit.event.CraftEventFactory.callInventoryOpenEventWithTitle(((ServerPlayer)(Object)this), container, cancelled);
                container = result.getSecond();
                if (container == null && !cancelled) {
                    if (factory instanceof Container) {
                        ((Container) factory).stopOpen((ServerPlayer)(Object)this);
                    } else if (created instanceof ChestMenu) {
                        ((ChestMenu) created).getContainer().stopOpen((ServerPlayer)(Object)this);
                    }

                    ci.setReturnValue(OptionalInt.empty());
                }
            }
            if (container == null) {
                ci.setReturnValue(OptionalInt.empty());
            } else {
                ((ServerPlayer)(Object)this).containerMenu = container;
                
                /*From FabricAPI*/
                if (factory instanceof ExtendedMenuProvider) {
                    fabric_openedScreenHandler.set(container);
                } else if (container.getType() instanceof ExtendedMenuType) { // TODO: 1.20.5: check ExtendedScreenHandlerType<?>
                    Identifier id = BuiltInRegistries.MENU.getKey(container.getType());
                    throw new IllegalArgumentException("[Fabric] Extended screen handler " + id + " must be opened with an ExtendedScreenHandlerFactory!");
                }
                
                fabric_replaceVanillaScreenPacket_include(((ServerPlayer)(Object)this).connection,
                        new ClientboundOpenScreenPacket(container.containerId, container.getType(), factory.getDisplayName()),
                        factory);
                /*End*/

                ((ServerPlayer)(Object)this).initMenu(container);

                fabric_openedScreenHandler.remove();
                ci.setReturnValue(OptionalInt.of(this.containerCounter));
            }
        }
        ci.cancel();
    }

    // TODO: 1.19
    /*@Inject(at = @At("HEAD"), method = "onDeath", cancellable = true)
    public void bukkitizeDeath(DamageSource damagesource, CallbackInfo ci) {
        boolean flag = this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES);
        if (((ServerPlayerEntity)(Object)this).isRemoved()) {
            ci.cancel();
            return;
        }

        java.util.List<org.bukkit.inventory.ItemStack> loot = new java.util.ArrayList<org.bukkit.inventory.ItemStack>(((ServerPlayerEntity)(Object)this).inventory.size());
        boolean keepInventory = this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || ((ServerPlayerEntity)(Object)this).isSpectator();

        if (!keepInventory)
            for (DefaultedList<ItemStack> items : ((ServerPlayerEntity)(Object)this).inventory.combinedInventory)
                for (ItemStack item : items)
                    if (!item.isEmpty() && !EnchantmentHelper.hasVanishingCurse(item))
                        loot.add(CraftItemStack.asCraftMirror(item));

        // SPIGOT-5071: manually add player loot tables (SPIGOT-5195 - ignores keepInventory rule)
        this.dropLoot(damagesource, ((ServerPlayerEntity)(Object)this).playerHitTimer > 0);
        for (org.bukkit.inventory.ItemStack item : ((IMixinEntity)this).cardboard_getDrops()) loot.add(item);
        ((IMixinEntity)this).cardboard_getDrops().clear(); // SPIGOT-5188: make sure to clear

        Text defaultMessage = ((ServerPlayerEntity)(Object)this).getDamageTracker().getDeathMessage();

        String deathmessage = defaultMessage.getString();
        org.bukkit.event.entity.PlayerDeathEvent event = CraftEventFactory.callPlayerDeathEvent(((ServerPlayerEntity)(Object)this), loot, deathmessage, keepInventory);

        // SPIGOT-943 - only call if they have an inventory open
        if (((ServerPlayerEntity)(Object)this).currentScreenHandler != ((ServerPlayerEntity)(Object)this).playerScreenHandler) this.closeHandledScreen();

        String deathMessage = event.getDeathMessage();
        ServerPlayerEntity plr = ((ServerPlayerEntity)(Object)this);

        if ((deathMessage = event.getDeathMessage()) != null && deathMessage.length() > 0 && flag) {
            Text ichatbasecomponent = deathMessage.equals(deathmessage) ? plr.getDamageTracker().getDeathMessage() : CraftChatMessage.fromStringOrNull(deathMessage);
            plr.networkHandler.sendPacket(new DeathMessageS2CPacket(plr.getDamageTracker(), ichatbasecomponent), future -> {
                if (!future.isSuccess()) {
                    boolean flag1 = true;
                    String s = ichatbasecomponent.asTruncatedString(256);
                    TranslatableText chatmessage = new TranslatableText("death.attack.message_too_long", new LiteralText(s).formatted(Formatting.YELLOW));
                    MutableText ichatmutablecomponent = new TranslatableText("death.attack.even_more_magic", plr.getDisplayName()).styled(chatmodifier -> chatmodifier.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, chatmessage)));
                    plr.networkHandler.sendPacket(new DeathMessageS2CPacket(plr.getDamageTracker(), ichatmutablecomponent));
                }
            });
            AbstractTeam scoreboardteambase = plr.getScoreboardTeam();
            if (scoreboardteambase != null && scoreboardteambase.getDeathMessageVisibilityRule() != AbstractTeam.VisibilityRule.ALWAYS) {
                if (scoreboardteambase.getDeathMessageVisibilityRule() == AbstractTeam.VisibilityRule.HIDE_FOR_OTHER_TEAMS) {
                    plr.server.getPlayerManager().sendToTeam(plr, ichatbasecomponent);
                } else if (scoreboardteambase.getDeathMessageVisibilityRule() == AbstractTeam.VisibilityRule.HIDE_FOR_OWN_TEAM) {
                    plr.server.getPlayerManager().sendToOtherTeams(plr, ichatbasecomponent);
                }
            } else {
                plr.server.getPlayerManager().broadcast(ichatbasecomponent, MessageType.SYSTEM, Util.NIL_UUID);
            }
        } else {
            plr.networkHandler.sendPacket(new DeathMessageS2CPacket(plr.getDamageTracker(), LiteralText.EMPTY));
        }
        ((ServerPlayerEntity)(Object)this).dropShoulderEntities();
        if (this.world.getGameRules().getBoolean(GameRules.FORGIVE_DEAD_PLAYERS)) this.forgiveMobAnger();

        // SPIGOT-5478 must be called manually now
        ((ServerPlayerEntity)(Object)this).dropXp();
        // we clean the player's inventory after the EntityDeathEvent is called so plugins can get the exact state of the inventory.
        if (!event.getKeepInventory())  ((ServerPlayerEntity)(Object)this).inventory.clear();

        ((ServerPlayerEntity)(Object)this).setCameraEntity(((ServerPlayerEntity)(Object)this)); // Remove spectated target
        // CraftBukkit end

        // CraftBukkit - Get our scores instead
       // this.world.getServer().getScoreboard().get.getScoreboardScores(ScoreboardCriterion.DEATH_COUNT, ((ServerPlayerEntity)(Object)this).getEntityName(), ScoreboardPlayerScore::incrementScore);
        LivingEntity entityliving = ((ServerPlayerEntity)(Object)this).getPrimeAdversary();

        if (entityliving != null) {
            entityliving.updateKilledAdvancementCriterion(((ServerPlayerEntity)(Object)this), ((ServerPlayerEntity)(Object)this).scoreAmount, damagesource);
            ((ServerPlayerEntity)(Object)this).onKilledBy(entityliving);
        }

        this.world.sendEntityStatus(((ServerPlayerEntity)(Object)this), (byte) 3);

        ((ServerPlayerEntity)(Object)this).extinguish();
        ((ServerPlayerEntity)(Object)this).setFlag(0, false);
        ((ServerPlayerEntity)(Object)this).getDamageTracker().update();
        ci.cancel();
        return;
    }*/

    @Shadow
    public void tellNeutralMobsThatIDied() {}

    @Shadow public abstract OptionalInt openMenu(@Nullable MenuProvider factory);
    @Shadow private String language;

    @Shadow
    public abstract @org.jspecify.annotations.Nullable RespawnConfig getRespawnConfig();

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    private @org.jspecify.annotations.Nullable RespawnConfig respawnConfig;

    @Shadow
    @Final
    private static Component SPAWN_SET_MESSAGE;

    @Shadow
    public abstract void sendSystemMessage(Component component);

    @Shadow
    public ServerGamePacketListenerImpl connection;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Override
    public void setConnectionBF(Connection connection) {
        this.connectionBF = connection;
    }

    @Override
    public Connection getConnectionBF() {
        return this.connectionBF;
    }

    private int oldLevel = -1;
    private float h = 0;

    @Inject(at = @At("TAIL"), method = "doTick")
    public void doBukkitEvent_PlayerLevelChangeEvent(CallbackInfo ci) {
        //ServerPlayerEntity plr = ((ServerPlayerEntity)(Object)this);

        try {
            if (this.oldLevel == -1) this.oldLevel = ((ServerPlayer)(Object)this).experienceLevel;
            if (this.oldLevel != ((ServerPlayer)(Object)this).experienceLevel) {
                CraftEventFactory.callPlayerLevelChangeEvent((Player) getBukkitEntity(), this.oldLevel, ((ServerPlayer)(Object)this).experienceLevel);
                this.oldLevel = ((ServerPlayer)(Object)this).experienceLevel;
            }
        } catch (Throwable throwable) {}
    }

    @Inject(at = @At("HEAD"), method = "closeContainer")
    public void cardboard_doInventoryCloseEvent(CallbackInfo ci) {
        org.bukkit.craftbukkit.event.CraftEventFactory.handleInventoryCloseEvent(((ServerPlayer)(Object)this), InventoryCloseEvent.Reason.UNKNOWN); // CraftBukkit
    }

    @Override
    public void spawnIn(ServerLevel level) {
    	if (level == null) {
    		throw new IllegalArgumentException("level can't be null");
    	} else {
    		ServerPlayer plr = ((ServerPlayer)(Object)this);
    		plr.setServerLevel(level);
    		plr.gameMode.setLevel(level);
    	}
    }

	// SPIGOT-1903, MC-98153
	@Override
	public void spigot$forceSetPositionRotation(double x, double y, double z, float yaw, float pitch) {
		((ServerPlayer)(Object)this).snapTo(x, y, z, yaw, pitch);
		((ServerPlayer)(Object)this).connection.resetPosition();
    }

    // Paper start
    @Override
    public @org.jspecify.annotations.Nullable ServerPlayer_RespawnResult cardboard$findRespawnPositionAndUseSpawnBlock0(boolean useCharge, TeleportTransition.PostTeleportTransition postTeleportTransition, org.bukkit.event.player.PlayerRespawnEvent.RespawnReason respawnReason) {
        TeleportTransition teleportTransition;
        boolean isBedSpawn = false;
        boolean isAnchorSpawn = false;
        Runnable consumeAnchorCharge = null;
        // Paper end
        ServerPlayer.RespawnConfig respawnConfig = this.getRespawnConfig();
        ServerLevel level = this.server.getLevel(ServerPlayer.RespawnConfig.getDimensionOrDefault(respawnConfig));
        if (level != null && respawnConfig != null) {
            Optional<ServerPlayer_RespawnPosAngle> optional = ServerPlayerBridge.cardboard$findRespawnAndUseSpawnBlock(level, respawnConfig, useCharge);
            if (optional.isPresent()) {
                ServerPlayer_RespawnPosAngle respawnPosAngle = optional.get();
                // CraftBukkit start
                isBedSpawn = respawnPosAngle.isBedSpawn();
                isAnchorSpawn = respawnPosAngle.isAnchorSpawn();
                consumeAnchorCharge = respawnPosAngle.consumeAnchorCharge();
                teleportTransition = new TeleportTransition(
                        level, respawnPosAngle.position(), Vec3.ZERO, respawnPosAngle.yaw(), respawnPosAngle.pitch(), postTeleportTransition
                );
                // CraftBukkit end
            } else {
                teleportTransition = TeleportTransition.missingRespawnBlock(((ServerPlayer)(Object)this), postTeleportTransition); // CraftBukkit
            }
        } else {
            // CraftBukkit start
            teleportTransition = TeleportTransition.createDefault(((ServerPlayer)(Object)this), postTeleportTransition);
        }

        org.bukkit.entity.Player respawnPlayer = (Player) this.getBukkitEntity();
        org.bukkit.Location location = org.bukkit.craftbukkit.util.CraftLocation.toBukkit(
                teleportTransition.position(),
                teleportTransition.newLevel(),
                teleportTransition.yRot(),
                teleportTransition.xRot()
        );

        // Paper start - respawn flags
        org.bukkit.event.player.PlayerRespawnEvent respawnEvent = new org.bukkit.event.player.PlayerRespawnEvent(
                respawnPlayer,
                location,
                isBedSpawn,
                isAnchorSpawn,
                teleportTransition.missingRespawnBlock(),
                respawnReason
        );
        // Paper end - respawn flags
        CraftServer.INSTANCE.getPluginManager().callEvent(respawnEvent);
        // Spigot start
        if (((ServerGamePacketListenerImplBridge)this.connection).isDisconnected()) {
            return null;
        }
        // Spigot end

        // Paper start - consume anchor charge if location hasn't changed
        if (location.equals(respawnEvent.getRespawnLocation()) && consumeAnchorCharge != null) {
            consumeAnchorCharge.run();
        }
        // Paper end - consume anchor charge if location hasn't changed
        location = respawnEvent.getRespawnLocation();

        return new ServerPlayer_RespawnResult(
                new TeleportTransition(
                        ((CraftWorld) location.getWorld()).getHandle(),
                        org.bukkit.craftbukkit.util.CraftLocation.toVec3(location),
                        teleportTransition.deltaMovement(),
                        location.getYaw(),
                        location.getPitch(),
                        teleportTransition.missingRespawnBlock(),
                        teleportTransition.asPassenger(),
                        teleportTransition.relatives(),
                        teleportTransition.postTeleportTransition()
                        //teleportTransition.cause()
                ),
                isBedSpawn,
                isAnchorSpawn
        );
        // CraftBukkit end
    }

    @Override
    public boolean cardboard$drop(boolean dropStack) { // Paper - add back success return
        Inventory inventory = this.getInventory();
        ItemStack itemStack = inventory.removeFromSelected(dropStack);
        this.containerMenu
                .findSlot(inventory, inventory.getSelectedSlot())
                .ifPresent(slot -> this.containerMenu.setRemoteSlot(slot, inventory.getSelectedItem()));
        if (this.useItem.isEmpty()) {
            this.stopUsingItem();
        }

        return this.cardboard$drop(itemStack, false, true) != null; // Paper - add back success return
    }


    // private ItemEntity cardboard_stored_entity;

	/*
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;setPickupDelay(I)V"),
            method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;")
    public void store_item_entity(ItemEntity ie, int i, net.minecraft.item.ItemStack stack, boolean z, boolean z2) {
        ie.setPickupDelay(i);
        cardboard_stored_entity = ie;
    }
    */

    @SuppressWarnings("deprecation")
    @Inject(at = @At("RETURN"),
            method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void cardboard_doPlayerDropItemEvent(
            net.minecraft.world.item.ItemStack stack,
            boolean throwRandomly,
            boolean retainOwnership,
            CallbackInfoReturnable<ItemEntity> ci,
            @Local ItemEntity itemEntity
    ) {
        if (stack.isEmpty()) {
            return;
        }
        Player player = (Player)(((EntityBridge)this).getBukkitEntity());
        Item drop = (Item) ((EntityBridge)itemEntity).getBukkitEntity();
        PlayerDropItemEvent event = new PlayerDropItemEvent(player, drop);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            org.bukkit.inventory.ItemStack cur = player.getInventory().getItemInHand();
            if (retainOwnership && (cur == null || cur.getAmount() == 0)) {
                player.getInventory().setItemInHand(drop.getItemStack());
            } else if (retainOwnership && cur.isSimilar(drop.getItemStack()) && cur.getAmount() < cur.getMaxStackSize() && drop.getItemStack().getAmount() == 1) {
                cur.setAmount(cur.getAmount() + 1);
                player.getInventory().setItemInHand(cur);
            } else player.getInventory().addItem(drop.getItemStack());

            itemEntity = null;
            ci.setReturnValue(null);
        }
        // cardboard_stored_entity = null;
    }

    @Inject(method = "setRespawnPosition", at = @At("HEAD"), cancellable = true)
    public void setRespawnPositionPaper(RespawnConfig respawnConfig, boolean displayInChat, CallbackInfo ci) {
        // Paper start - Add PlayerSetSpawnEvent
        this.cardboard$setRespawnPosition(respawnConfig, displayInChat, com.destroystokyo.paper.event.player.PlayerSetSpawnEvent.Cause.UNKNOWN);
        ci.cancel();
    }

    @Override
    public boolean cardboard$setRespawnPosition(ServerPlayer.@org.jspecify.annotations.Nullable RespawnConfig respawnConfig, boolean displayInChat, com.destroystokyo.paper.event.player.PlayerSetSpawnEvent.Cause cause) {
        org.bukkit.Location spawnLoc = null;
        boolean actuallyDisplayInChat = false;
        if (respawnConfig != null) {
            actuallyDisplayInChat = displayInChat && !respawnConfig.isSamePosition(this.respawnConfig);
            spawnLoc = org.bukkit.craftbukkit.util.CraftLocation.toBukkit(respawnConfig.respawnData().pos(), this.server.getLevel(respawnConfig.respawnData().dimension()));
            spawnLoc.setYaw(respawnConfig.respawnData().yaw());
            spawnLoc.setPitch(respawnConfig.respawnData().pitch());
        }
        org.bukkit.event.player.PlayerSpawnChangeEvent dumbEvent = new org.bukkit.event.player.PlayerSpawnChangeEvent(
                (Player) this.getBukkitEntity(),
                spawnLoc,
                respawnConfig != null && respawnConfig.forced(),
                cause == com.destroystokyo.paper.event.player.PlayerSetSpawnEvent.Cause.PLAYER_RESPAWN
                        ? org.bukkit.event.player.PlayerSpawnChangeEvent.Cause.RESET
                        : org.bukkit.event.player.PlayerSpawnChangeEvent.Cause.valueOf(cause.name())
        );
        dumbEvent.callEvent();

        com.destroystokyo.paper.event.player.PlayerSetSpawnEvent event = new com.destroystokyo.paper.event.player.PlayerSetSpawnEvent(
                (Player) this.getBukkitEntity(),
                cause,
                dumbEvent.getNewSpawn(),
                dumbEvent.isForced(),
                actuallyDisplayInChat,
                actuallyDisplayInChat ? io.papermc.paper.adventure.PaperAdventure.asAdventure(SPAWN_SET_MESSAGE) : null
        );
        event.setCancelled(dumbEvent.isCancelled());
        if (!event.callEvent()) {
            return false;
        }

        if (event.getLocation() != null) {
            respawnConfig = new ServerPlayer.RespawnConfig(
                    net.minecraft.world.level.storage.LevelData.RespawnData.of(
                            ((CraftWorld) event.getLocation().getWorld()).getHandle().dimension(),
                            org.bukkit.craftbukkit.util.CraftLocation.toBlockPosition(event.getLocation()),
                            event.getLocation().getYaw(),
                            event.getLocation().getPitch()
                    ),
                    event.isForced()
            );
            if (event.willNotifyPlayer() && event.getNotification() != null) {
                this.sendSystemMessage(io.papermc.paper.adventure.PaperAdventure.asVanilla(event.getNotification()));
            }
        }

        this.respawnConfig = respawnConfig;
        return true;
        // Paper end - Add PlayerSetSpawnEvent
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ValueInput;read(Ljava/lang/String;Lcom/mojang/serialization/Codec;)Ljava/util/Optional;", ordinal = 2, shift = At.Shift.AFTER))
    protected void readAdditionalSaveDataPaper(ValueInput valueInput, CallbackInfo ci) {
        ((CraftPlayer)this.getBukkitEntity()).readExtraData(valueInput); // CraftBukkit
    }

    @Inject(method = "addAdditionalSaveData", at = @At(value = "TAIL"))
    protected void addAdditionalSaveDataPaper(ValueOutput valueOutput, CallbackInfo ci) {
        ((CraftPlayer)this.getBukkitEntity()).setExtraData(valueOutput); // CraftBukkit
    }
}
