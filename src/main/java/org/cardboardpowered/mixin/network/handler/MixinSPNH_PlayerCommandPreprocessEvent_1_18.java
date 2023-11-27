package org.cardboardpowered.mixin.network.handler;

import com.javazilla.bukkitfabric.BukkitLogger;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayNetworkHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.util.LazyPlayerSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 800)
public abstract class MixinSPNH_PlayerCommandPreprocessEvent_1_18 implements IMixinPlayNetworkHandler {

    @Shadow 
    public ServerPlayerEntity player;

    /**
     * @reason command
     * @author Cardboard 1.18.2
     */
    // TODO: 1.19
    @Inject(at = @At("HEAD"), method = "handleCommandExecution", cancellable = true)
    public void executeCommand_1_18_2(CommandExecutionC2SPacket packet, LastSeenMessageList messages, CallbackInfo ci) {
        String command = packet.command();
        BukkitLogger.getLogger().info(this.player.getName().getString() + " issued server command: " + command);
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(getPlayer(), command, new LazyPlayerSet(CraftServer.server));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        try {
            boolean b = Bukkit.getServer().dispatchCommand(event.getPlayer(), event.getMessage().substring(1));
            if (b) {
                ci.cancel();
                return;
            }
        } catch (org.bukkit.command.CommandException ex) {
            getPlayer().sendMessage(org.bukkit.ChatColor.RED + "An internal error occurred while attempting to perform this command");
            java.util.logging.Logger.getLogger(ServerPlayNetworkHandler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    public PlayerImpl getPlayer() {
        return (PlayerImpl) ((IMixinServerEntityPlayer)(Object)this.player).getBukkitEntity();
    }

}
