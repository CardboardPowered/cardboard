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
package com.javazilla.bukkitfabric.mixin;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.BukkitLogger;
import com.javazilla.bukkitfabric.impl.util.ServerShutdownThread;
import com.javazilla.bukkitfabric.interfaces.IMixinDedicatedServer;

import net.minecraft.server.dedicated.DedicatedPlayerManager;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.PendingServerCommand;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MixinDedicatedServer extends MixinMinecraftServer implements IMixinDedicatedServer {

    public MixinDedicatedServer(String string) {
        super(string);
    }

    @Shadow
    @Final
    private List<PendingServerCommand> commandQueue;

    @Inject(at = @At(value = "HEAD"), method = "setupServer()Z")
    private void initVar(CallbackInfoReturnable<Boolean> callbackInfo) {
        CraftServer.server = (MinecraftDedicatedServer) (Object) this;
        Runtime.getRuntime().addShutdownHook(new ServerShutdownThread());
    }

    @Inject(at = @At(value = "JUMP", ordinal = 8), method = "setupServer()Z") // TODO keep ordinal updated
    private void init(CallbackInfoReturnable<Boolean> ci) {
        BukkitLogger.getLogger().info(" Bukkit4Fabric Mod ");

        ((MinecraftDedicatedServer) (Object) this).setPlayerManager(new DedicatedPlayerManager((MinecraftDedicatedServer) (Object) this, registryManager, saveHandler));
        Bukkit.setServer(new CraftServer((MinecraftDedicatedServer) (Object) this));
        org.spigotmc.SpigotConfig.init(new File("spigot.yml"));

        Bukkit.getLogger().info("Loading Bukkit plugins...");
        File pluginsDir = new File("plugins");
        pluginsDir.mkdir();

        Bukkit.getPluginManager().registerInterface(JavaPluginLoader.class);
       // BukkitFabricMod.loadLibs();

        CraftServer s = ((CraftServer)Bukkit.getServer());
        if (CraftServer.server == null) CraftServer.server = (MinecraftDedicatedServer) (Object) this;

        s.loadPlugins();
        s.enablePlugins(PluginLoadOrder.STARTUP);
        
        Bukkit.getLogger().info("");
    }

    @Inject(at = @At("TAIL"), method = "exit")
    public void killProcess(CallbackInfo ci) {
        BukkitLogger.getLogger().info("Goodbye!");
    }

    /**
     * @author BukkitFabric
     * @reason ServerCommandEvent
     */
    @Overwrite
    public void executeQueuedCommands() {
        while (!this.commandQueue.isEmpty()) {
            PendingServerCommand servercommand = (PendingServerCommand) this.commandQueue.remove(0);

            ServerCommandEvent event = new ServerCommandEvent(CraftServer.INSTANCE.getConsoleSender(), servercommand.command);
            CraftServer.INSTANCE.getPluginManager().callEvent(event);
            if (event.isCancelled()) continue;
            servercommand = new PendingServerCommand(event.getCommand(), servercommand.source);

            CraftServer.INSTANCE.dispatchServerCommand(CraftServer.INSTANCE.getConsoleSender(), servercommand);
        }

    }

    @Override
    public boolean isDebugging() {
        return false;
    }

}