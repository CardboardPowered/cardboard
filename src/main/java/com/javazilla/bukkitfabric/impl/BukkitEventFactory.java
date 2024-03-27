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
package com.javazilla.bukkitfabric.impl;

import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.BukkitLogger;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinLivingEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinLootManager;
import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import io.papermc.paper.event.block.BellRingEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftStatistic;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.cardboardpowered.impl.entity.LivingEntityImpl;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.entity.UnknownEntity;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.cardboardpowered.impl.world.WorldImpl;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class BukkitEventFactory {

    public static Entity entityDamage;

    /**
     */
    public static void callEvent(Event e) {
        if (!e.isAsynchronous() && !Bukkit.isPrimaryThread()) {
            ((IMixinMinecraftServer)CraftServer.server).cardboard_runOnMainThread(() -> {
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

    public static BlockPlaceEvent callBlockPlaceEvent(ServerWorld world, PlayerEntity who, Hand hand, BlockState replacedBlockState, int x, int y, int z) {
        WorldImpl worldImpl = ((IMixinWorld)world).getWorldImpl();
        CraftServer craftServer = CraftServer.INSTANCE;

        Player player = (Player) ((IMixinServerEntityPlayer) who).getBukkitEntity();

        Block blockClicked = worldImpl.getBlockAt(x, y, z);
        Block placedBlock = replacedBlockState.getBlock();

        boolean canBuild = canBuild(world, player, placedBlock.getX(), placedBlock.getZ());
        boolean isMainHand = (hand == Hand.MAIN_HAND);

        org.bukkit.inventory.ItemStack item;
        EquipmentSlot equipmentSlot;

        item = isMainHand ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
        equipmentSlot = isMainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;

        BlockPlaceEvent event = new BlockPlaceEvent(placedBlock, replacedBlockState, blockClicked, item, player, canBuild, equipmentSlot);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static BlockBurnEvent callBlockBurnEvent(World world, BlockPos pos, @Nullable Block ignitingBlock){
        BlockBurnEvent event = new BlockBurnEvent(((IMixinWorld)world).getWorldImpl().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), ignitingBlock);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        return event;
    }

    private static boolean canBuild(ServerWorld world, Player player, int x, int z) {
        int spawnSize = Bukkit.getServer().getSpawnRadius();

        if (world.getRegistryKey() != World.OVERWORLD) return true;
        if (spawnSize <= 0) return true;
        if (((CraftServer) Bukkit.getServer()).getHandle().getPlayerManager().getOpList().isEmpty()) return true;
        if (player.isOp()) return true;

        BlockPos chunkcoordinates = world.getSpawnPos();

        int distanceFromSpawn = Math.max(Math.abs(x - chunkcoordinates.getX()), Math.abs(z - chunkcoordinates.getZ()));
        return distanceFromSpawn > spawnSize;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, int x, int y, int z, Explosion explosion) {
        org.bukkit.World bukkitWorld = ((IMixinWorld) world).getWorldImpl();
        org.bukkit.entity.Entity igniter = explosion.entity == null ? null : ((IMixinEntity)explosion.entity).getBukkitEntity();

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), IgniteCause.EXPLOSION, igniter);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerInteractEvent callPlayerInteractEvent(ServerPlayerEntity who, Action action, ItemStack itemstack, Hand hand) {
        if (action != Action.LEFT_CLICK_AIR && action != Action.RIGHT_CLICK_AIR)
            throw new AssertionError(String.format("%s performing %s with %s", who, action, itemstack));
        return callPlayerInteractEvent(who, action, null, Direction.SOUTH, itemstack, hand);
    }

    public static PlayerInteractEvent callPlayerInteractEvent(ServerPlayerEntity who, Action action, BlockPos position, Direction direction, ItemStack itemstack, Hand hand) {
        return callPlayerInteractEvent(who, action, position, direction, itemstack, false, hand);
    }

    public static PlayerInteractEvent callPlayerInteractEvent(ServerPlayerEntity who, Action action, BlockPos position, Direction direction, ItemStack itemstack, boolean cancelledBlock, Hand hand) {
        Player player = (who == null) ? null : (Player) ((IMixinServerEntityPlayer)who).getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

        assert player != null;
        WorldImpl WorldImpl = (WorldImpl) player.getWorld();
        CraftServer craftServer = (CraftServer) player.getServer();

        Block blockClicked = null;
        if (position != null) {
            blockClicked = WorldImpl.getBlockAt(position.getX(), position.getY(), position.getZ());
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

        PlayerInteractEvent event = new PlayerInteractEvent(player, action, itemInHand, blockClicked, blockFace, (hand == null) ? null : ((hand == Hand.OFF_HAND) ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND));
        if (cancelledBlock)
            event.setUseInteractedBlock(Event.Result.DENY);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static BlockDamageEvent callBlockDamageEvent(ServerPlayerEntity who, int x, int y, int z, ItemStack itemstack, boolean instaBreak) {
        Player player = (who == null) ? null : (Player) ((IMixinServerEntityPlayer)who).getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

        assert player != null;
        WorldImpl WorldImpl = (WorldImpl) player.getWorld();
        CraftServer craftServer = (CraftServer) player.getServer();

        Block blockClicked = WorldImpl.getBlockAt(x, y, z);

        BlockDamageEvent event = new BlockDamageEvent(player, blockClicked, itemInHand, instaBreak);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static BlockRedstoneEvent callRedstoneChange(World world, BlockPos pos, int oldCurrent, int newCurrent) {
        BlockRedstoneEvent event = new BlockRedstoneEvent(((IMixinWorld)world).getWorldImpl().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), oldCurrent, newCurrent);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handlePlayerRecipeListUpdateEvent(PlayerEntity who, Identifier recipe) {
        PlayerRecipeDiscoverEvent event = new PlayerRecipeDiscoverEvent((Player) ((IMixinServerEntityPlayer)who).getBukkitEntity(), CraftNamespacedKey.fromMinecraft(recipe));
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static void callProjectileHitEvent(Entity entity, HitResult position) {
        if (position.getType() == Type.MISS) return;

        Block hitBlock = null;
        BlockFace hitFace = null;
        if (position.getType() == Type.BLOCK) {
            BlockHitResult positionBlock = (BlockHitResult) position;
            hitBlock = CraftBlock.at((ServerWorld) entity.getWorld(), positionBlock.getBlockPos());
            hitFace = CraftBlock.notchToBlockFace(positionBlock.getSide());
        }

        org.bukkit.entity.Entity hitEntity = null;
        if (position.getType() == Type.ENTITY) {
            assert position instanceof EntityHitResult;
            hitEntity = ((IMixinEntity)((EntityHitResult) position).getEntity()).getBukkitEntity();
        }

        CraftEntity e = ((IMixinEntity)entity).getBukkitEntity();
        if (!(e instanceof Projectile)) {
            BukkitLogger.getLogger().warning("Entity \"" + e.nms.getName().getString() + "\" is not an instance of Projectile! Can not fire ProjectileHitEvent!");
            return;
        }

        ProjectileHitEvent event = new ProjectileHitEvent((Projectile) ((IMixinEntity)entity).getBukkitEntity(), hitEntity, hitBlock, hitFace);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    public static ScreenHandler callInventoryOpenEvent(ServerPlayerEntity player, ScreenHandler container) {
        return callInventoryOpenEvent(player, container, false);
    }

    public static ScreenHandler callInventoryOpenEvent(ServerPlayerEntity player, ScreenHandler container, boolean cancelled) {
        PlayerImpl PlayerImpl = (PlayerImpl) ((IMixinServerEntityPlayer)player).getBukkitEntity();
        if (!(player.currentScreenHandler instanceof IMixinScreenHandler))
            return container;

        CardboardInventoryView bv = ((IMixinScreenHandler)container).getBukkitView();
        bv.setPlayerIfNotSet((PlayerImpl) ((IMixinServerEntityPlayer)player).getBukkitEntity());

        try {
            ((IMixinScreenHandler)player.currentScreenHandler).transferTo(container, PlayerImpl);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return container;
        }

        InventoryOpenEvent event = new InventoryOpenEvent(bv);
        event.setCancelled(cancelled);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            ((IMixinScreenHandler)container).transferTo(player.currentScreenHandler, PlayerImpl);
            return null;
        }

        return container;
    }

    public static FireworkExplodeEvent callFireworkExplodeEvent(FireworkRocketEntity firework) {
        FireworkExplodeEvent event = new FireworkExplodeEvent((Firework) ((IMixinEntity) firework).getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static VillagerCareerChangeEvent callVillagerCareerChangeEvent(VillagerEntity vilager, Profession future, ChangeReason reason) {
        VillagerCareerChangeEvent event = new VillagerCareerChangeEvent((Villager) ((IMixinEntity)vilager).getBukkitEntity(), future, reason);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handleBlockFormEvent(World world, BlockPos pos, net.minecraft.block.BlockState block, Entity entity) {
        return handleBlockFormEvent(world, pos, block, 3, entity);
    }

    public static boolean handleBlockFormEvent(World world, BlockPos pos, net.minecraft.block.BlockState block, int flag, Entity entity) {
        CraftBlockState blockState = CraftBlockState.getBlockState(world, pos, flag);
        blockState.setData(block);

        BlockFormEvent event = (entity == null) ? new BlockFormEvent(blockState.getBlock(), blockState) : new EntityBlockFormEvent(((IMixinEntity)entity).getBukkitEntity(), blockState.getBlock(), blockState);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (!event.isCancelled())
            blockState.update(true);
        return !event.isCancelled();
    }

    @SuppressWarnings("unchecked")
    public static Cancellable handleStatisticsIncrease(PlayerEntity entityHuman, net.minecraft.stat.Stat<?> statistic, int current, int newValue) {
        Player player = (Player) ((IMixinServerEntityPlayer) entityHuman).getBukkitEntity();
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
            EntityType entityType = CraftStatistic.getEntityTypeFromStatistic((net.minecraft.stat.Stat<net.minecraft.entity.EntityType<?>>) statistic);
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
        EntityPickupItemEvent event = new EntityPickupItemEvent((org.bukkit.entity.LivingEntity) ((IMixinEntity)who).getBukkitEntity(), (org.bukkit.entity.Item) ((IMixinEntity)item).getBukkitEntity(), remaining);
        event.setCancelled(cancelled);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityEnterLoveModeEvent callEntityEnterLoveModeEvent(PlayerEntity entityHuman, AnimalEntity entityAnimal, int loveTicks) {
        EntityEnterLoveModeEvent entityEnterLoveModeEvent = new EntityEnterLoveModeEvent((Animals) ((IMixinEntity)entityAnimal).getBukkitEntity(), entityHuman != null ? (HumanEntity) ((IMixinEntity)entityHuman).getBukkitEntity() : null, loveTicks);
        Bukkit.getPluginManager().callEvent(entityEnterLoveModeEvent);
        return entityEnterLoveModeEvent;
    }

    public static ItemStack callPreCraftEvent(Inventory matrix, Inventory resultInventory, ItemStack result, InventoryView lastCraftView, boolean isRepair) {
        CraftInventoryCrafting inventory = new CraftInventoryCrafting(matrix, resultInventory);
        inventory.setResult(CraftItemStack.asCraftMirror(result));

        PrepareItemCraftEvent event = new PrepareItemCraftEvent(inventory, lastCraftView, isRepair);
        Bukkit.getPluginManager().callEvent(event);

        org.bukkit.inventory.ItemStack bitem = event.getInventory().getResult();

        return CraftItemStack.asNMSCopy(bitem);
    }

    public static EntityTransformEvent callEntityTransformEvent(net.minecraft.entity.LivingEntity original, net.minecraft.entity.LivingEntity coverted, EntityTransformEvent.TransformReason transformReason) {
        return callEntityTransformEvent(original, Collections.singletonList(coverted), transformReason);
    }

    public static EntityTransformEvent callEntityTransformEvent(net.minecraft.entity.LivingEntity original, List<net.minecraft.entity.LivingEntity> convertedList, EntityTransformEvent.TransformReason convertType) {
        List<org.bukkit.entity.Entity> list = new ArrayList<>();
        for (net.minecraft.entity.LivingEntity entityLiving : convertedList)
            list.add(((IMixinEntity)entityLiving).getBukkitEntity());

        if (list.size() <= 0)
            return null;

        EntityTransformEvent event = new EntityTransformEvent(((IMixinEntity)original).getBukkitEntity(), list, convertType);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityPlaceEvent callEntityPlaceEvent(ItemUsageContext itemactioncontext, Entity entity) {
        return callEntityPlaceEvent(itemactioncontext.getWorld(), itemactioncontext.getBlockPos(), itemactioncontext.getSide(), itemactioncontext.getPlayer(), entity);
    }

    public static EntityPlaceEvent callEntityPlaceEvent(World world, BlockPos clickPosition, Direction clickedFace, PlayerEntity human, Entity entity) {
        Player who = (human == null) ? null : (Player) ((IMixinEntity)human).getBukkitEntity();
        org.bukkit.block.Block blockClicked = CraftBlock.at((ServerWorld) world, clickPosition);
        org.bukkit.block.BlockFace blockFace = org.bukkit.craftbukkit.block.CraftBlock.notchToBlockFace(clickedFace);

        EntityPlaceEvent event = new EntityPlaceEvent(((IMixinEntity)entity).getBukkitEntity(), who, blockClicked, blockFace);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }
    
    public static EntityPlaceEvent callEntityPlaceEvent(World world, BlockPos clickPosition, Direction clickedFace, PlayerEntity  human, Entity entity, Hand enumhand) {
        Player who = (human == null) ? null : (Player) ((IMixinEntity)human).getBukkitEntity();
        org.bukkit.block.Block blockClicked = CraftBlock.at((ServerWorld) world, clickPosition);
        org.bukkit.block.BlockFace blockFace = org.bukkit.craftbukkit.block.CraftBlock.notchToBlockFace(clickedFace);

        //EntityPlaceEvent event = new EntityPlaceEvent(((IMixinEntity)entity).getBukkitEntity(), who, blockClicked, blockFace, EquipmentSlotImpl.getHand(enumhand));
        EntityPlaceEvent event = new EntityPlaceEvent(((IMixinEntity)entity).getBukkitEntity(), who, blockClicked, blockFace/*, Hand.MAIN_HAND*/);

        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    public static CreeperPowerEvent callCreeperPowerEvent(Entity creeper, Entity lightning, CreeperPowerEvent.PowerCause cause) {
        CreeperPowerEvent event = new CreeperPowerEvent((Creeper) ((IMixinEntity)creeper).getBukkitEntity(), (LightningStrike) ((IMixinEntity)lightning).getBukkitEntity(), cause);
        ((IMixinEntity)creeper).getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static void callPlayerItemBreakEvent(PlayerEntity human, ItemStack brokenItem) {
        CraftItemStack item = CraftItemStack.asCraftMirror(brokenItem);
        PlayerItemBreakEvent event = new PlayerItemBreakEvent((Player) ((IMixinEntity)human).getBukkitEntity(), item);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static EntityTargetLivingEntityEvent callEntityTargetLivingEvent(Entity entity, net.minecraft.entity.LivingEntity target, EntityTargetEvent.TargetReason reason) {
        EntityTargetLivingEntityEvent event = new EntityTargetLivingEntityEvent(((IMixinEntity)entity).getBukkitEntity(), (target == null) ? null : (org.bukkit.entity.LivingEntity) (((IMixinEntity)entity).getBukkitEntity()), reason);
        ((IMixinEntity)entity).getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static HorseJumpEvent callHorseJumpEvent(Entity horse, float power) {
        HorseJumpEvent event = new HorseJumpEvent((AbstractHorse) ((IMixinEntity)horse).getBukkitEntity(), power);
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * ItemMergeEvent
     */
    public static ItemMergeEvent callItemMergeEvent(ItemEntity merging, ItemEntity mergingWith) {
        org.bukkit.entity.Item entityMerging = (org.bukkit.entity.Item) ((IMixinEntity)merging).getBukkitEntity();
        org.bukkit.entity.Item entityMergingWith = (org.bukkit.entity.Item) ((IMixinEntity)mergingWith).getBukkitEntity();

        ItemMergeEvent event = new ItemMergeEvent(entityMerging, entityMergingWith);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerDeathEvent callPlayerDeathEvent(ServerPlayerEntity victim, List<org.bukkit.inventory.ItemStack> drops, String deathMessage, boolean keepInventory) {
        PlayerImpl entity = (PlayerImpl) ((IMixinServerEntityPlayer)victim).getBukkitEntity();
        PlayerDeathEvent event = new PlayerDeathEvent(entity, drops, ((IMixinLivingEntity)victim).getExpReward(), 0, deathMessage);
        event.setKeepInventory(keepInventory);
        org.bukkit.World world = entity.getWorld();
        Bukkit.getServer().getPluginManager().callEvent(event);

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            world.dropItem(entity.getLocation(), stack);
        }
        return event;
    }

    public static LootGenerateEvent callLootGenerateEvent(Inventory inventory, LootTable lootTable, LootContext lootInfo, List<ItemStack> loot, boolean plugin) {
        WorldImpl world = ((IMixinWorld)lootInfo.getWorld()).getWorldImpl();
        Entity entity = lootInfo.get(LootContextParameters.THIS_ENTITY);
        NamespacedKey key = CraftNamespacedKey.fromMinecraft(((IMixinLootManager)world.getHandle().getServer().getLootManager()).getLootTableToKeyMapBF().get(lootTable));
        LootTableImpl craftLootTable = new LootTableImpl(key, lootTable);
        List<org.bukkit.inventory.ItemStack> bukkitLoot = loot.stream().map(CraftItemStack::asCraftMirror).collect(Collectors.toCollection(ArrayList::new));

        LootGenerateEvent event = new LootGenerateEvent(world, (entity != null ? ((IMixinEntity)entity).getBukkitEntity() : null), ((IMixinInventory)inventory).getOwner(), craftLootTable, LootTableImpl.convertContext(lootInfo), bukkitLoot, plugin);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityDeathEvent callEntityDeathEvent(net.minecraft.entity.LivingEntity victim, List<org.bukkit.inventory.ItemStack> drops) {
        if (((IMixinEntity)victim).getBukkitEntity() instanceof UnknownEntity) {
            UnknownEntity uk = (UnknownEntity) ((IMixinEntity)victim).getBukkitEntity();
            BukkitFabricMod.LOGGER.info("Oh no! " + net.minecraft.entity.EntityType.getId(uk.nms.getType()).toString() + " is an unknown bukkit entity!");
        }
        LivingEntityImpl entity = (LivingEntityImpl) ((IMixinEntity)victim).getBukkitEntity();
        EntityDeathEvent event = new EntityDeathEvent(entity, drops, ((IMixinLivingEntity)victim).getExpReward());

        if ((null == entity) || (null == entity.getWorld())) {
            boolean e = (null == entity);
            BukkitFabricMod.LOGGER.info("WARNING: Null " + (e ? "entity" : "world") + "!");
            return event;
        }

        WorldImpl world = (WorldImpl) entity.getWorld();
        Bukkit.getServer().getPluginManager().callEvent(event);

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0) continue;

            world.dropItem(entity.getLocation(), stack); // Paper - note: dropItem already clones due to this being bukkit -> NMS
            if (stack instanceof CraftItemStack) stack.setAmount(0); // Paper
        }

        return event;
    }

    public static ExpBottleEvent callExpBottleEvent(Entity entity, int exp) {
        ThrownExpBottle bottle = (ThrownExpBottle) ((IMixinEntity)entity).getBukkitEntity();
        ExpBottleEvent event = new ExpBottleEvent(bottle, exp);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityShootBowEvent callEntityShootBowEvent(net.minecraft.entity.LivingEntity who, ItemStack bow, ItemStack consumableItem, Entity entityArrow, Hand hand, float force, boolean consumeItem) {
        LivingEntity shooter = (LivingEntity) ((IMixinEntity)who).getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(bow);
        CraftItemStack itemConsumable = CraftItemStack.asCraftMirror(consumableItem);
        org.bukkit.entity.Entity arrow = ((IMixinEntity)entityArrow).getBukkitEntity();
        EquipmentSlot handSlot = (hand == Hand.MAIN_HAND) ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;

        if (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0)
            itemInHand = null;

        EntityShootBowEvent event = new EntityShootBowEvent(shooter, itemInHand, itemConsumable, arrow, handSlot, force, consumeItem);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Bucket methods
     */
    public static PlayerBucketEmptyEvent callPlayerBucketEmptyEvent(ServerWorld world, PlayerEntity who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemInHand) {
        return (PlayerBucketEmptyEvent) getPlayerBucketEvent(false, world, who, changed, clicked, clickedFace, itemInHand, Items.BUCKET);
    }

    public static PlayerBucketFillEvent callPlayerBucketFillEvent(ServerWorld world, PlayerEntity who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemInHand, net.minecraft.item.Item bucket) {
        return (PlayerBucketFillEvent) getPlayerBucketEvent(true, world, who, clicked, changed, clickedFace, itemInHand, bucket);
    }

    private static PlayerEvent getPlayerBucketEvent(boolean isFilling, ServerWorld world, PlayerEntity who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemstack, net.minecraft.item.Item item) {
        return getPlayerBucketEvent(isFilling, world, who, changed, clicked, clickedFace, itemstack, item, null);
    }

    public static PlayerBucketEmptyEvent callPlayerBucketEmptyEvent(World world, PlayerEntity who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemstack, Hand enumHand) {
        return (PlayerBucketEmptyEvent) getPlayerBucketEvent(false, world, who, changed, clicked, clickedFace, itemstack, Items.BUCKET, enumHand);
    }

    public static PlayerBucketFillEvent callPlayerBucketFillEvent(World world, PlayerEntity who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemInHand, net.minecraft.item.Item bucket, Hand enumHand) {
        return (PlayerBucketFillEvent) getPlayerBucketEvent(true, world, who, clicked, changed, clickedFace, itemInHand, bucket, enumHand);
    }

    private static PlayerEvent getPlayerBucketEvent(boolean isFilling, World world, PlayerEntity who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemstack, net.minecraft.item.Item item, Hand enumHand) {
        // Paper end
        Player player = (Player) ((IMixinServerEntityPlayer)who).getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asNewCraftStack(item);
        Material bucket = CraftMagicNumbers.getMaterial(itemstack.getItem());

        CraftServer craftServer = (CraftServer) player.getServer();

        Block block = CraftBlock.at((ServerWorld) world, changed);
        Block blockClicked = CraftBlock.at((ServerWorld) world, clicked);
        BlockFace blockFace = CraftBlock.notchToBlockFace(clickedFace);

        // TODO - When we move to PaperAPI we need to add hand to event.
        PlayerEvent event;
        if (isFilling) {
            event = new PlayerBucketFillEvent(player, block, blockClicked, blockFace, bucket, itemInHand); 
            ((PlayerBucketFillEvent) event).setCancelled(!canBuild((ServerWorld) world, player, changed.getX(), changed.getZ()));
        } else {
            event = new PlayerBucketEmptyEvent(player, block, blockClicked, blockFace, bucket, itemInHand);
            ((PlayerBucketEmptyEvent) event).setCancelled(!canBuild((ServerWorld) world, player, changed.getX(), changed.getZ()));
        }

        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, BlockPos pos, IgniteCause cause, Entity igniter) {
        BlockIgniteEvent event = new BlockIgniteEvent(((IMixinWorld)world).getWorldImpl().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), cause, ((IMixinEntity)igniter).getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerExpChangeEvent callPlayerExpChangeEvent(PlayerEntity entity, int expAmount) {
        Player player = (Player) ((IMixinEntity)entity).getBukkitEntity();
        PlayerExpChangeEvent event = new PlayerExpChangeEvent(player, expAmount);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerItemMendEvent callPlayerItemMendEvent(PlayerEntity entity, ExperienceOrbEntity orb, net.minecraft.item.ItemStack nmsMendedItem, int repairAmount) {
        Player player = (Player) ((IMixinEntity)entity).getBukkitEntity();
        org.bukkit.inventory.ItemStack bukkitStack = CraftItemStack.asCraftMirror(nmsMendedItem);
        PlayerItemMendEvent event = new PlayerItemMendEvent(player, bukkitStack, (ExperienceOrb) ((IMixinEntity)orb).getBukkitEntity(), repairAmount);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handlePlayerShearEntityEvent(net.minecraft.entity.LivingEntity player, Entity sheared, ItemStack shears, Hand hand) {
        if (!(player instanceof PlayerEntity)) return true;

        PlayerShearEntityEvent event = new PlayerShearEntityEvent((Player) ((IMixinEntity)player).getBukkitEntity(), ((IMixinEntity)sheared).getBukkitEntity(), CraftItemStack.asCraftMirror(shears), (hand == Hand.OFF_HAND ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND));
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static PlayerUnleashEntityEvent callPlayerUnleashEntityEvent(MobEntity entity, PlayerEntity player) {
        PlayerUnleashEntityEvent event = new PlayerUnleashEntityEvent(((IMixinEntity)entity).getBukkitEntity(), (Player) ((IMixinEntity)player).getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerLeashEntityEvent callPlayerLeashEntityEvent(MobEntity entity, Entity leashHolder, PlayerEntity player) {
        PlayerLeashEntityEvent event = new PlayerLeashEntityEvent(((IMixinEntity)entity).getBukkitEntity(), ((IMixinEntity)leashHolder).getBukkitEntity(), (Player) ((IMixinEntity)player).getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerLevelChangeEvent callPlayerLevelChangeEvent(Player player, int oldLevel, int newLevel) {
        PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(player, oldLevel, newLevel);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
    
    public static EntityBreakDoorEvent callEntityBreakDoorEvent(Entity entity, BlockPos pos) {
        org.bukkit.entity.Entity entity1 = ((IMixinEntity)entity).getBukkitEntity();
        Block block = CraftBlock.at((ServerWorld) entity.getEntityWorld(), pos);

        EntityBreakDoorEvent event = new EntityBreakDoorEvent((LivingEntity) entity1, block);
        entity1.getServer().getPluginManager().callEvent(event);

        return event;
    }

    public static boolean handleBlockSpreadEvent(World world, BlockPos source, BlockPos target, net.minecraft.block.BlockState block, int flag) {
        // Suppress during worldgen
        if (!(world instanceof World)) {
            world.setBlockState(target, block, flag);
            return true;
        }

        CraftBlockState state = CraftBlockState.getBlockState(world, target, flag);
        state.setData(block);

        BlockSpreadEvent event = new BlockSpreadEvent(((IMixinWorld) world).getWorldImpl().getBlockAt(target.getX(), target.getY(), target.getZ()), ((IMixinWorld) world).getWorldImpl().getBlockAt(source.getX(), source.getY(), source.getZ()), state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }
        return !event.isCancelled();
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(Entity entity, BlockPos position, net.minecraft.block.BlockState newBlock) {
        return callEntityChangeBlockEvent(entity, position, newBlock, false);
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(Entity entity, BlockPos position, net.minecraft.block.BlockState newBlock, boolean cancelled) {
        Block block = ((IMixinWorld) entity).getWorldImpl().getBlockAt(position.getX(), position.getY(), position.getZ());

        EntityChangeBlockEvent event = new EntityChangeBlockEvent(((IMixinEntity) entity).getBukkitEntity(), block, CraftBlockData.fromData(newBlock));
        event.setCancelled(cancelled);
        event.getEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handleBlockGrowEvent(World world, BlockPos pos, net.minecraft.block.BlockState block) {
        return handleBlockGrowEvent(world, pos, block, 3);
    }

    public static boolean handleBlockGrowEvent(World world, BlockPos pos, net.minecraft.block.BlockState newData, int flag) {
        Block block = ((IMixinWorld) world).getWorldImpl().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
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
    public static BlockFadeEvent callBlockFadeEvent(WorldAccess world, BlockPos pos, net.minecraft.block.BlockState newBlock) {
        CraftBlockState state = CraftBlockState.getBlockState(world, pos);
        state.setData(newBlock);

        BlockFadeEvent event = new BlockFadeEvent(state.getBlock(), state);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    @Deprecated
    public static boolean handleBellRingEvent_(ServerWorld world, BlockPos pos, Entity entity) {
        //Block block = CraftBlock.at(world, pos);
        //BellRingEvent event = new BellRingEvent(block, (entity != null) ? ((IMixinEntity) entity).getBukkitEntity() : null);
        //Bukkit.getPluginManager().callEvent(event);
        //return !event.isCancelled();
    	return false;
    }
    
    public static boolean handleBellRingEvent(ServerWorld world, BlockPos position, Direction direction, net.minecraft.entity.Entity entity) {
        CraftBlock block = CraftBlock.at(world, position);
        BlockFace bukkitDirection = CraftBlock.notchToBlockFace(direction);
        BellRingEvent event = new BellRingEvent((Block)block, bukkitDirection, (entity != null) ? ((IMixinEntity) entity).getBukkitEntity() : null);
        Bukkit.getPluginManager().callEvent((Event)event);
        return !event.isCancelled();
    }

    public static EntityBreedEvent callEntityBreedEvent(LivingEntity child, LivingEntity mother, LivingEntity father, LivingEntity breeder, ItemStack bredWith, int experience) {
        org.bukkit.entity.LivingEntity breederEntity = (org.bukkit.entity.LivingEntity) (breeder == null ? null : ((IMixinEntity)breeder).getBukkitEntity());
        CraftItemStack bredWithStack = bredWith == null ? null : CraftItemStack.asCraftMirror(bredWith).clone();

        EntityBreedEvent event = new EntityBreedEvent((org.bukkit.entity.LivingEntity) ((IMixinEntity)child).getBukkitEntity(), (org.bukkit.entity.LivingEntity) ((IMixinEntity) mother).getBukkitEntity(), (org.bukkit.entity.LivingEntity) ((IMixinEntity) father).getBukkitEntity(), breederEntity, bredWithStack, experience);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

}
