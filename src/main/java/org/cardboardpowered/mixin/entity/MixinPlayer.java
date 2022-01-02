/**
 * Cardboard - Spigot/Paper API for Fabric
 * Copyright (C) 2020-2021 Cardboard contributors
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
package org.cardboardpowered.mixin.entity;

import java.util.OptionalInt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.MainHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinCommandOutput;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType;
import net.fabricmc.fabric.impl.screenhandler.Networking;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Arm;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

@Mixin(value = ServerPlayerEntity.class, priority = 999)
public class MixinPlayer extends MixinLivingEntity implements IMixinCommandOutput, IMixinServerEntityPlayer  {

    private PlayerImpl bukkit;

    public ClientConnection connectionBF;

    @Shadow
    public int screenHandlerSyncId;

    @Override
    public void setBukkit(PlayerImpl plr) {
        this.bukkit = plr;
    }

    @Override
    public PlayerImpl getBukkit() {
        return bukkit;
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
    public void teleport1(ServerWorld worldserver, double x, double y, double z, float f, float f1, CallbackInfo ci) {
        PlayerTeleportEvent event = new PlayerTeleportEvent((Player) this.getBukkitEntity(), this.getBukkitEntity().getLocation(), new Location(((IMixinWorld)worldserver).getWorldImpl(), x,y,z,f,f1), PlayerTeleportEvent.TeleportCause.UNKNOWN);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
    }

    public String locale_BF = "en_us";

    @Inject(at = @At("HEAD"), method = "setClientSettings")
    public void setClientSettings(ClientSettingsC2SPacket packet, CallbackInfo ci) {
        PlayerChangedMainHandEvent event = new PlayerChangedMainHandEvent((Player) getBukkitEntity(), ((ServerPlayerEntity) (Object) this).getMainArm() == Arm.LEFT ? MainHand.LEFT : MainHand.RIGHT);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
    }

    @Shadow
    public void closeHandledScreen() {
    }

    @Override
    public int nextContainerCounter() {
        this.screenHandlerSyncId = this.screenHandlerSyncId % 100 + 1;
        return screenHandlerSyncId; // CraftBukkit
    }

    /**/
    @Unique
    private final ThreadLocal<ScreenHandler> fabric_openedScreenHandler = new ThreadLocal<>();

    private void fabric_replaceVanillaScreenPacket_include(ServerPlayNetworkHandler networkHandler, Packet<?> packet, NamedScreenHandlerFactory factory) {
        if (factory instanceof ExtendedScreenHandlerFactory) {
            ScreenHandler handler = fabric_openedScreenHandler.get();

            if (handler.getType() instanceof ExtendedScreenHandlerType<?>) {
                Networking.sendOpenPacket((ServerPlayerEntity) (Object) this, (ExtendedScreenHandlerFactory) factory, handler, screenHandlerSyncId);
            } else {
                Identifier id = Registry.SCREEN_HANDLER.getId(handler.getType());
                throw new IllegalArgumentException("[Fabric] Non-extended screen handler " + id + " must not be opened with an ExtendedScreenHandlerFactory!");
            }
        } else {
            // Use vanilla logic for non-extended screen handlers
            networkHandler.sendPacket(packet);
        }
    }

    @Inject(method = "openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;", at = @At("RETURN"))
    private void fabric_clearStoredScreenHandler_include(NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> info) {
        fabric_openedScreenHandler.remove();
    }

    /**
     * @reason Inventory Open Event
     * @author Cardboard
     */
    @Inject(at = @At("HEAD"), method = "openHandledScreen", cancellable = true)
    public void openHandledScreen_c(NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> ci) {
        if (factory == null) {
            ci.setReturnValue(OptionalInt.empty());
        } else {
            this.nextContainerCounter();
            ScreenHandler container = factory.createMenu(this.screenHandlerSyncId, ((ServerPlayerEntity)(Object)this).inventory, ((ServerPlayerEntity)(Object)this));

            if (container != null) {
                ((IMixinScreenHandler)container).setTitle(factory.getDisplayName());

                boolean cancelled = false;
                container = BukkitEventFactory.callInventoryOpenEvent((ServerPlayerEntity)(Object)this, container, cancelled);
                if (container == null && !cancelled) {
                    if (factory instanceof Inventory) {
                        ((Inventory) factory).onClose((ServerPlayerEntity)(Object)this);
                    } else if (factory instanceof DoubleInventory)
                        ((DoubleInventory) factory).first.onClose((ServerPlayerEntity)(Object)this);

                    ci.setReturnValue(OptionalInt.empty());
                }
            }
            if (container == null) {
                ci.setReturnValue(OptionalInt.empty());
            } else {
                ((ServerPlayerEntity)(Object)this).currentScreenHandler = container;
                
                /*From FabricAPI*/
                if (factory instanceof ExtendedScreenHandlerFactory) {
                    fabric_openedScreenHandler.set(container);
                } else if (container.getType() instanceof ExtendedScreenHandlerType<?>) {
                    Identifier id = Registry.SCREEN_HANDLER.getId(container.getType());
                    throw new IllegalArgumentException("[Fabric] Extended screen handler " + id + " must be opened with an ExtendedScreenHandlerFactory!");
                }
                
                fabric_replaceVanillaScreenPacket_include(((ServerPlayerEntity)(Object)this).networkHandler,
                        new OpenScreenS2CPacket(container.syncId, container.getType(), factory.getDisplayName()),
                        factory);
                /*End*/

                if ( CraftServer.INSTANCE.getMinecraftVersion().contains("1.16") ) {
                    // 1.16.5
                    container.addListener((ScreenHandlerListener) ((ServerPlayerEntity)(Object)this));
                } else {
                    // 1.17
                    ((ServerPlayerEntity)(Object)this).onScreenHandlerOpened(container);
                }

                fabric_openedScreenHandler.remove();
                ci.setReturnValue(OptionalInt.of(this.screenHandlerSyncId));
            }
        }
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "onDeath", cancellable = true)
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
        org.bukkit.event.entity.PlayerDeathEvent event = BukkitEventFactory.callPlayerDeathEvent(((ServerPlayerEntity)(Object)this), loot, deathmessage, keepInventory);

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

    private int oldLevel = -1;
    private float h = 0;

    @Inject(at = @At("TAIL"), method = "playerTick")
    public void doBukkitEvent_PlayerLevelChangeEvent(CallbackInfo ci) {
        //ServerPlayerEntity plr = ((ServerPlayerEntity)(Object)this);

        // Avoid suffocation on join
        /*BlockPos saved = bukkit.posAtLogin;
        if (null != saved && plr.age > 8) {
            if (plr.age < 60) {
                if (h == 0) h = plr.getHealth();
                 plr.setInvulnerable(true);
                BlockPos pos = plr.getBlockPos();
                if (Math.abs(saved.x-pos.x) <= 1 && Math.abs(saved.z-pos.z) <= 1) {
                    if (!plr.getServerWorld().getBlockState(new BlockPos(pos.x, pos.y+1, pos.z)).isAir()) {
                        int ty = saved.getY();
                        while (!plr.getServerWorld().getBlockState(new BlockPos(pos.x, ty, pos.z)).isAir()) { ty++; }
                        plr.teleport(saved.x, ty, saved.z);
                    }
                }
                plr.setHealth(h);
            } else if (plr.age < 80) {
                plr.setInvulnerable(bukkit.in);
            }
        }*/
        // end

        try {
            if (this.oldLevel == -1) this.oldLevel = ((ServerPlayerEntity)(Object)this).experienceLevel;
            if (this.oldLevel != ((ServerPlayerEntity)(Object)this).experienceLevel) {
                BukkitEventFactory.callPlayerLevelChangeEvent((Player)getBukkitEntity(), this.oldLevel, ((ServerPlayerEntity)(Object)this).experienceLevel);
                this.oldLevel = ((ServerPlayerEntity)(Object)this).experienceLevel;
            }
        } catch (Throwable throwable) {}
    }

    //@Overwrite
    public void copyFrom_unused(ServerPlayerEntity entityplayer, boolean flag) {
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
        //((ServerPlayerEntity)(Object)this).removedEntities.addAll(entityplayer.removedEntities);
        ((ServerPlayerEntity)(Object)this).seenCredits = entityplayer.seenCredits;
        ((ServerPlayerEntity)(Object)this).enteredNetherPos = entityplayer.enteredNetherPos;
        //((ServerPlayerEntity)(Object)this).setShoulderEntityLeft(entityplayer.getShoulderEntityLeft());
        //((ServerPlayerEntity)(Object)this).setShoulderEntityRight(entityplayer.getShoulderEntityRight());

    }
    
    @Inject(at = @At("HEAD"), method = "closeHandledScreen")
    public void cardboard_doInventoryCloseEvent(CallbackInfo ci) {
        IMixinScreenHandler handler = (IMixinScreenHandler) ((ServerPlayerEntity)(Object)this).currentScreenHandler;
        CardboardInventoryView view = handler.getBukkitView();
        view.setPlayerIfNotSet(getBukkit());
        InventoryCloseEvent event = new InventoryCloseEvent(view);
        Bukkit.getPluginManager().callEvent(event);
        handler.transferTo(((ServerPlayerEntity)(Object)this).playerScreenHandler, (CraftHumanEntity) getBukkitEntity());
    }

}