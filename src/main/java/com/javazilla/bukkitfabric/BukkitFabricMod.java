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
package com.javazilla.bukkitfabric;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.cardboardpowered.impl.CardboardPotionEffectType;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.world.WorldImpl;

import com.javazilla.bukkitfabric.interfaces.IMixinBlockEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import com.javazilla.bukkitfabric.nms.MappingsReader;

import me.isaiah.common.event.EventHandler;
import me.isaiah.common.event.EventRegistery;
import me.isaiah.common.event.block.BlockEntityWriteNbtEvent;
import me.isaiah.common.event.entity.BlockEntityLoadEvent;
import me.isaiah.common.event.entity.CampfireBlockEntityCookEvent;
import me.isaiah.common.event.entity.player.PlayerGamemodeChangeEvent;
import me.isaiah.common.event.entity.player.ServerPlayerInitEvent;
import me.isaiah.common.event.server.ServerWorldInitEvent;
import me.isaiah.common.fabric.FabricWorld;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;

@SuppressWarnings("deprecation")
public class BukkitFabricMod implements ModInitializer {

    public static Logger LOGGER = BukkitLogger.getLogger(); 
    public static boolean isAfterWorldLoad = false;
    public static final Random random = new Random();

    public static List<ServerLoginNetworkHandler> NETWORK_CACHE = new ArrayList<>();
    public static Method GET_SERVER;

    @Override
    public void onInitialize() {
        LOGGER.info("");
        LOGGER.info("Cardboard - CardboardPowered.org");
        LOGGER.info("");

        int r = EventRegistery.registerAll(this);
        LOGGER.info("Registered '" + r + "' iCommon events.");

        try {
            MappingsReader.main(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Cardboard mod Loaded.");
        new File("plugins").mkdirs();
        
        for (Object effect : Registry.STATUS_EFFECT) {
            try {
                org.bukkit.potion.PotionEffectType.registerPotionEffectType(new CardboardPotionEffectType((StatusEffect) effect));
            } catch (ArrayIndexOutOfBoundsException e) {
                // ignore
            }
        }
    }

    @EventHandler
    public void on_world_init__(ServerWorldInitEvent ev) {
        System.out.println("on_world_init");

        FabricWorld fw = (FabricWorld) ev.getWorld();

        if (!(fw.mc instanceof ServerWorld)) {
            System.out.println("CLIENT WORLD!");
            return;
        }

        ServerWorld nms = ((ServerWorld) fw.mc);
        String name = ((ServerWorldProperties) nms.getLevelProperties()).getLevelName();

        File fi = new File(name + "_the_end");
        File van = new File(new File(name), "DIM1");

        if (fi.exists()) {
            File dim = new File(fi, "DIM1");
            if (dim.exists()) {
                BukkitFabricMod.LOGGER.info("------ Migration of world file: " + name + "_the_end !");
                BukkitFabricMod.LOGGER.info("Cardboard is currently migrating the world back to the vanilla format!");
                BukkitFabricMod.LOGGER.info("Do to the differences between Spigot & Fabric world folders, we require migration.");
                if (dim.renameTo(van)) {
                    BukkitFabricMod.LOGGER.info("---- Migration of old bukkit format folder complete ----");
                } else {
                    BukkitFabricMod.LOGGER.info("---- Migration of old bukkit format folder FAILED! ----");
                    BukkitFabricMod.LOGGER.info("Please follow these instructions: https://s.cardboardpowered.org/world-migration-info");
                }
                fi.delete();
            }
        }
        
        File fi2 = new File(name + "_nether");
        File van2 = new File(new File(name), "DIM-1");

        if (fi2.exists()) {
            File dim = new File(fi2, "DIM-1");
            if (dim.exists()) {
                BukkitFabricMod.LOGGER.info("------ Migration of world file: " + fi2.getName() + " !");
                BukkitFabricMod.LOGGER.info("Cardboard is currently migrating the world back to the vanilla format!");
                BukkitFabricMod.LOGGER.info("Do to the differences between Spigot & Fabric world folders, we require migration.");
                if (dim.renameTo(van2)) {
                    BukkitFabricMod.LOGGER.info("---- Migration of old bukkit format folder complete ----");
                } else {
                    BukkitFabricMod.LOGGER.info("---- Migration of old bukkit format folder FAILED! ----");
                    BukkitFabricMod.LOGGER.info("Please follow these instructions: https://s.cardboardpowered.org/world-migration-info");
                }
                fi.delete();
            }
        }

        if (CraftServer.INSTANCE.worlds.containsKey(name)) {
            if (nms.getRegistryKey() == World.NETHER) {
                name = name + "_nether";
                fi2.mkdirs(); // Keep empty directory to fool plugins, ex. Multiverse.
            }
            if (nms.getRegistryKey() == World.END) {
                name = name + "_the_end";
                fi.mkdirs();
            }

            if (CraftServer.INSTANCE.worlds.containsKey(name)) {
                // Fabric-mod added world
                name = nms.getRegistryKey().getValue().toUnderscoreSeparatedString();
                new File(name).mkdirs();
            }
            

            ((IMixinWorld)nms).set_bukkit_world( new WorldImpl(name, nms) );
            CraftServer.INSTANCE.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(((IMixinWorld)nms).getWorldImpl()));
        } else {
            ((IMixinWorld)nms).set_bukkit_world( new WorldImpl(name, nms) );
        }
        ((CraftServer)Bukkit.getServer()).addWorldToMap( ((IMixinWorld)nms).getWorldImpl() );
    }

    @EventHandler
    public void onPlayerInit(ServerPlayerInitEvent ev) {
        ServerPlayerEntity e = (ServerPlayerEntity) ev.getPlayer().getMC();
        IMixinServerEntityPlayer ie = (IMixinServerEntityPlayer) e;

        if (null != Bukkit.getPlayer(e.getUuid())) {
            ie.setBukkit( (PlayerImpl) Bukkit.getPlayer(e.getUuid()) );
            ie.getBukkit().setHandle(e);
        } else {
            ie.setBukkit( new PlayerImpl(e) );
            CraftServer.INSTANCE.playerView.add(ie.getBukkit());
        }
    }

    @EventHandler
    public void onGamemodeChange(PlayerGamemodeChangeEvent ev) {
        PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent((Player) ((IMixinEntity)ev.getPlayer().getMC()).getBukkitEntity(), GameMode.getByValue(ev.getNewGamemode().getId()));
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ev.setCanceled(true);
        }
    }

    @EventHandler
    public void onBlockEntityLoadEnd(BlockEntityLoadEvent ev) {
        IMixinBlockEntity mc = (IMixinBlockEntity) ((BlockEntity) ev.getMC());

        mc.setCardboardPersistentDataContainer( new CraftPersistentDataContainer(mc.getCardboardDTR()) );

        NbtCompound tag = (NbtCompound) ev.getElement();
        NbtCompound persistentDataTag = tag.getCompound("PublicBukkitValues");
        if (persistentDataTag != null)
            mc.getPersistentDataContainer().putAll(persistentDataTag);
    }
    
    @EventHandler
    public void onBlockEntitySaveEnd(BlockEntityWriteNbtEvent ev) {
        IMixinBlockEntity mc = (IMixinBlockEntity) ((BlockEntity) ev.getMC());

        NbtCompound tag = (NbtCompound) ev.getElement();
        CraftPersistentDataContainer persistentDataContainer = mc.getPersistentDataContainer();
        if (persistentDataContainer != null && !persistentDataContainer.isEmpty())
            tag.put("PublicBukkitValues", persistentDataContainer.toTagCompound());
    }

    @EventHandler
    public void onCampfireCook(CampfireBlockEntityCookEvent ev) {
        Object[] ob = ev.getMcObjects();
        World w = (World) ob[0];
        BlockPos pos = (BlockPos) ob[1];
        ItemStack itemstack = (ItemStack) ob[2];
        ItemStack itemstack1 = (ItemStack) ob[3];
        
        CraftItemStack source = CraftItemStack.asCraftMirror(itemstack);
        org.bukkit.inventory.ItemStack result = CraftItemStack.asBukkitCopy(itemstack1);

        BlockCookEvent blockCookEvent = new BlockCookEvent(CraftBlock.at((ServerWorld) w, pos), source, result);
        CraftServer.INSTANCE.getPluginManager().callEvent(blockCookEvent);

        if (blockCookEvent.isCancelled()) {
            ev.setCanceled(true);
            return;
        }

        result = blockCookEvent.getResult();
        ev.setResult( CraftItemStack.asNMSCopy(result) );
    }

}