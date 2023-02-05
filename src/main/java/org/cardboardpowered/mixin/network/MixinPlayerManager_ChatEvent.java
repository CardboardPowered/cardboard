package org.cardboardpowered.mixin.network;

import java.util.List;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.BukkitFabricMod;

import net.minecraft.network.message.MessageSourceProfile;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerManager.class)
public class MixinPlayerManager_ChatEvent {
	
	// 1.19.2:
	
    @Shadow
    public List<ServerPlayerEntity> players;
	
    @Shadow
    @Final
    private MinecraftServer server;
    
    @Overwrite
    private void broadcast(SignedMessage message, Predicate<ServerPlayerEntity> shouldSendFiltered, ServerPlayerEntity sender, MessageSourceProfile sourceProfile, MessageType.Parameters params) {
        BukkitFabricMod.LOGGER.info("BROADCAST DEBUG: " + message.getContent().getString());
    	
    	boolean bl = this.verify(message, sourceProfile);
        this.server.logChatMessage(message.getContent(), params, bl ? null : "Not Secure");
        SentMessage sentMessage = SentMessage.of(message);
        boolean bl2 = message.isFullyFiltered();
        boolean bl3 = false;
        for (ServerPlayerEntity serverPlayerEntity : this.players) {
            boolean bl4 = shouldSendFiltered.test(serverPlayerEntity);
            serverPlayerEntity.sendChatMessage(sentMessage, bl4, params);
            if (sender == serverPlayerEntity) continue;
            bl3 |= bl2 && bl4;
        }
        if (bl3 && sender != null) {
            sender.sendMessage(PlayerManager.FILTERED_FULL_TEXT);
        }
        sentMessage.afterPacketsSent((PlayerManager)(Object)this);
    }

    @Shadow
    private boolean verify(SignedMessage message, MessageSourceProfile profile) {
        return true;
    }
    
	
	
	
	// TODO: 1.19

    /*@SuppressWarnings("deprecation")
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
    }*/

}