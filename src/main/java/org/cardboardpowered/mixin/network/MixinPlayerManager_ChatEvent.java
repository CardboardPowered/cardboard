package org.cardboardpowered.mixin.network;

import static org.bukkit.craftbukkit.CraftServer.server;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.cardboardpowered.impl.util.LazyPlayerSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Mixin(PlayerManager.class)
public class MixinPlayerManager_ChatEvent {

    @Inject(at = @At("HEAD"), method = "broadcastChatMessage", cancellable = true)
    public void broadcastChatMessage(Text message, MessageType type, UUID senderUuid, CallbackInfo ci) {
        Player player = Bukkit.getPlayer(senderUuid);
        if (null != player) {
            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, player, message.asString(), new LazyPlayerSet(CraftServer.server));
            Bukkit.getServer().getPluginManager().callEvent(event);
            
            if (event.isCancelled()) return;

            String s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
            server.sendSystemMessage(new LiteralText(s), player.getUniqueId());
            if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
                for (ServerPlayerEntity recipient : server.getPlayerManager().players)
                    for (Text txt : CraftChatMessage.fromString(s))
                        recipient.sendMessage(txt, MessageType.CHAT, player.getUniqueId());
            } else for (Player recipient : event.getRecipients())
                recipient.sendMessage(s);
            ci.cancel();
            return;
        }
    }

    @Shadow
    public void sendToAll(Packet<?> packet) {
    }

}