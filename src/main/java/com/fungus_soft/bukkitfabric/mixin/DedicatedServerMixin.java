package com.fungus_soft.bukkitfabric.mixin;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.PluginLoadOrder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.fungus_soft.bukkitfabric.BukkitLogger;
import com.fungus_soft.bukkitfabric.interfaces.IMixinCommandOutput;

import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.DedicatedPlayerManager;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.text.Text;

@Mixin(MinecraftDedicatedServer.class)
public class DedicatedServerMixin implements CommandOutput, IMixinCommandOutput {

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

        ((MinecraftDedicatedServer) (Object) this).setPlayerManager(new DedicatedPlayerManager((MinecraftDedicatedServer) (Object) this));
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

    @Override
    public CommandSender getBukkitSender(ServerCommandSource serverCommandSource) {
        return Bukkit.getConsoleSender();
    }

    @Override
    public boolean sendCommandFeedback() {
        return false;
    }

    @Override
    public void sendMessage(Text message) {
        Bukkit.getConsoleSender().sendMessage(message.toString());
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return false;
    }

    @Override
    public boolean shouldTrackOutput() {
        return false;
    }

}