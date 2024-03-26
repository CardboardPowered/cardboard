package org.cardboardpowered.mixin.network;

import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.cardboardpowered.impl.entity.PlayerImpl;
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

@Mixin(PlayerManager.class)
public class MixinPlayerManager_ChatEvent {
	
	// 1.19.2:
	
    @Shadow
    public List<ServerPlayerEntity> players;
	
    @Shadow
    @Final
    private MinecraftServer server;
    
    
    public PlayerImpl getPlayer_0(ServerPlayerEntity e) {
        return (PlayerImpl) ((IMixinServerEntityPlayer)(Object)e).getBukkitEntity();
    }
    
    @Inject(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At("HEAD"), cancellable = true)
	private void onSendChatMessage(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params, CallbackInfo ci) {
    	
		 // BukkitFabricMod.LOGGER.info("onSendChatMessage: " + message.getContent().getString());
	}
    
    //     private void broadcast(SignedMessage message, Predicate<ServerPlayerEntity> shouldSendFiltered, @Nullable ServerPlayerEntity sender, MessageType.Parameters params) {

    
    @Overwrite
    public void broadcast(SignedMessage message, Predicate<ServerPlayerEntity> shouldSendFiltered, ServerPlayerEntity sender/*, MessageSourceProfile sourceProfile*/, MessageType.Parameters params) {
        BukkitFabricMod.LOGGER.info("BROADCAST DEBUG: " + message.getContent().getString());
        
    	boolean bl = this.verify(message);
        this.server.logChatMessage(message.getContent(), params, null);
        SentMessage sentMessage = SentMessage.of(message);
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
        
        
        String s = message.getContent().getString();
		boolean async = false; // TODO: allow async

		Player player = getPlayer_0(sender);
        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new LazyPlayerSet(CraftServer.server));
        Bukkit.getServer().getPluginManager().callEvent(event);

        BukkitFabricMod.LOGGER.info("Reg: " + PlayerChatEvent.getHandlerList().getRegisteredListeners().length);
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
                    for (ServerPlayerEntity plr : CraftServer.server.getPlayerManager().getPlayerList())
                        for (Text txt : CraftChatMessage.fromString(messag))
                            plr.sendMessage(txt, false);
                } else for (Player plr : queueEvent.getRecipients())
                    plr.sendMessage(messag);
            });
            
            if (async)
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
            // server.sendMessage(new LiteralTextContent(s));
            if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
                for (ServerPlayerEntity recipient : server.getPlayerManager().players)
                    for (Text txt : CraftChatMessage.fromString(s))
                        recipient.sendMessage(txt);
            } else for (Player recipient : event.getRecipients())
                recipient.sendMessage(s);
        }
        // sentMessage.afterPacketsSent((PlayerManager)(Object)this);
    }

    @Shadow
    private boolean verify(SignedMessage message/*, MessageSourceProfile profile*/) {
        return true;
    }

}
