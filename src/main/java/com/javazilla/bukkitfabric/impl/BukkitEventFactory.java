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
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.javazilla.bukkitfabric.impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.Animals;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent.ChangeReason;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;

import com.javazilla.bukkitfabric.BukkitLogger;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.c2s.play.GuiCloseC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class BukkitEventFactory {

    public static Entity entityDamage;

    public static ServerListPingEvent callServerListPingEvent(Server craftServer, InetAddress address, String motd, int numPlayers, int maxPlayers) {
        ServerListPingEvent event = new ServerListPingEvent(address, motd, numPlayers, maxPlayers);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockPlaceEvent callBlockPlaceEvent(ServerWorld world, PlayerEntity who, Hand hand, BlockState replacedBlockState, int x, int y, int z) {
        WorldImpl worldImpl = ((IMixinWorld)world).getWorldImpl();
        CraftServer craftServer = CraftServer.INSTANCE;

        Player player = (Player) ((IMixinServerEntityPlayer)(ServerPlayerEntity)who).getBukkitEntity();

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
        org.bukkit.World bukkitWorld = ((IMixinWorld)(ServerWorld)world).getWorldImpl();
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
            hitBlock = CraftBlock.at((ServerWorld) entity.world, positionBlock.getBlockPos());
            hitFace = CraftBlock.notchToBlockFace(positionBlock.getSide());
        }

        org.bukkit.entity.Entity hitEntity = null;
        if (position.getType() == Type.ENTITY)
            hitEntity = ((IMixinEntity)((EntityHitResult) position).getEntity()).getBukkitEntity();

        org.bukkit.entity.Entity e = ((IMixinEntity)entity).getBukkitEntity();
        if (!(e instanceof Projectile)) {
            BukkitLogger.getLogger().warning("Entity \"" + e + "\" is not an instance of Projectile! Can not fire ProjectileHitEvent!");
            return;
        }

        ProjectileHitEvent event = new ProjectileHitEvent((Projectile) ((IMixinEntity)entity).getBukkitEntity(), hitEntity, hitBlock, hitFace);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    public static ScreenHandler callInventoryOpenEvent(ServerPlayerEntity player, ScreenHandler container) {
        return callInventoryOpenEvent(player, container, false);
    }

    public static ScreenHandler callInventoryOpenEvent(ServerPlayerEntity player, ScreenHandler container, boolean cancelled) {
        if (player.currentScreenHandler != player.playerScreenHandler)
            player.networkHandler.onGuiClose(new GuiCloseC2SPacket(player.currentScreenHandler.syncId));
        CraftPlayer craftPlayer = (CraftPlayer) ((IMixinServerEntityPlayer)player).getBukkitEntity();
        if (!(player.currentScreenHandler instanceof IMixinScreenHandler)) {
            System.out.println("FAILED TO FIRE InventoryOpenEvent! SCREEN HANDLER != IMixinInventory");
            return container;
        }
        try {
            ((IMixinScreenHandler)player.currentScreenHandler).transferTo(container, craftPlayer);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return container;
        }

        InventoryOpenEvent event = new InventoryOpenEvent(((IMixinScreenHandler)container).getBukkitView());
        event.setCancelled(cancelled);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            ((IMixinScreenHandler)container).transferTo(player.currentScreenHandler, craftPlayer);
            return null;
        }

        return container;
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

    public static Cancellable handleStatisticsIncrease(PlayerEntity player, Stat<?> statistic, int stat, int j) {
        // TODO Auto-generated method stub
        return null;
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

        EntityTransformEvent event = new EntityTransformEvent(((IMixinEntity)original).getBukkitEntity(), list, convertType);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    @SuppressWarnings("deprecation")
    public static EntityPlaceEvent callEntityPlaceEvent(ItemUsageContext itemactioncontext, Entity entity) {
        return callEntityPlaceEvent(itemactioncontext.getWorld(), itemactioncontext.getBlockPos(), itemactioncontext.getSide(), itemactioncontext.getPlayer(), entity);
    }

    @SuppressWarnings("deprecation")
    public static EntityPlaceEvent callEntityPlaceEvent(World world, BlockPos clickPosition, Direction clickedFace, PlayerEntity human, Entity entity) {
        Player who = (human == null) ? null : (Player) ((IMixinEntity)human).getBukkitEntity();
        org.bukkit.block.Block blockClicked = CraftBlock.at((ServerWorld) world, clickPosition);
        org.bukkit.block.BlockFace blockFace = org.bukkit.craftbukkit.block.CraftBlock.notchToBlockFace(clickedFace);

        EntityPlaceEvent event = new EntityPlaceEvent(((IMixinEntity)entity).getBukkitEntity(), who, blockClicked, blockFace);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

}