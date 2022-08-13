package org.cardboardpowered.mixin.network;

import static org.bukkit.craftbukkit.CraftServer.server;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.cardboardpowered.impl.util.LazyPlayerSet;
import org.cardboardpowered.impl.util.WaitableImpl;
import org.minecarts.api.util.Multithreading;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Mixin(PlayerManager.class)
public class MixinPlayerManager_ChatEvent {

    @SuppressWarnings("deprecation")
    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Ljava/util/function/Function;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V", cancellable = true)
    public void cardboard_doChatEvent_PLRMGR(Text tmessage, Function<ServerPlayerEntity, Text> playerMessageFactory, MessageType type, UUID sender, CallbackInfo ci) {
        try {
            Player player = Bukkit.getPlayer(sender);
            if (type != MessageType.CHAT) return;

            Multithreading.runAsync(() -> {
                String s = CraftChatMessage.fromComponent(tmessage);
                if (s.indexOf('>') != -1) s = s.substring(s.indexOf('>')+1).trim();

                if (null != player) {
                    AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, player, s, new LazyPlayerSet(CraftServer.server));
        
                    if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
                        // Evil plugins still listening to deprecated event
                        final PlayerChatEvent queueEvent = new PlayerChatEvent(player, event.getMessage(), event.getFormat(), event.getRecipients());
                        queueEvent.setCancelled(event.isCancelled());
                        Waitable<?> waitable = new WaitableImpl(()-> {
                            Bukkit.getPluginManager().callEvent(queueEvent);
        
                            if (queueEvent.isCancelled())
                                return;
        
                            String message = String.format(queueEvent.getFormat(), queueEvent.getPlayer().getDisplayName(), queueEvent.getMessage());
                            for (Text txt : CraftChatMessage.fromString(message))
                                CraftServer.server.sendSystemMessage(txt, queueEvent.getPlayer().getUniqueId());
                            if (((LazyPlayerSet) queueEvent.getRecipients()).isLazy()) {
                                for (ServerPlayerEntity plr : CraftServer.server.getPlayerManager().getPlayerList())
                                    for (Text txt : CraftChatMessage.fromString(message))
                                        plr.sendMessage(txt, false);
                            } else for (Player plr : queueEvent.getRecipients())
                                plr.sendMessage(message);
                        });
        
                        if (true)
                            ((IMixinMinecraftServer)CraftServer.server).getProcessQueue().add(waitable);
                        else waitable.run();
                        try {
                            waitable.get();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // This is proper habit for java. If we aren't handling it, pass it on!
                        } catch (ExecutionException e) {
                            throw new RuntimeException("Exception processing chat event", e.getCause());
                        }
                    } else {
                        if (event.isCancelled()) return;
        
                        s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
                        server.sendSystemMessage(new LiteralText(s), player.getUniqueId());
                        if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
                            for (ServerPlayerEntity recipient : server.getPlayerManager().players)
                                for (Text txt : CraftChatMessage.fromString(s))
                                    recipient.sendMessage(txt, MessageType.CHAT, player.getUniqueId());
                        } else for (Player recipient : event.getRecipients())
                            recipient.sendMessage(s);
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ci.cancel();
    }

    @Shadow
    public void sendToAll(Packet<?> packet) {
    }

}