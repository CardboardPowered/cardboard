package com.fungus_soft.bukkitfabric.mixin;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLoadOrder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.fungus_soft.bukkitfabric.bukkitimpl.FakeLogger;
import com.fungus_soft.bukkitfabric.bukkitimpl.FakeServer;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

@Mixin(MinecraftDedicatedServer.class)
public class DedicatedServerMixin {

    @Inject(at = @At(value = "JUMP", ordinal = 8), method = "setupServer()Z") // TODO keep ordinal updated
    private void init(CallbackInfoReturnable<Boolean> callbackInfo) {
        FakeLogger.getLogger().info("  ____          _     _     _  _    ");
        FakeLogger.getLogger().info(" |  _ \\        | |   | |   (_)| |   ");
        FakeLogger.getLogger().info(" | |_) | _   _ | | __| | __ _ | |_  ");
        FakeLogger.getLogger().info(" |  _ < | | | || |/ /| |/ /| || __| ");
        FakeLogger.getLogger().info(" | |_) || |_| ||   < |   < | || |_  ");
        FakeLogger.getLogger().info(" |____/  \\__,_||_|\\_\\|_|\\_\\|_| \\__| ");
        FakeLogger.getLogger().info("");
        Bukkit.setServer(new FakeServer());

        Bukkit.getLogger().info("Loading Spoutcraft API ...");

        Bukkit.getLogger().info("Loading Bukkit plugins...");
        File pluginsDir = new File("plugins");
        pluginsDir.mkdir();

        FakeServer s = ((FakeServer)Bukkit.getServer());
        s.server = (MinecraftDedicatedServer) (Object) this;

        s.loadPlugins();
        s.enablePlugins(PluginLoadOrder.STARTUP);
        
        Bukkit.getLogger().info("");
    }

    @Inject(at = @At(value = "RETURN"), method = "setupServer()Z") // TODO keep ordinal updated
    private void finish(CallbackInfoReturnable<Boolean> callbackInfo) {
        FakeServer s = ((FakeServer)Bukkit.getServer());

        s.enablePlugins(PluginLoadOrder.POSTWORLD);
    }

}