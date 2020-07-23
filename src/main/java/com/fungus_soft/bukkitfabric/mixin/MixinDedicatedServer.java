package com.fungus_soft.bukkitfabric.mixin;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.PluginLoadOrder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.fungus_soft.bukkitfabric.BukkitLogger;
import net.minecraft.server.dedicated.DedicatedPlayerManager;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

@Mixin(MinecraftDedicatedServer.class)
public class MixinDedicatedServer extends MixinMinecraftServer {

    @Inject(at = @At(value = "HEAD"), method = "setupServer()Z")
    private void initVar(CallbackInfoReturnable<Boolean> callbackInfo) {
        CraftServer.server = (MinecraftDedicatedServer) (Object) this;
    }

    @Inject(at = @At(value = "JUMP", ordinal = 8), method = "setupServer()Z") // TODO keep ordinal updated
    private void init(CallbackInfoReturnable<Boolean> callbackInfo) {
        BukkitLogger.getLogger().info("  ____          _     _     _  _    ");
        BukkitLogger.getLogger().info(" |  _ \\        | |   | |   (_)| |   ");
        BukkitLogger.getLogger().info(" | |_) | _   _ | | __| | __ _ | |_  ");
        BukkitLogger.getLogger().info(" |  _ < | | | || |/ /| |/ /| || __| ");
        BukkitLogger.getLogger().info(" | |_) || |_| ||   < |   < | || |_  ");
        BukkitLogger.getLogger().info(" |____/  \\__,_||_|\\_\\|_|\\_\\|_| \\__| ");
        BukkitLogger.getLogger().info("");

        ((MinecraftDedicatedServer) (Object) this).setPlayerManager(new DedicatedPlayerManager((MinecraftDedicatedServer) (Object) this, dimensionTracker, field_24371));
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


}