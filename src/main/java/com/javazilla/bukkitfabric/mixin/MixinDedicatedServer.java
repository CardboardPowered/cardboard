package com.javazilla.bukkitfabric.mixin;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.PluginLoadOrder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.BukkitLogger;
import net.minecraft.server.dedicated.DedicatedPlayerManager;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.PendingServerCommand;

@Mixin(MinecraftDedicatedServer.class)
public class MixinDedicatedServer extends MixinMinecraftServer {

    @Shadow
    @Final
    private List<PendingServerCommand> commandQueue;

    @Inject(at = @At(value = "HEAD"), method = "setupServer()Z")
    private void initVar(CallbackInfoReturnable<Boolean> callbackInfo) {
        CraftServer.server = (MinecraftDedicatedServer) (Object) this;
    }

    @Inject(at = @At(value = "JUMP", ordinal = 8), method = "setupServer()Z") // TODO keep ordinal updated
    private void init(CallbackInfoReturnable<Boolean> ci) {
        BukkitLogger.getLogger().info("  ____          _     _     _  _    ");
        BukkitLogger.getLogger().info(" |  _ \\        | |   | |   (_)| |   ");
        BukkitLogger.getLogger().info(" | |_) | _   _ | | __| | __ _ | |_  ");
        BukkitLogger.getLogger().info(" |  _ < | | | || |/ /| |/ /| || __| ");
        BukkitLogger.getLogger().info(" | |_) || |_| ||   < |   < | || |_  ");
        BukkitLogger.getLogger().info(" |____/  \\__,_||_|\\_\\|_|\\_\\|_| \\__| ");
        BukkitLogger.getLogger().info("");

        ((MinecraftDedicatedServer) (Object) this).setPlayerManager(new DedicatedPlayerManager((MinecraftDedicatedServer) (Object) this, registryManager, saveHandler));
        Bukkit.setServer(new CraftServer((MinecraftDedicatedServer) (Object) this));

        Bukkit.getLogger().info("Loading Bukkit plugins...");
        File pluginsDir = new File("plugins");
        pluginsDir.mkdir();

        CraftServer s = ((CraftServer)Bukkit.getServer());
        if (CraftServer.server == null) CraftServer.server = (MinecraftDedicatedServer) (Object) this;

        s.loadPlugins();
        s.enablePlugins(PluginLoadOrder.STARTUP);
        
        Bukkit.getLogger().info("");
    }

    @Inject(at = @At("TAIL"), method = "exit")
    public void killProcess(CallbackInfo ci) {
        BukkitLogger.getLogger().info("Goodbye!");
        System.exit(0); // CraftBukkit
    }

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

}