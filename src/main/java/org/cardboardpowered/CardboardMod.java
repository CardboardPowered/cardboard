/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
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
package org.cardboardpowered;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
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
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.cardboardpowered.api.event.CardboardEventManager;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.cardboardpowered.bridge.world.level.block.entity.BlockEntityBridge;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;
import org.cardboardpowered.library.LibraryManager;

import io.papermc.paper.plugin.PluginInitializerManager;
import joptsimple.OptionSet;
import me.isaiah.common.event.EventHandler;
import me.isaiah.common.event.EventRegistery;
import me.isaiah.common.event.block.BlockEntityWriteNbtEvent;
import me.isaiah.common.event.block.LeavesDecayEvent;
import me.isaiah.common.event.entity.BlockEntityLoadEvent;
import me.isaiah.common.event.entity.CampfireBlockEntityCookEvent;
import me.isaiah.common.event.entity.EntityPortalCollideEvent;
import me.isaiah.common.event.entity.player.PlayerGamemodeChangeEvent;
import me.isaiah.common.event.entity.player.ServerPlayerInitEvent;
import me.isaiah.common.event.server.ServerWorldInitEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ServerLevelData;

import org.bukkit.craftbukkit.CraftWorld;

/**
 * Cardbord Mod - Spigot/Paper API for Fabric
 *
 * @author isaiah
 */
@SuppressWarnings({ "removal", "deprecation" })
public class CardboardMod implements ModInitializer {

    public static Logger LOGGER = BukkitLogger.getLogger();
    public static boolean isAfterWorldLoad = false;
    public static final Random random = new Random();

    public static Method GET_SERVER;

    // Set by LibraryManager
    public static String paperVersion = "";
	public static OptionSet options;

    @Override
    public void onInitialize() {
        FabricLoader loader = FabricLoader.getInstance();
        Optional<ModContainer> omcc = loader.getModContainer("minecraft");
        String mc = "";

        if (omcc.isPresent()) {
            ModContainer mcc = omcc.get();
            String mcver = mcc.getMetadata().getVersion().getFriendlyString();
            mc = "- Minecraft " + mcver;
        }

        new File("plugins").mkdirs();

        int r = EventRegistery.registerAll(this);

        paperVersion = LibraryManager.INSTANCE.getPaperVersion();
        String details = " - Paper-API " + paperVersion + ". " + "Registered '" + r + "' iCommon events.";

        // Check for FabricBetterConsole
        if (CardboardConfig.isBetterConsole()) {
            Component message = Component.literal("Cardboard " + mc)
                    .withStyle(ChatFormatting.GOLD).append(details);
            LOGGER.info(message.getString());
        } else {

            LOGGER.info("Cardboard " + mc + details);
        }
        
     // Paste this inside your early mod initialization entry point (e.g. onInitialize)
        try {
            // Force the JVM to evaluate and index our freshly injected ASM method 
            // before BKCommonLib handles its plugin setup loops.
            java.lang.reflect.Method m = net.minecraft.server.MinecraftServer.class.getMethod("getServer");
            System.out.println("[Cardboard Fix] Successfully registered reflection hook: " + m.getName());
            
        } catch (NoSuchMethodException e) {
            System.err.println("[Cardboard Fix] Warning: Method reflection registration delayed!");
        }

        CardboardEventManager.INSTANCE.callCardboardEvents();
        
        System.out.println("loading PluginInitializerManager");
        // Only the dedicated server's Main parses command-line options; the client gets the defaults.
        if (options == null) options = CardboardOptions.parse(new String[0]);
        try {
			PluginInitializerManager.load(options);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public CraftPlayer getPlayer_0(ServerPlayer e) {
        return (CraftPlayer) ((ServerPlayerBridge)(Object)e).getBukkitEntity();
    }

    @EventHandler
    public void on_leaves_decay(LeavesDecayEvent ev) {
        CraftWorld w = (CraftWorld) ((LevelBridge)ev.world).cardboard$getWorld();
        org.bukkit.event.block.LeavesDecayEvent event =
                new org.bukkit.event.block.LeavesDecayEvent(w.getBlockAt(ev.pos.getX(), ev.pos.getY(), ev.pos.getZ()));
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled() || !(ev.world.getBlockState(ev.pos).getBlock() instanceof LeavesBlock)) {
            ev.setCanceled(true);
        }
    }

    @EventHandler
    public void on_world_init__(ServerWorldInitEvent ev) {
    	/*
    	FabricWorld fw = (FabricWorld) ev.getWorld();

        if (!(fw.mc instanceof ServerWorld)) {
            System.out.println("CLIENT WORLD!");
            return;
        }

        ServerWorld nms = ((ServerWorld) fw.mc);
        on_world_init_mc(nms);
        */
    }

    public static void on_world_init_mc(ServerLevel nms) {
        // Check if Server is null
        if (null == CraftServer.INSTANCE) {
            return;
        }


        String name = ((ServerLevelData) nms.getLevelData()).getLevelName();

        /*
        File fi = new File(name + "_the_end");
        File van = new File(new File(name), "DIM1");

        if (fi.exists()) {
            File dim = new File(fi, "DIM1");
            if (dim.exists()) {
                CardboardMod.LOGGER.info("---- Migration of world file: " + name + "_the_end !");
                CardboardMod.LOGGER.info("Cardboard is currently migrating the world back to the vanilla format!");
                if (dim.renameTo(van)) {
                    CardboardMod.LOGGER.info("---- Migration of old bukkit format folder complete ----");
                } else {
                    CardboardMod.LOGGER.info("Please follow these instructions: https://s.cardboardpowered.org/world-migration-info");
                }
                fi.delete();
            }
        }

        File fi2 = new File(name + "_nether");
        File van2 = new File(new File(name), "DIM-1");

        if (fi2.exists()) {
            File dim = new File(fi2, "DIM-1");
            if (dim.exists()) {
                CardboardMod.LOGGER.info("---- Migration of world file: " + fi2.getName() + " !");
                CardboardMod.LOGGER.info("Cardboard is currently migrating the world back to the vanilla format!");
                if (dim.renameTo(van2)) {
                    CardboardMod.LOGGER.info("---- Migration of old bukkit format folder complete ----");
                } else {
                    CardboardMod.LOGGER.info("Please follow these instructions: https://s.cardboardpowered.org/world-migration-info");
                }
                fi.delete();
            }
        }
        */

        if (CraftServer.INSTANCE.worlds.containsKey(name)) {
            if (nms.dimension() == Level.NETHER) {
                name = name + "_nether";
                // fi2.mkdirs(); // Keep empty directory to fool plugins, ex. Multiverse.
            }
            if (nms.dimension() == Level.END) {
                name = name + "_the_end";
                // fi.mkdirs();
            }

            if (CraftServer.INSTANCE.worlds.containsKey(name)) {
                // Fabric-mod added world
                name = nms.dimension().identifier().toDebugFileName();
                new File(name).mkdirs();
            }


            ((LevelBridge)nms).set_bukkit_world( new CraftWorld(name, nms) );
            CraftServer.INSTANCE.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(((LevelBridge)nms).cardboard$getWorld()));
        } else {
            ((LevelBridge)nms).set_bukkit_world( new CraftWorld(name, nms) );
        }

        // Object o = nms.convertable;

        // this.uuid = WorldUUID.getUUID(levelStorageAccess.getDimensionPath(nms.getDimension()).toFile());
        // nms.cardboard$set_uuid(Utils.getWorldUUID(((IMixinWorld)nms).getCraftWorld().getWorldFolder())); 

        ((CraftServer)Bukkit.getServer()).addWorldToMap( ((LevelBridge)nms).cardboard$getWorld() );
    }

    // TODO
    //public File getWorldFolder() {
    // FIXME BROKEN (check for DMM1 & DMM-1)
    //	return CraftServer.server.getRunDirectory().toFile();
    //}

    @EventHandler
    public void onPlayerInit(ServerPlayerInitEvent ev) {
        // Replaced as of 1/24
    }

    @SuppressWarnings("removal")
    @EventHandler
    public void onGamemodeChange(PlayerGamemodeChangeEvent ev) {
        PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent((Player) ((EntityBridge)ev.getPlayer().getMC()).getBukkitEntity(), GameMode.getByValue(ev.getNewGamemode().getId()));
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ev.setCanceled(true);
        }
    }

    @EventHandler
    public void onBlockEntityLoadEnd(BlockEntityLoadEvent ev) {
        BlockEntityBridge mc = (BlockEntityBridge) ((BlockEntity) ev.getMC());

        mc.setCardboardPersistentDataContainer( new CraftPersistentDataContainer(mc.getCardboardDTR()) );

        CompoundTag tag = (CompoundTag) ev.getElement();
        CraftPersistentDataContainer pdc = mc.getPersistentDataContainer();
        tag.getCompound("PublicBukkitValues").ifPresent(pdc::putAll);;
    }

    @EventHandler
    public void onBlockEntitySaveEnd(BlockEntityWriteNbtEvent ev) {
        BlockEntityBridge mc = (BlockEntityBridge) ((BlockEntity) ev.getMC());

        CompoundTag tag = (CompoundTag) ev.getElement();
        CraftPersistentDataContainer persistentDataContainer = mc.getPersistentDataContainer();
        if (persistentDataContainer != null && !persistentDataContainer.isEmpty())
            tag.put("PublicBukkitValues", persistentDataContainer.toTagCompound());
    }

    /**
     * iCommonLib CampfireBlockEntityCookEvent -> Bukkit BlockCookEvent
     */
    @EventHandler
    public void onCampfireCook(CampfireBlockEntityCookEvent ev) {
        Object[] ob = ev.getMcObjects();
        Level w = (Level) ob[0];
        BlockPos pos = (BlockPos) ob[1];
        ItemStack itemstack = (ItemStack) ob[2];
        ItemStack itemstack1 = (ItemStack) ob[3];

        CraftItemStack source = CraftItemStack.asCraftMirror(itemstack);
        org.bukkit.inventory.ItemStack result = CraftItemStack.asBukkitCopy(itemstack1);

        BlockCookEvent blockCookEvent = new BlockCookEvent(CraftBlock.at((ServerLevel) w, pos), source, result);
        CraftServer.INSTANCE.getPluginManager().callEvent(blockCookEvent);

        if (blockCookEvent.isCancelled()) {
            ev.setCanceled(true);
            return;
        }

        result = blockCookEvent.getResult();
        ev.setResult( CraftItemStack.asNMSCopy(result) );
    }

    /**
     * iCommonLib EntityPortalCollideEvent -> Bukkit EntityPortalEnterEvent
     */
    @EventHandler
    public void onNetherPortalEnter(EntityPortalCollideEvent ev) {
        Entity entity = ev.getEntity();
        BlockPos pos = ev.getBlockPos();
        Level world = ev.getEntity().level(); // TODO: should we add EntityPortalCollideEvent.getWorld() ?

        if (!entity.isPassenger() && !entity.isVehicle() && entity.canUsePortal(true)) {
            EntityPortalEnterEvent event = new EntityPortalEnterEvent(((EntityBridge)entity).getBukkitEntity(), new org.bukkit.Location(((LevelBridge)world).cardboard$getWorld(), pos.getX(), pos.getY(), pos.getZ()));
            Bukkit.getPluginManager().callEvent(event);
        }
    }

}