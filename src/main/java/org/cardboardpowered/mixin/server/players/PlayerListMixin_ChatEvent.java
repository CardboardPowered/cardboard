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
        CardboardMod.LOGGER.info("BROADCAST DEBUG: " + message.decoratedContent().getString());
        
    	boolean bl = this.verifyChatTrusted(message);
        this.server.logChatMessage(message.decoratedContent(), params, null);
        OutgoingChatMessage sentMessage = OutgoingChatMessage.create(message);
        boolean bl2 = message.isFullyFiltered();
        boolean bl3 = false;
        /*for (ServerPlayerEntity serverPlayerEntity : this.players) {
            boolean bl4 = shouldSendFiltered.test(serverPlayerEntity);
            serverPlayerEntity.sendChatMessage(sentMessage, bl4, params);
            if (sender == serverPlayerEntity) continue;
            bl3 |= bl2 && bl4;
        }
        if (bl3 && sender != null) {
            sender.sendMessage(PlayerManager.FILTERED_FULL_TEXT);
        }*/
        
        
        String s = message.decoratedContent().getString();
		boolean async = false; // TODO: allow async

		// Console and system broadcasts (e.g. /say) have no sending player.
		// AsyncPlayerChatEvent requires one, so deliver directly instead of
		// firing a player-chat event with a null player.
		if (sender == null) {
			for (ServerPlayer recipient : this.server.getPlayerList().players) {
				recipient.sendChatMessage(sentMessage, false, params);
			}
			return;
		}

		Player player = getPlayer_0(sender);
        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new LazyPlayerSet(CraftServer.server));
        Bukkit.getServer().getPluginManager().callEvent(event);

        CardboardMod.LOGGER.info("Reg: " + PlayerChatEvent.getHandlerList().getRegisteredListeners().length);
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
                    for (ServerPlayer plr : CraftServer.server.getPlayerList().getPlayers()) {
                        for (Component txt : CraftChatMessage.fromString(messag)) {
                            plr.sendSystemMessage(txt, false);
                        }
                    }
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
