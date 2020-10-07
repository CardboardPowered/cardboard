package com.javazilla.bukkitfabric.impl;

import java.net.InetAddress;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.GuiCloseC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
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
        WorldImpl WorldImpl = ((IMixinWorld)world).getWorldImpl();
        CraftServer craftServer = CraftServer.INSTANCE;

        Player player = (Player) ((IMixinServerEntityPlayer)(ServerPlayerEntity)who).getBukkitEntity();

        Block blockClicked = WorldImpl.getBlockAt(x, y, z);
        Block placedBlock = replacedBlockState.getBlock();

        boolean canBuild = canBuild(world, player, placedBlock.getX(), placedBlock.getZ());

        org.bukkit.inventory.ItemStack item;
        EquipmentSlot equipmentSlot;
        if (hand == Hand.MAIN_HAND) {
            item = player.getInventory().getItemInMainHand();
            equipmentSlot = EquipmentSlot.HAND;
        } else {
            item = player.getInventory().getItemInOffHand();
            equipmentSlot = EquipmentSlot.OFF_HAND;
        }

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

    public static void callRedstoneChange(World world, BlockPos pos, int oldPower, int newPower) {
        // TODO Auto-generated method stub
    }

    public static boolean handlePlayerRecipeListUpdateEvent(ServerPlayerEntity entityplayer, Identifier minecraftkey) {
        // TODO Auto-generated method stub
        return true;
    }

    public static void callProjectileHitEvent(ProjectileEntity projectileEntity, HitResult movingobjectposition) {
        // TODO Auto-generated method stub
    }

    public static ScreenHandler callInventoryOpenEvent(ServerPlayerEntity player, ScreenHandler container) {
        return callInventoryOpenEvent(player, container, false);
    }

    public static ScreenHandler callInventoryOpenEvent(ServerPlayerEntity player, ScreenHandler container, boolean cancelled) {
        if (player.currentScreenHandler != player.playerScreenHandler)
            player.networkHandler.onGuiClose(new GuiCloseC2SPacket(player.currentScreenHandler.syncId));
        CraftPlayer craftPlayer = (CraftPlayer) ((IMixinServerEntityPlayer)player).getBukkitEntity();
        ((IMixinScreenHandler)player.currentScreenHandler).transferTo(container, craftPlayer);

        InventoryOpenEvent event = new InventoryOpenEvent(((IMixinScreenHandler)container).getBukkitView());
        event.setCancelled(cancelled);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            ((IMixinScreenHandler)container).transferTo(player.currentScreenHandler, craftPlayer);
            return null;
        }

        return container;
    }

}