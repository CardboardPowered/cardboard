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

import java.util.OptionalInt;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.MainHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinCommandOutput;
import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import com.mojang.authlib.GameProfile;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.s2c.play.CombatEventS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Arm;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

@Mixin(value = ServerPlayerEntity.class, priority = 999)
public class MixinPlayer extends MixinLivingEntity implements IMixinCommandOutput, IMixinServerEntityPlayer  {

    private PlayerImpl bukkit;

    public ClientConnection connectionBF;

    @Shadow
    public int screenHandlerSyncId;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(MinecraftServer server, ServerWorld world, GameProfile profile, ServerPlayerInteractionManager interactionManager, CallbackInfo ci) {
        if (null != Bukkit.getPlayer(((ServerPlayerEntity)(Object)this).getUuid())) {
            this.bukkit = (PlayerImpl) Bukkit.getPlayer(((ServerPlayerEntity)(Object)this).getUuid());
            this.bukkit.setHandle((ServerPlayerEntity)(Object)this);
        } else {
            this.bukkit = new PlayerImpl((ServerPlayerEntity)(Object)this);
            CraftServer.INSTANCE.playerView.add(this.bukkit);
        }
    }

    @Override
    public CommandSender getBukkitSender(ServerCommandSource serverCommandSource) {
        return bukkit;
    }

    @Override
    public CraftEntity getBukkitEntity() {
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

    @Inject(at = @At("HEAD"), method = "teleport", cancellable = true)
    public void teleport(ServerWorld worldserver, double x, double y, double z, float f, float f1, CallbackInfo ci) {
        PlayerTeleportEvent event = new PlayerTeleportEvent((Player) this.getBukkitEntity(), this.getBukkitEntity().getLocation(), new Location(((IMixinWorld)worldserver).getWorldImpl(), x,y,z,f,f1), PlayerTeleportEvent.TeleportCause.UNKNOWN);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
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

    /**
     * @reason Inventory Open Event
     * @author BukkitFabricMod
     */
    @Inject(at = @At("HEAD"), method = "openHandledScreen", cancellable = true)
    public void openHandledScreen(NamedScreenHandlerFactory itileinventory, CallbackInfoReturnable<OptionalInt> ci) {
        if (itileinventory == null) {
            ci.setReturnValue(OptionalInt.empty());
        } else {
            if (((ServerPlayerEntity)(Object)this).currentScreenHandler != ((ServerPlayerEntity)(Object)this).playerScreenHandler)
                this.closeHandledScreen();

            this.nextContainerCounter();
            ScreenHandler container = itileinventory.createMenu(this.screenHandlerSyncId, ((ServerPlayerEntity)(Object)this).inventory, ((ServerPlayerEntity)(Object)this));

            if (container != null) {
                ((IMixinScreenHandler)container).setTitle(itileinventory.getDisplayName());

                boolean cancelled = false;
                container = BukkitEventFactory.callInventoryOpenEvent((ServerPlayerEntity)(Object)this, container, cancelled);
                if (container == null && !cancelled) {
                    if (itileinventory instanceof Inventory) {
                        ((Inventory) itileinventory).onClose((ServerPlayerEntity)(Object)this);
                    } else if (itileinventory instanceof DoubleInventory)
                        ((DoubleInventory) itileinventory).first.onClose((ServerPlayerEntity)(Object)this);
                    ci.setReturnValue(OptionalInt.empty());
                }
            }
            if (container == null)
                ci.setReturnValue(OptionalInt.empty());
            else {
                ((ServerPlayerEntity)(Object)this).currentScreenHandler = container;
                ((ServerPlayerEntity)(Object)this).networkHandler.sendPacket(new OpenScreenS2CPacket(container.syncId, container.getType(), ((IMixinScreenHandler)container).getTitle()));
                container.addListener(((ServerPlayerEntity)(Object)this));
                ci.setReturnValue(OptionalInt.of(this.screenHandlerSyncId));
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "onDeath", cancellable = true)
    public void bukkitizeDeath(DamageSource damagesource, CallbackInfo ci) {
        boolean flag = this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES);
        if (((ServerPlayerEntity)(Object)this).removed) {
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
        for (org.bukkit.inventory.ItemStack item : this.drops) loot.add(item);
        drops.clear(); // SPIGOT-5188: make sure to clear

        Text defaultMessage = ((ServerPlayerEntity)(Object)this).getDamageTracker().getDeathMessage();

        String deathmessage = defaultMessage.getString();
        org.bukkit.event.entity.PlayerDeathEvent event = BukkitEventFactory.callPlayerDeathEvent(((ServerPlayerEntity)(Object)this), loot, deathmessage, keepInventory);

        // SPIGOT-943 - only call if they have an inventory open
        if (((ServerPlayerEntity)(Object)this).currentScreenHandler != ((ServerPlayerEntity)(Object)this).playerScreenHandler) this.closeHandledScreen();

        String deathMessage = event.getDeathMessage();

        if (deathMessage != null && deathMessage.length() > 0 && flag) { // TODO: allow plugins to override?
            Text ichatbasecomponent = deathMessage.equals(deathmessage) ? ((ServerPlayerEntity)(Object)this).getDamageTracker().getDeathMessage() : CraftChatMessage.fromStringOrNull(deathMessage);
            ((ServerPlayerEntity)(Object)this).networkHandler.sendPacket((Packet) (new CombatEventS2CPacket(((ServerPlayerEntity)(Object)this).getDamageTracker(), CombatEventS2CPacket.Type.ENTITY_DIED, ichatbasecomponent)), (future) -> {
                if (!future.isSuccess()) {
                    boolean flag1 = true;
                    String s = ichatbasecomponent.asTruncatedString(256);
                    TranslatableText chatmessage = new TranslatableText("death.attack.message_too_long", new Object[]{(new LiteralText(s)).formatted(Formatting.GOLD)});
                    MutableText ichatmutablecomponent = (new TranslatableText("death.attack.even_more_magic", new Object[]{((ServerPlayerEntity)(Object)this).getDisplayName()})).styled((chatmodifier) -> {
                        return chatmodifier.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, chatmessage));
                    });
                    ((ServerPlayerEntity)(Object)this).networkHandler.sendPacket(new CombatEventS2CPacket(((ServerPlayerEntity)(Object)this).getDamageTracker(), CombatEventS2CPacket.Type.ENTITY_DIED, ichatmutablecomponent));
                }

            });
            AbstractTeam scoreboardteambase = ((ServerPlayerEntity)(Object)this).getScoreboardTeam();

            if (scoreboardteambase != null && scoreboardteambase.getDeathMessageVisibilityRule() != AbstractTeam.VisibilityRule.ALWAYS) {
                if (scoreboardteambase.getDeathMessageVisibilityRule() == AbstractTeam.VisibilityRule.HIDE_FOR_OTHER_TEAMS) {
                    CraftServer.server.getPlayerManager().sendToTeam((PlayerEntity)(Object) this, ichatbasecomponent);
                } else if (scoreboardteambase.getDeathMessageVisibilityRule() == AbstractTeam.VisibilityRule.HIDE_FOR_OWN_TEAM)
                    CraftServer.server.getPlayerManager().sendToOtherTeams(((ServerPlayerEntity)(Object)this), ichatbasecomponent);
            } else CraftServer.server.getPlayerManager().broadcastChatMessage(ichatbasecomponent, MessageType.SYSTEM, Util.NIL_UUID);
        } else ((ServerPlayerEntity)(Object)this).networkHandler.sendPacket(new CombatEventS2CPacket(((ServerPlayerEntity)(Object)this).getDamageTracker(), CombatEventS2CPacket.Type.ENTITY_DIED));

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
    }

    @Shadow
    public void forgiveMobAnger() {}

    @Override
    public void setConnectionBF(ClientConnection connection) {
        this.connectionBF = connection;
    }

    @Override
    public ClientConnection getConnectionBF() {
        return this.connectionBF;
    }

    @Overwrite
    public void copyFrom(ServerPlayerEntity entityplayer, boolean flag) {
        if (flag) {
            ((ServerPlayerEntity)(Object)this).inventory.clone(entityplayer.inventory);
            ((ServerPlayerEntity)(Object)this).setHealth(entityplayer.getHealth());
            ((ServerPlayerEntity)(Object)this).hungerManager = entityplayer.hungerManager;
            ((ServerPlayerEntity)(Object)this).experienceLevel = entityplayer.experienceLevel;
            ((ServerPlayerEntity)(Object)this).totalExperience = entityplayer.totalExperience;
            ((ServerPlayerEntity)(Object)this).experienceProgress = entityplayer.experienceProgress;
            ((ServerPlayerEntity)(Object)this).setScore(entityplayer.getScore());
            ((ServerPlayerEntity)(Object)this).lastNetherPortalPosition = entityplayer.lastNetherPortalPosition;
        } else if (((ServerPlayerEntity)(Object)this).world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || entityplayer.isSpectator()) {
            ((ServerPlayerEntity)(Object)this).inventory.clone(entityplayer.inventory);
            ((ServerPlayerEntity)(Object)this).experienceLevel = entityplayer.experienceLevel;
            ((ServerPlayerEntity)(Object)this).totalExperience = entityplayer.totalExperience;
            ((ServerPlayerEntity)(Object)this).experienceProgress = entityplayer.experienceProgress;
            ((ServerPlayerEntity)(Object)this).setScore(entityplayer.getScore());
        }
        ((ServerPlayerEntity)(Object)this).enderChestInventory = entityplayer.enderChestInventory;
        ((ServerPlayerEntity)(Object)this).getDataTracker().set(ServerPlayerEntity.PLAYER_MODEL_PARTS, entityplayer.getDataTracker().get(ServerPlayerEntity.PLAYER_MODEL_PARTS));
        ((ServerPlayerEntity)(Object)this).syncedExperience = -1;
        ((ServerPlayerEntity)(Object)this).syncedHealth = -1.0F;
        ((ServerPlayerEntity)(Object)this).syncedFoodLevel = -1;
        ((ServerPlayerEntity)(Object)this).removedEntities.addAll(entityplayer.removedEntities);
        ((ServerPlayerEntity)(Object)this).seenCredits = entityplayer.seenCredits;
        ((ServerPlayerEntity)(Object)this).enteredNetherPos = entityplayer.enteredNetherPos;
        ((ServerPlayerEntity)(Object)this).setShoulderEntityLeft(entityplayer.getShoulderEntityLeft());
        ((ServerPlayerEntity)(Object)this).setShoulderEntityRight(entityplayer.getShoulderEntityRight());

    }

}