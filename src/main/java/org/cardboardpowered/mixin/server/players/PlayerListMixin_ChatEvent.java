package org.cardboardpowered.mixin.server.players;

import org.cardboardpowered.CardboardMod;
import org.cardboardpowered.bridge.server.MinecraftServerBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.cardboardpowered.impl.util.LazyPlayerSet;
import org.cardboardpowered.impl.util.WaitableImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

@Mixin(PlayerList.class)
public class PlayerListMixin_ChatEvent {
	
	// 1.19.2:
	
    @Shadow
    public List<ServerPlayer> players;
	
    @Shadow
    @Final
    private MinecraftServer server;
    
    
    public CraftPlayer getPlayer_0(ServerPlayer e) {
        return (CraftPlayer) ((ServerPlayerBridge)(Object)e).getBukkitEntity();
    }
    
    @Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At("HEAD"), cancellable = true)
	private void onSendChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound params, CallbackInfo ci) {
    	
		 // CardboardMod.LOGGER.info("onSendChatMessage: " + message.getContent().getString());
	}
    
    //     private void broadcast(SignedMessage message, Predicate<ServerPlayerEntity> shouldSendFiltered, @Nullable ServerPlayerEntity sender, MessageType.Parameters params) {

    
    /**
     * @author cardboard
     * @reason Alternative chat events
     */
    @Overwrite
    public void broadcastChatMessage(PlayerChatMessage message, Predicate<ServerPlayer> shouldSendFiltered, ServerPlayer sender/*, MessageSourceProfile sourceProfile*/, ChatType.Bound params) {
    	boolean bl = this.verifyChatTrusted(message);
        this.server.logChatMessage(message.decoratedContent(), params, bl ? null : "Not Secure");

        // Only real player chat goes through the Bukkit chat events. /say, /me, /msg, command
        // blocks and mod broadcasts have no chat sender and must keep vanilla delivery, otherwise
        // they either NPE below or get reformatted with the chat format.
        if (sender == null || !params.chatType().is(ChatType.CHAT)) {
            OutgoingChatMessage vanillaMessage = OutgoingChatMessage.create(message);
            boolean fullyFiltered = message.isFullyFiltered();
            boolean notifySender = false;
            for (ServerPlayer recipient : this.players) {
                boolean filtered = shouldSendFiltered.test(recipient);
                recipient.sendChatMessage(vanillaMessage, filtered, params);
                if (sender == recipient) continue;
                notifySender |= fullyFiltered && filtered;
            }
            if (notifySender && sender != null) sender.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
            return;
        }

        String s = message.decoratedContent().getString();
		boolean async = false; // TODO: allow async

		Player player = getPlayer_0(sender);
        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new LazyPlayerSet(CraftServer.server));
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
            // Evil plugins still listening to deprecated event
            final PlayerChatEvent queueEvent = new PlayerChatEvent(player, event.getMessage(), event.getFormat(), event.getRecipients());
            queueEvent.setCancelled(event.isCancelled());
            
            queueEvent.getRecipients();
            
            Waitable<?> waitable = new WaitableImpl(()-> {
                Bukkit.getPluginManager().callEvent(queueEvent);

                if (queueEvent.isCancelled())
                    return;

                String messag = String.format(queueEvent.getFormat(), queueEvent.getPlayer().getDisplayName(), queueEvent.getMessage());
                //for (Text txt : CraftChatMessage.fromString(message))
                //    CraftServer.server.sendSystemMessage(txt, queueEvent.getPlayer().getUniqueId());
                if (((LazyPlayerSet) queueEvent.getRecipients()).isLazy()) {
                    for (ServerPlayer plr : CraftServer.server.getPlayerList().getPlayers())
                        for (Component txt : CraftChatMessage.fromString(messag))
                            plr.displayClientMessage(txt, false);
                } else for (Player plr : queueEvent.getRecipients())
                    plr.sendMessage(messag);
            });
            
            if (async)
                ((MinecraftServerBridge)CraftServer.server).getProcessQueue().add(waitable);
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
            // server.sendMessage(new LiteralTextContent(s));
            if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
                for (ServerPlayer recipient : server.getPlayerList().players)
                    for (Component txt : CraftChatMessage.fromString(s))
                        recipient.sendSystemMessage(txt);
            } else for (Player recipient : event.getRecipients())
                recipient.sendMessage(s);
        }
        // sentMessage.afterPacketsSent((PlayerManager)(Object)this);
    }

    @Shadow
    private boolean verifyChatTrusted(PlayerChatMessage message/*, MessageSourceProfile profile*/) {
        return true;
    }

}
