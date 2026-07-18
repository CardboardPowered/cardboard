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
package org.bukkit.craftbukkit.event;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import org.bukkit.craftbukkit.*;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.cardboardpowered.CardboardMod;
import org.cardboardpowered.extras.PlayerList_LoginResult;
import org.cardboardpowered.BukkitLogger;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.ContainerBridge;
import org.cardboardpowered.bridge.world.entity.LivingEntityBridge;
import org.cardboardpowered.bridge.server.MinecraftServerBridge;
import org.cardboardpowered.bridge.world.inventory.AbstractContainerMenuBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;

import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.connection.PlayerConnection;
import io.papermc.paper.event.block.BellRingEvent;
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent;
import io.papermc.paper.event.world.PaperWorldGameRuleChangeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.CraftBlockStates;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Firework;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.VillagerCareerChangeEvent.ChangeReason;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.cardboardpowered.impl.entity.UnknownEntity;
import org.cardboardpowered.impl.world.CraftWorld;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class CraftEventFactory {

    public static Entity entityDamage;

    /**
     */
    public static void callEvent(Event e) {
        if (!e.isAsynchronous() && !Bukkit.isPrimaryThread()) {
            ((MinecraftServerBridge)CraftServer.server).cardboard_runOnMainThread(() -> {
                CraftServer.INSTANCE.getPluginManager().callEvent(e);
            });
            return;
        }
        CraftServer.INSTANCE.getPluginManager().callEvent(e);
    }

    public static ServerListPingEvent callServerListPingEvent(Server craftServer, InetAddress address, String motd, int numPlayers, int maxPlayers) {
    	ServerListPingEvent event =  new ServerListPingEvent("", address, motd, numPlayers, maxPlayers);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockPlaceEvent callBlockPlaceEvent(ServerLevel level, net.minecraft.world.entity.player.Player player, InteractionHand hand, BlockState replacedSnapshot, BlockPos clickedPos) {
        Player cplayer = (Player) ((EntityBridge)player).getBukkitEntity();

        Block clickedBlock = CraftBlock.at(level, clickedPos);
        Block placedBlock = replacedSnapshot.getBlock();

        boolean canBuild = CraftEventFactory.canBuild(level, cplayer, placedBlock.getX(), placedBlock.getZ());

        EquipmentSlot handSlot = CraftEquipmentSlot.getHand(hand);
        BlockPlaceEvent event = new BlockPlaceEvent(placedBlock, replacedSnapshot, clickedBlock, cplayer.getInventory().getItem(handSlot), cplayer, canBuild, handSlot);
        event.callEvent();

        return event;
    }

    public static BlockBurnEvent callBlockBurnEvent(Level world, BlockPos pos, @Nullable Block ignitingBlock){
        BlockBurnEvent event = new BlockBurnEvent(((LevelBridge)world).cardboard$getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), ignitingBlock);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        return event;
    }

    private static boolean canBuild(ServerLevel world, Player player, int x, int z) {
        int spawnSize = Bukkit.getServer().getSpawnRadius();

        if (world.dimension() != Level.OVERWORLD) return true;
        if (spawnSize <= 0) return true;
        if (((CraftServer) Bukkit.getServer()).getHandle().getOps().isEmpty()) return true;
        if (player.isOp()) return true;

        if (null == world.getRespawnData()) {
        	return true;
        }
        
        BlockPos chunkcoordinates = world.getRespawnData().pos();

        int distanceFromSpawn = Math.max(Math.abs(x - chunkcoordinates.getX()), Math.abs(z - chunkcoordinates.getZ()));
        return distanceFromSpawn > spawnSize;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(Level world, int x, int y, int z, Explosion explosion) {
        org.bukkit.World bukkitWorld = ((LevelBridge) world).cardboard$getWorld();
        // org.bukkit.entity.Entity igniter = explosion.entity == null ? null : ((IMixinEntity)explosion.entity).getBukkitEntity();
        org.bukkit.entity.Entity igniter = explosion.getDirectSourceEntity() == null ? null : explosion.getDirectSourceEntity().getBukkitEntity();

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), IgniteCause.EXPLOSION, igniter);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        return event;
    }

    public static void handleInventoryCloseEvent(net.minecraft.world.entity.player.Player human, org.bukkit.event.inventory.InventoryCloseEvent.Reason reason) {
        InventoryCloseEvent event = new InventoryCloseEvent(((AbstractContainerMenuBridge)human.containerMenu).getBukkitView(), reason); // Paper
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
       ((AbstractContainerMenuBridge)human.containerMenu).transferTo(human.inventoryMenu, (CraftHumanEntity) ((EntityBridge)human).getBukkitEntity());
    }

    public static PlayerInteractEvent callPlayerInteractEvent(ServerPlayer who, Action action, ItemStack itemstack, InteractionHand hand) {
        if (action != Action.LEFT_CLICK_AIR && action != Action.RIGHT_CLICK_AIR)
            throw new AssertionError(String.format("%s performing %s with %s", who, action, itemstack));
        return callPlayerInteractEvent(who, action, null, Direction.SOUTH, itemstack, hand);
    }

    public static PlayerInteractEvent callPlayerInteractEvent(ServerPlayer who, Action action, BlockPos position, Direction direction, ItemStack itemstack, InteractionHand hand) {
        return callPlayerInteractEvent(who, action, position, direction, itemstack, false, hand);
    }

    public static PlayerInteractEvent callPlayerInteractEvent(ServerPlayer who, Action action, BlockPos position, Direction direction, ItemStack itemstack, boolean cancelledBlock, InteractionHand hand) {
        Player player = (who == null) ? null : (Player) ((ServerPlayerBridge)who).getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

        assert player != null;
        CraftWorld CraftWorld = (CraftWorld) player.getWorld();
        CraftServer craftServer = (CraftServer) player.getServer();

        Block blockClicked = null;
        if (position != null) {
            blockClicked = CraftWorld.getBlockAt(position.getX(), position.getY(), position.getZ());
        } else {
            switch (action) {
                case LEFT_CLICK_BLOCK:
                    action = Action.LEFT_CLICK_AIR;
                    break;
                case RIGHT_CLICK_BLOCK:
                    action = Action.RIGHT_CLICK_AIR;
                    break;
                default:
                    break;
            }
        }
        BlockFace blockFace = CraftBlock.notchToBlockFace(direction);
        if (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0)
            itemInHand = null;

        PlayerInteractEvent event = new PlayerInteractEvent(player, action, itemInHand, blockClicked, blockFace, (hand == null) ? null : ((hand == InteractionHand.OFF_HAND) ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND));
        if (cancelledBlock)
            event.setUseInteractedBlock(Event.Result.DENY);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static BlockDamageEvent callBlockDamageEvent(ServerPlayer who, int x, int y, int z, ItemStack itemstack, boolean instaBreak) {
        Player player = (who == null) ? null : (Player) ((ServerPlayerBridge)who).getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

        assert player != null;
        CraftWorld CraftWorld = (CraftWorld) player.getWorld();
        CraftServer craftServer = (CraftServer) player.getServer();

        Block blockClicked = CraftWorld.getBlockAt(x, y, z);

        BlockDamageEvent event = new BlockDamageEvent(player, blockClicked, itemInHand, instaBreak);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static BlockRedstoneEvent callRedstoneChange(Level world, BlockPos pos, int oldCurrent, int newCurrent) {
        BlockRedstoneEvent event = new BlockRedstoneEvent(((LevelBridge)world).cardboard$getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), oldCurrent, newCurrent);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handlePlayerRecipeListUpdateEvent(net.minecraft.world.entity.player.Player who, Identifier recipe) {
        PlayerRecipeDiscoverEvent event = new PlayerRecipeDiscoverEvent((Player) ((ServerPlayerBridge)who).getBukkitEntity(), CraftNamespacedKey.fromMinecraft(recipe), true);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static void callProjectileHitEvent(Entity entity, HitResult position) {
        if (position.getType() == Type.MISS) return;

        Block hitBlock = null;
        BlockFace hitFace = null;
        if (position.getType() == Type.BLOCK) {
            BlockHitResult positionBlock = (BlockHitResult) position;
            hitBlock = CraftBlock.at((ServerLevel) entity.level(), positionBlock.getBlockPos());
            hitFace = CraftBlock.notchToBlockFace(positionBlock.getDirection());
        }

        org.bukkit.entity.Entity hitEntity = null;
        if (position.getType() == Type.ENTITY) {
            assert position instanceof EntityHitResult;
            hitEntity = ((EntityBridge)((EntityHitResult) position).getEntity()).getBukkitEntity();
        }

        CraftEntity e = ((EntityBridge)entity).getBukkitEntity();
        if (!(e instanceof Projectile)) {
            BukkitLogger.getLogger().warning("Entity \"" + e.getHandle().getName().getString() + "\" is not an instance of Projectile! Can not fire ProjectileHitEvent!");
            return;
        }

        ProjectileHitEvent event = new ProjectileHitEvent((Projectile) ((EntityBridge)entity).getBukkitEntity(), hitEntity, hitBlock, hitFace);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    public static @Nullable AbstractContainerMenu callInventoryOpenEvent(ServerPlayer player, AbstractContainerMenu container) {
        // Paper start - Add titleOverride to InventoryOpenEvent
        return callInventoryOpenEventWithTitle(player, container).getSecond();
    }

    public static com.mojang.datafixers.util.Pair<net.kyori.adventure.text.@Nullable Component, @Nullable AbstractContainerMenu> callInventoryOpenEventWithTitle(ServerPlayer player, AbstractContainerMenu container) {
        return callInventoryOpenEventWithTitle(player, container, false);
        // Paper end - Add titleOverride to InventoryOpenEvent
    }

    public static com.mojang.datafixers.util.Pair<net.kyori.adventure.text.@Nullable Component, @Nullable AbstractContainerMenu> callInventoryOpenEventWithTitle(ServerPlayer player, AbstractContainerMenu container, boolean cancelled) {
        if (player.containerMenu != player.inventoryMenu) { // fire INVENTORY_CLOSE if one already open
            player.connection.handleContainerClose(new ServerboundContainerClosePacket(player.containerMenu.containerId));
        }

        CraftServer server = player.level().getCraftServer();
        CraftPlayer craftPlayer = (CraftPlayer) ((EntityBridge)player).getBukkitEntity();
        ((AbstractContainerMenuBridge)player.containerMenu).transferTo(container, craftPlayer);

        InventoryOpenEvent event = new InventoryOpenEvent(((AbstractContainerMenuBridge)container).getBukkitView());
        event.setCancelled(cancelled);
        server.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            ((AbstractContainerMenuBridge)container).transferTo(player.containerMenu, craftPlayer);
            return com.mojang.datafixers.util.Pair.of(null, null); // Paper - Add titleOverride to InventoryOpenEvent
        }

        return com.mojang.datafixers.util.Pair.of(event.titleOverride(), container); // Paper - Add titleOverride to InventoryOpenEvent
    }

    public static FireworkExplodeEvent callFireworkExplodeEvent(FireworkRocketEntity firework) {
        FireworkExplodeEvent event = new FireworkExplodeEvent((Firework) ((EntityBridge) firework).getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static VillagerCareerChangeEvent callVillagerCareerChangeEvent(net.minecraft.world.entity.npc.villager.Villager vilager, Profession future, ChangeReason reason) {
        VillagerCareerChangeEvent event = new VillagerCareerChangeEvent((Villager) ((EntityBridge)vilager).getBukkitEntity(), future, reason);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handleBlockFormEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, @net.minecraft.world.level.block.Block.UpdateFlags int flags) {
        return CraftEventFactory.handleBlockFormEvent(world, pos, state, flags, null);
    }

    public static boolean handleBlockFormEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, @net.minecraft.world.level.block.Block.UpdateFlags int flags, @Nullable Entity entity) {
        return CraftEventFactory.handleBlockFormEvent(world, pos, state, flags, entity, false);
    }

    public static boolean handleBlockFormEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, @net.minecraft.world.level.block.Block.UpdateFlags int flags, @Nullable Entity entity, boolean checkSetResult) {
        CraftBlockState snapshot = CraftBlockStates.getBlockState(world, pos);
        snapshot.setData(state);

        BlockFormEvent event = (entity == null) ? new BlockFormEvent(snapshot.getBlock(), snapshot) : new EntityBlockFormEvent(entity.getBukkitEntity(), snapshot.getBlock(), snapshot);
        if (event.callEvent()) {
            boolean result = snapshot.place(flags);
            return !checkSetResult || result;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public static Cancellable handleStatisticsIncrease(net.minecraft.world.entity.player.Player entityHuman, net.minecraft.stats.Stat<?> statistic, int current, int newValue) {
        Player player = (Player) ((ServerPlayerBridge) entityHuman).getBukkitEntity();
        Event event;
		// Handle stats, which are missing in Bukkit API
		if (!Arrays.asList(Statistic.values()).contains(statistic)) {
			// This is very spammy
			// System.out.println("Missing statistic in bukkit API: " + statistic);
			return null;
		}
        Statistic stat = CraftStatistic.getBukkitStatistic(statistic);
        if (stat == null) {
            System.err.println("Unhandled statistic: " + statistic);
            return null;
        }
        switch (stat) {
            case FALL_ONE_CM:
            case BOAT_ONE_CM:
            case CLIMB_ONE_CM:
            case WALK_ON_WATER_ONE_CM:
            case WALK_UNDER_WATER_ONE_CM:
            case FLY_ONE_CM:
            case HORSE_ONE_CM:
            case MINECART_ONE_CM:
            case PIG_ONE_CM:
            case PLAY_ONE_MINUTE:
            case SWIM_ONE_CM:
            case WALK_ONE_CM:
            case SPRINT_ONE_CM:
            case CROUCH_ONE_CM:
            case TIME_SINCE_DEATH:
            case SNEAK_TIME:
                return null;
            default:
        }
        if (stat.getType() == Statistic.Type.UNTYPED) {
            event = new PlayerStatisticIncrementEvent(player, stat, current, newValue);
        } else if (stat.getType() == Statistic.Type.ENTITY) {
            EntityType entityType = CraftStatistic.getEntityTypeFromStatistic((net.minecraft.stats.Stat<net.minecraft.world.entity.EntityType<?>>) statistic);
            event = new PlayerStatisticIncrementEvent(player, stat, current, newValue, entityType);
        } else {
            Material material = CraftStatistic.getMaterialFromStatistic(statistic);
            assert material != null;
            event = new PlayerStatisticIncrementEvent(player, stat, current, newValue, material);
        }
        Bukkit.getPluginManager().callEvent(event);
        return (Cancellable) event;
    }

    public static EntityPickupItemEvent callEntityPickupItemEvent(Entity who, ItemEntity item, int remaining, boolean cancelled) {
        EntityPickupItemEvent event = new EntityPickupItemEvent((org.bukkit.entity.LivingEntity) ((EntityBridge)who).getBukkitEntity(), (org.bukkit.entity.Item) ((EntityBridge)item).getBukkitEntity(), remaining);
        event.setCancelled(cancelled);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityEnterLoveModeEvent callEntityEnterLoveModeEvent(net.minecraft.world.entity.player.Player entityHuman, Animal entityAnimal, int loveTicks) {
        EntityEnterLoveModeEvent entityEnterLoveModeEvent = new EntityEnterLoveModeEvent((Animals) ((EntityBridge)entityAnimal).getBukkitEntity(), entityHuman != null ? (HumanEntity) ((EntityBridge)entityHuman).getBukkitEntity() : null, loveTicks);
        Bukkit.getPluginManager().callEvent(entityEnterLoveModeEvent);
        return entityEnterLoveModeEvent;
    }

    public static ItemStack callPreCraftEvent(Container matrix, Container resultInventory, ItemStack result, InventoryView lastCraftView, boolean isRepair) {
        CraftInventoryCrafting inventory = new CraftInventoryCrafting(matrix, resultInventory);
        inventory.setResult(CraftItemStack.asCraftMirror(result));

        PrepareItemCraftEvent event = new PrepareItemCraftEvent(inventory, lastCraftView, isRepair);
        Bukkit.getPluginManager().callEvent(event);

        org.bukkit.inventory.ItemStack bitem = event.getInventory().getResult();

        return CraftItemStack.asNMSCopy(bitem);
    }

    public static EntityTransformEvent callEntityTransformEvent(net.minecraft.world.entity.LivingEntity original, net.minecraft.world.entity.LivingEntity coverted, EntityTransformEvent.TransformReason transformReason) {
        return callEntityTransformEvent(original, Collections.singletonList(coverted), transformReason);
    }

    public static EntityTransformEvent callEntityTransformEvent(net.minecraft.world.entity.LivingEntity original, List<net.minecraft.world.entity.LivingEntity> convertedList, EntityTransformEvent.TransformReason convertType) {
        List<org.bukkit.entity.Entity> list = new ArrayList<>();
        for (net.minecraft.world.entity.LivingEntity entityLiving : convertedList)
            list.add(((EntityBridge)entityLiving).getBukkitEntity());

        if (list.size() <= 0)
            return null;

        EntityTransformEvent event = new EntityTransformEvent(((EntityBridge)original).getBukkitEntity(), list, convertType);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityPlaceEvent callEntityPlaceEvent(UseOnContext itemactioncontext, Entity entity) {
        return callEntityPlaceEvent(itemactioncontext.getLevel(), itemactioncontext.getClickedPos(), itemactioncontext.getClickedFace(), itemactioncontext.getPlayer(), entity);
    }

    public static EntityPlaceEvent callEntityPlaceEvent(Level world, BlockPos clickPosition, Direction clickedFace, net.minecraft.world.entity.player.Player human, Entity entity) {
        Player who = (human == null) ? null : (Player) ((EntityBridge)human).getBukkitEntity();
        org.bukkit.block.Block blockClicked = CraftBlock.at((ServerLevel) world, clickPosition);
        org.bukkit.block.BlockFace blockFace = org.bukkit.craftbukkit.block.CraftBlock.notchToBlockFace(clickedFace);

        EntityPlaceEvent event = new EntityPlaceEvent(((EntityBridge)entity).getBukkitEntity(), who, blockClicked, blockFace);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }
    
    public static EntityPlaceEvent callEntityPlaceEvent(Level world, BlockPos clickPosition, Direction clickedFace, net.minecraft.world.entity.player.Player  human, Entity entity, InteractionHand enumhand) {
        Player who = (human == null) ? null : (Player) ((EntityBridge)human).getBukkitEntity();
        org.bukkit.block.Block blockClicked = CraftBlock.at((ServerLevel) world, clickPosition);
        org.bukkit.block.BlockFace blockFace = org.bukkit.craftbukkit.block.CraftBlock.notchToBlockFace(clickedFace);

        //EntityPlaceEvent event = new EntityPlaceEvent(((IMixinEntity)entity).getBukkitEntity(), who, blockClicked, blockFace, EquipmentSlotImpl.getHand(enumhand));
        EntityPlaceEvent event = new EntityPlaceEvent(((EntityBridge)entity).getBukkitEntity(), who, blockClicked, blockFace/*, Hand.MAIN_HAND*/);

        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    public static CreeperPowerEvent callCreeperPowerEvent(Entity creeper, Entity lightning, CreeperPowerEvent.PowerCause cause) {
        CreeperPowerEvent event = new CreeperPowerEvent((Creeper) ((EntityBridge)creeper).getBukkitEntity(), (LightningStrike) ((EntityBridge)lightning).getBukkitEntity(), cause);
        ((EntityBridge)creeper).getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static void callPlayerItemBreakEvent(net.minecraft.world.entity.player.Player human, ItemStack brokenItem) {
        CraftItemStack item = CraftItemStack.asCraftMirror(brokenItem);
        PlayerItemBreakEvent event = new PlayerItemBreakEvent((Player) ((EntityBridge)human).getBukkitEntity(), item);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static EntityTargetLivingEntityEvent callEntityTargetLivingEvent(Entity entity, net.minecraft.world.entity.LivingEntity target, EntityTargetEvent.TargetReason reason) {
        EntityTargetLivingEntityEvent event = new EntityTargetLivingEntityEvent(((EntityBridge)entity).getBukkitEntity(), (target == null) ? null : (org.bukkit.entity.LivingEntity) (((EntityBridge)entity).getBukkitEntity()), reason);
        ((EntityBridge)entity).getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static boolean callHorseJumpEvent(Entity horse, float power) {
        HorseJumpEvent event = new HorseJumpEvent((AbstractHorse) ((EntityBridge)horse).getBukkitEntity(), power);
        ((EntityBridge)horse).getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    /**
     * ItemMergeEvent
     */
    public static ItemMergeEvent callItemMergeEvent(ItemEntity merging, ItemEntity mergingWith) {
        org.bukkit.entity.Item entityMerging = (org.bukkit.entity.Item) ((EntityBridge)merging).getBukkitEntity();
        org.bukkit.entity.Item entityMergingWith = (org.bukkit.entity.Item) ((EntityBridge)mergingWith).getBukkitEntity();

        ItemMergeEvent event = new ItemMergeEvent(entityMerging, entityMergingWith);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerDeathEvent callPlayerDeathEvent(ServerPlayer victim, DamageSource damageSource, List<org.bukkit.inventory.ItemStack> drops, String deathMessage, boolean keepInventory) {
        CraftPlayer entity = (CraftPlayer) ((ServerPlayerBridge)victim).getBukkitEntity();
        
        CraftDamageSource bukkitDamageSource = new CraftDamageSource(damageSource);
        
        PlayerDeathEvent event = new PlayerDeathEvent(entity, bukkitDamageSource, drops, ((LivingEntityBridge)victim).getExpReward(), 0, deathMessage);
        event.setKeepInventory(keepInventory);
        org.bukkit.World world = entity.getWorld();
        Bukkit.getServer().getPluginManager().callEvent(event);

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            world.dropItem(entity.getLocation(), stack);
        }
        return event;
    }

    public static LootGenerateEvent callLootGenerateEvent(Container inventory, LootTable lootTable, LootContext lootInfo, List<ItemStack> loot, boolean plugin) {
        CraftWorld world = ((LevelBridge)lootInfo.getLevel()).cardboard$getWorld();
        Entity entity = lootInfo.getOptionalParameter(LootContextParams.THIS_ENTITY);

        NamespacedKey key = null; // CraftNamespacedKey.fromMinecraft(((IMixinLootManager)world.getHandle().getServer().getLootManager()).getLootTableToKeyMapBF().get(lootTable));

        Registry<LootTable> reg = CraftServer.server.registryAccess().lookupOrThrow(Registries.LOOT_TABLE);
        Optional<ResourceKey<LootTable>> opt = reg.getResourceKey(lootTable);
        if (opt.isPresent()) {
        	key = CraftLootTable.minecraftToBukkitKey(opt.get());
        }
        
        CraftLootTable craftLootTable = new CraftLootTable(key, lootTable);

        List<org.bukkit.inventory.ItemStack> bukkitLoot = loot.stream().map(CraftItemStack::asCraftMirror).collect(Collectors.toCollection(ArrayList::new));

        LootGenerateEvent event = new LootGenerateEvent(world, (entity != null ? ((EntityBridge)entity).getBukkitEntity() : null), ((ContainerBridge)inventory).getOwner(), craftLootTable, CraftLootTable.convertContext(lootInfo), bukkitLoot, plugin);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityDeathEvent callEntityDeathEvent(net.minecraft.world.entity.LivingEntity victim, DamageSource damageSource, List<org.bukkit.inventory.ItemStack> drops) {
        if (((EntityBridge)victim).getBukkitEntity() instanceof UnknownEntity) {
            UnknownEntity uk = (UnknownEntity) ((EntityBridge)victim).getBukkitEntity();
            CardboardMod.LOGGER.info("Oh no! " + net.minecraft.world.entity.EntityType.getKey(uk.getHandle().getType()).toString() + " is an unknown bukkit entity!");
        }
        CraftLivingEntity entity = (CraftLivingEntity) ((EntityBridge)victim).getBukkitEntity();
        
        CraftDamageSource bukkitDamageSource = new CraftDamageSource(damageSource);
        EntityDeathEvent event = new EntityDeathEvent(entity, bukkitDamageSource, drops, ((LivingEntityBridge)victim).getExpReward());

        if ((null == entity) || (null == entity.getWorld())) {
            boolean e = (null == entity);
            CardboardMod.LOGGER.info("WARNING: Null " + (e ? "entity" : "world") + "!");
            return event;
        }

        CraftWorld world = (CraftWorld) entity.getWorld();
        Bukkit.getServer().getPluginManager().callEvent(event);

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0) continue;

            world.dropItem(entity.getLocation(), stack); // Paper - note: dropItem already clones due to this being bukkit -> NMS
            if (stack instanceof CraftItemStack) stack.setAmount(0); // Paper
        }

        return event;
    }
    
    /*
    public int LivingEntity_getExpReward(net.minecraft.entity.LivingEntity thiz) {

        if (thiz.getEntityWorld() instanceof ServerWorld && !thiz.isExperienceDroppingDisabled()
        		&& (thiz.shouldAlwaysDropExperience() || thiz.playerHitTimer > 0 && thiz.shouldDropExperience() && ((ServerWorld)thiz.getEntityWorld()).getGameRules().getBoolean(GameRules.DO_MOB_LOOT))) {
            int exp = thiz.getExperienceToDrop((ServerWorld) thiz.getEntityWorld());
            return exp;
        } else {
            return 0;
        }
    }
    */

    public static ExpBottleEvent callExpBottleEvent(Entity entity, int exp) {
        ThrownExpBottle bottle = (ThrownExpBottle) ((EntityBridge)entity).getBukkitEntity();
        ExpBottleEvent event = new ExpBottleEvent(bottle, exp);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityShootBowEvent callEntityShootBowEvent(net.minecraft.world.entity.LivingEntity who, ItemStack bow, ItemStack consumableItem, Entity entityArrow, InteractionHand hand, float force, boolean consumeItem) {
        LivingEntity shooter = (LivingEntity) ((EntityBridge)who).getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(bow);
        CraftItemStack itemConsumable = CraftItemStack.asCraftMirror(consumableItem);
        org.bukkit.entity.Entity arrow = ((EntityBridge)entityArrow).getBukkitEntity();
        EquipmentSlot handSlot = (hand == InteractionHand.MAIN_HAND) ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;

        if (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0)
            itemInHand = null;

        EntityShootBowEvent event = new EntityShootBowEvent(shooter, itemInHand, itemConsumable, arrow, handSlot, force, consumeItem);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Bucket methods
     */
    public static PlayerBucketEmptyEvent callPlayerBucketEmptyEvent(ServerLevel world, net.minecraft.world.entity.player.Player who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemInHand) {
        return (PlayerBucketEmptyEvent) getPlayerBucketEvent(false, world, who, changed, clicked, clickedFace, itemInHand, Items.BUCKET);
    }

    public static PlayerBucketFillEvent callPlayerBucketFillEvent(ServerLevel world, net.minecraft.world.entity.player.Player who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemInHand, net.minecraft.world.item.Item bucket) {
        return (PlayerBucketFillEvent) getPlayerBucketEvent(true, world, who, clicked, changed, clickedFace, itemInHand, bucket);
    }

    private static PlayerEvent getPlayerBucketEvent(boolean isFilling, ServerLevel world, net.minecraft.world.entity.player.Player who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemstack, net.minecraft.world.item.Item item) {
        return getPlayerBucketEvent(isFilling, world, who, changed, clicked, clickedFace, itemstack, item, null);
    }

    public static PlayerBucketEmptyEvent callPlayerBucketEmptyEvent(Level world, net.minecraft.world.entity.player.Player who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemstack, InteractionHand enumHand) {
        return (PlayerBucketEmptyEvent) getPlayerBucketEvent(false, world, who, changed, clicked, clickedFace, itemstack, Items.BUCKET, enumHand);
    }

    public static PlayerBucketFillEvent callPlayerBucketFillEvent(Level world, net.minecraft.world.entity.player.Player who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemInHand, net.minecraft.world.item.Item bucket, InteractionHand enumHand) {
        return (PlayerBucketFillEvent) getPlayerBucketEvent(true, world, who, clicked, changed, clickedFace, itemInHand, bucket, enumHand);
    }

    private static PlayerEvent getPlayerBucketEvent(boolean isFilling, Level world, net.minecraft.world.entity.player.Player who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemstack, net.minecraft.world.item.Item item, InteractionHand enumHand) {
        // Paper end
        Player player = (Player) ((ServerPlayerBridge)who).getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asNewCraftStack(item);
        Material bucket = CraftMagicNumbers.getMaterial(itemstack.getItem());

        CraftServer craftServer = (CraftServer) player.getServer();

        Block block = CraftBlock.at((ServerLevel) world, changed);
        Block blockClicked = CraftBlock.at((ServerLevel) world, clicked);
        BlockFace blockFace = CraftBlock.notchToBlockFace(clickedFace);

        // TODO - When we move to PaperAPI we need to add hand to event.
        PlayerEvent event;
        if (isFilling) {
            event = new PlayerBucketFillEvent(player, block, blockClicked, blockFace, bucket, itemInHand); 
            ((PlayerBucketFillEvent) event).setCancelled(!canBuild((ServerLevel) world, player, changed.getX(), changed.getZ()));
        } else {
            event = new PlayerBucketEmptyEvent(player, block, blockClicked, blockFace, bucket, itemInHand);
            ((PlayerBucketEmptyEvent) event).setCancelled(!canBuild((ServerLevel) world, player, changed.getX(), changed.getZ()));
        }

        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(Level world, BlockPos pos, IgniteCause cause, Entity igniter) {
        BlockIgniteEvent event = new BlockIgniteEvent(((LevelBridge)world).cardboard$getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), cause, ((EntityBridge)igniter).getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Replaced in 1.21.8
     * 
     * @deprecated Replaced
     * @see {@link #callPlayerExpChangeEvent(net.minecraft.world.entity.player.Player, net.minecraft.world.entity.ExperienceOrb)}
     */
    @Deprecated
    public static PlayerExpChangeEvent callPlayerExpChangeEvent1(net.minecraft.world.entity.player.Player entity, int expAmount) {
    	Player player = (Player) ((EntityBridge)entity).getBukkitEntity();
    	PlayerExpChangeEvent event = new PlayerExpChangeEvent(player, expAmount);
    	Bukkit.getPluginManager().callEvent(event);
    	return event;
    }

    public static PlayerExpChangeEvent callPlayerExpChangeEvent(net.minecraft.world.entity.player.Player entity, net.minecraft.world.entity.ExperienceOrb entityOrb, int expAmount) {
    	Player player = (Player) ((EntityBridge)entity).getBukkitEntity();
    	ExperienceOrb source = (ExperienceOrb) ((EntityBridge)entityOrb).getBukkitEntity();
    	PlayerExpChangeEvent event = new PlayerExpChangeEvent(player, source, expAmount);
    	Bukkit.getPluginManager().callEvent(event);
    	return event;
    }

    @Deprecated
    public static PlayerExpChangeEvent callPlayerExpChangeEvent_old(net.minecraft.world.entity.player.Player entity, net.minecraft.world.entity.ExperienceOrb entityOrb) {
    	Player player = (Player) ((EntityBridge) entity).getBukkitEntity();
    	ExperienceOrb source = (ExperienceOrb) ((EntityBridge)entityOrb).getBukkitEntity();
    	int expAmount = source.getExperience();

    	// TODO: 1.21.8 API: new PlayerExpChangeEvent(player, (Entity)source, expAmount);
    	PlayerExpChangeEvent event = new PlayerExpChangeEvent(player, expAmount);
    	Bukkit.getPluginManager().callEvent(event);
    	return event;
    }

    @Deprecated(forRemoval = true)
    public static PlayerItemMendEvent callPlayerItemMendEvent(net.minecraft.world.entity.player.Player entity, net.minecraft.world.entity.ExperienceOrb orb, net.minecraft.world.item.ItemStack nmsMendedItem, int repairAmount) {
        Player player = (Player) ((EntityBridge)entity).getBukkitEntity();
        org.bukkit.inventory.ItemStack bukkitStack = CraftItemStack.asCraftMirror(nmsMendedItem);
        PlayerItemMendEvent event = new PlayerItemMendEvent(player, bukkitStack, (ExperienceOrb) ((EntityBridge)orb).getBukkitEntity(), repairAmount);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerItemMendEvent callPlayerItemMendEvent(net.minecraft.world.entity.player.Player entity, net.minecraft.world.entity.ExperienceOrb orb, net.minecraft.world.item.ItemStack nmsMendedItem, net.minecraft.world.entity.EquipmentSlot slot, int repairAmount, int consumedExperience) { // Paper - Expand PlayerItemMendEvent
        Player player = (Player) ((EntityBridge)entity).getBukkitEntity();
        org.bukkit.inventory.ItemStack bukkitStack = CraftItemStack.asCraftMirror(nmsMendedItem);
        PlayerItemMendEvent event = new PlayerItemMendEvent(player, bukkitStack, CraftEquipmentSlot.getSlot(slot), (ExperienceOrb) ((EntityBridge)orb).getBukkitEntity(), repairAmount, consumedExperience); // Paper - Expand PlayerItemMendEvent
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    // TODO Fix Collections.emptyList()
    public static boolean handlePlayerShearEntityEvent(net.minecraft.world.entity.LivingEntity player, Entity sheared, ItemStack shears, InteractionHand hand) {
        if (!(player instanceof net.minecraft.world.entity.player.Player)) return true;

        PlayerShearEntityEvent event = new PlayerShearEntityEvent(
        		(Player) ((EntityBridge)player).getBukkitEntity(),
        		((EntityBridge)sheared).getBukkitEntity(),
        		CraftItemStack.asCraftMirror(shears),
        		(hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND),
        		Lists.transform( Collections.emptyList() , CraftItemStack::asCraftMirror)
        	);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static PlayerUnleashEntityEvent callPlayerUnleashEntityEvent(Mob entity, net.minecraft.world.entity.player.Player player) {
        PlayerUnleashEntityEvent event = new PlayerUnleashEntityEvent(((EntityBridge)entity).getBukkitEntity(), (Player) ((EntityBridge)player).getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerLeashEntityEvent callPlayerLeashEntityEvent(Mob entity, Entity leashHolder, net.minecraft.world.entity.player.Player player) {
        PlayerLeashEntityEvent event = new PlayerLeashEntityEvent(((EntityBridge)entity).getBukkitEntity(), ((EntityBridge)leashHolder).getBukkitEntity(), (Player) ((EntityBridge)player).getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerLevelChangeEvent callPlayerLevelChangeEvent(Player player, int oldLevel, int newLevel) {
        PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(player, oldLevel, newLevel);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
    
    public static EntityBreakDoorEvent callEntityBreakDoorEvent(Entity entity, BlockPos pos) {
        org.bukkit.entity.Entity entity1 = ((EntityBridge)entity).getBukkitEntity();
        CraftBlock block = CraftBlock.at((ServerLevel) entity.level(), pos);

        
        CraftBlockData bd = CraftBlockData.createData(block.getNMS());
        EntityBreakDoorEvent event = new EntityBreakDoorEvent((LivingEntity) entity1, block, bd);
        entity1.getServer().getPluginManager().callEvent(event);

        return event;
    }
    
    // todo: check this
    public static EntityBreakDoorEvent callEntityBreakDoorEvent(net.minecraft.world.entity.Entity entity, BlockPos pos, BlockState newState) {
    	 org.bukkit.entity.Entity entity1 = ((EntityBridge)entity).getBukkitEntity();
        CraftBlock block = CraftBlock.at((ServerLevel) entity.level(), pos);
        
        CraftBlockData bd = CraftBlockData.createData(block.getNMS());
        
        EntityBreakDoorEvent event = new EntityBreakDoorEvent((LivingEntity)entity1, block, bd);
        entity1.getServer().getPluginManager().callEvent((Event)event);
        return event;
    }

    public static boolean handleBlockSpreadEvent(Level world, BlockPos source, BlockPos target, net.minecraft.world.level.block.state.BlockState block, int flag) {
        // Suppress during worldgen
        if (!(world instanceof Level)) {
            world.setBlock(target, block, flag);
            return true;
        }

        CraftBlockState state = CraftBlockStates.getBlockState(world, target);
        state.setData(block);

        BlockSpreadEvent event = new BlockSpreadEvent(((LevelBridge) world).cardboard$getWorld().getBlockAt(target.getX(), target.getY(), target.getZ()), ((LevelBridge) world).cardboard$getWorld().getBlockAt(source.getX(), source.getY(), source.getZ()), state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }
        return !event.isCancelled();
    }

    public static boolean callEntityChangeBlockEvent(Entity entity, BlockPos pos, net.minecraft.world.level.block.state.BlockState newState) {
        return CraftEventFactory.callEntityChangeBlockEvent(entity, pos, newState, false);
    }

    public static boolean callEntityChangeBlockEvent(Entity entity, BlockPos pos, net.minecraft.world.level.block.state.BlockState newState, boolean cancelled) {
        Block block = CraftBlock.at(entity.level(), pos);

        EntityChangeBlockEvent event = new EntityChangeBlockEvent(entity.getBukkitEntity(), block, CraftBlockData.fromData(newState));
        event.setCancelled(cancelled);
        event.getEntity().getServer().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static boolean handleBlockGrowEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState block) {
        return handleBlockGrowEvent(world, pos, block, 3);
    }

    public static boolean handleBlockGrowEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState newData, int flag) {
        Block block = ((LevelBridge) world).cardboard$getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        CraftBlockState state = (CraftBlockState) block.getState();
        state.setData(newData);

        BlockGrowEvent event = new BlockGrowEvent(block, state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }

        return !event.isCancelled();
    }

    /**
     * BlockFadeEvent
     */
    public static BlockFadeEvent callBlockFadeEvent(LevelAccessor world, BlockPos pos, net.minecraft.world.level.block.state.BlockState newBlock) {
        CraftBlockState state = CraftBlockStates.getBlockState(world, pos);
        state.setData(newBlock);

        BlockFadeEvent event = new BlockFadeEvent(state.getBlock(), state);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    @Deprecated
    public static boolean handleBellRingEvent_(ServerLevel world, BlockPos pos, Entity entity) {
        //Block block = CraftBlock.at(world, pos);
        //BellRingEvent event = new BellRingEvent(block, (entity != null) ? ((IMixinEntity) entity).getBukkitEntity() : null);
        //Bukkit.getPluginManager().callEvent(event);
        //return !event.isCancelled();
    	return false;
    }
    
    public static boolean handleBellRingEvent(ServerLevel world, BlockPos position, Direction direction, net.minecraft.world.entity.Entity entity) {
        CraftBlock block = CraftBlock.at(world, position);
        BlockFace bukkitDirection = CraftBlock.notchToBlockFace(direction);
        BellRingEvent event = new BellRingEvent((Block)block, bukkitDirection, (entity != null) ? ((EntityBridge) entity).getBukkitEntity() : null);
        Bukkit.getPluginManager().callEvent((Event)event);
        return !event.isCancelled();
    }

    public static EntityBreedEvent callEntityBreedEvent(net.minecraft.world.entity.LivingEntity child, net.minecraft.world.entity.LivingEntity mother, net.minecraft.world.entity.LivingEntity father, net.minecraft.world.entity.LivingEntity breeder, ItemStack bredWith, int experience) {
        LivingEntity breederEntity = breeder == null ? null : (LivingEntity) breeder.getBukkitEntity();
        CraftItemStack bredWithStack = bredWith == null ? null : CraftItemStack.asCraftMirror(bredWith).clone();

        EntityBreedEvent event = new EntityBreedEvent((LivingEntity) child.getBukkitEntity(), (LivingEntity) mother.getBukkitEntity(), (LivingEntity) father.getBukkitEntity(), breederEntity, bredWithStack, experience);
        event.callEvent();
        return event;
    }

	public static boolean callPlayerSignOpenEvent(net.minecraft.world.entity.player.Player player, SignBlockEntity tileEntitySign, boolean front, PlayerSignOpenEvent.Cause cause) {
        CraftBlock block = CraftBlock.at((ServerLevel) tileEntitySign.getLevel(), tileEntitySign.getBlockPos());
        Sign sign = (Sign)CraftBlockStates.getBlockState(block);
        Side side = front ? Side.FRONT : Side.BACK;
        return callPlayerSignOpenEvent((Player)((EntityBridge) player).getBukkitEntity(), sign, side, cause);
    }

    public static boolean callPlayerSignOpenEvent(Player player, Sign sign, Side side, PlayerSignOpenEvent.Cause cause) {
        PlayerSignOpenEvent event = new PlayerSignOpenEvent(player, sign, side, cause);
        Bukkit.getPluginManager().callEvent((Event)event);
        return !event.isCancelled();
    }

    @SuppressWarnings("OptionalAssignedToNull")
    public static Component handleLoginResult(PlayerList_LoginResult result, PlayerConnection paperConnection, Connection connection, GameProfile profile, MinecraftServer server, boolean loginPhase) {
        PlayerConnectionValidateLoginEvent event = new PlayerConnectionValidateLoginEvent(
                paperConnection, result.isAllowed() ? null : PaperAdventure.asAdventure(result.message())
        );
        event.callEvent();

        Component disconnectReason = PaperAdventure.asVanilla(event.getKickMessage());

        // For the login event it normally was never fired during configuration phase. In order to make this deprecation less
        // breaky we will cache result and use it next time.
        // TODO
        /*if (loginPhase) {
            disconnectReason = HorriblePlayerLoginEventHack.execute(connection, server, profile,
                    disconnectReason == null ? PlayerList_LoginResult.ALLOW : new PlayerList_LoginResult(disconnectReason, disconnectReason == null ? PlayerLoginEvent.Result.KICK_OTHER : result.result())
            );
        } else if (connection.legacySavedLoginEventResultOverride != null) {
            // If the override is set, use it.
            disconnectReason = connection.legacySavedLoginEventResultOverride.orElse(null);
        }*/

        return disconnectReason;
    }

    public static PlayerExpCooldownChangeEvent callPlayerXpCooldownEvent(
    		net.minecraft.world.entity.player.Player entity, int newCooldown, org.bukkit.event.player.PlayerExpCooldownChangeEvent.ChangeReason changeReason
    		) {
    	Player player = (Player) ((ServerPlayerBridge) entity).getBukkitEntity();
    	PlayerExpCooldownChangeEvent event = new PlayerExpCooldownChangeEvent(player, newCooldown, changeReason);
    	Bukkit.getPluginManager().callEvent(event);
    	return event;
    }

    public static <T> CraftEventFactory.GameRuleSetResult<T> handleGameRuleSet(GameRule<T> rule, T value, ServerLevel level, @Nullable CommandSender sender) {
		String valueStr = rule.serialize(value);
		PaperWorldGameRuleChangeEvent event = new PaperWorldGameRuleChangeEvent(((LevelBridge)level).cardboard$getWorld(), sender, CraftGameRule.minecraftToBukkit(rule), valueStr);
		if (event.callEvent()) {
			if (!event.getValue().equals(valueStr)) {
				value = (T)rule.deserialize(event.getValue()).getOrThrow();
			}

			// TODO: Add Paper's Per world gamerules
			
			level.getGameRules().set(rule, value, level.getServer());
			return new CraftEventFactory.GameRuleSetResult<>(value, false);
		} else {
			return new CraftEventFactory.GameRuleSetResult<>(level.getGameRules().get(rule), true);
		}
	}

	public record GameRuleSetResult<T>(T value, boolean cancelled) {
	}

}
