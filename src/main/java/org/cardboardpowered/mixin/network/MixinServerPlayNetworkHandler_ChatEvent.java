package org.cardboardpowered.mixin.network;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 999)
public abstract class MixinServerPlayNetworkHandler_ChatEvent {

    @Shadow 
    public ServerPlayerEntity player;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    private static final AtomicIntegerFieldUpdater<ServerPlayNetworkHandler> chatSpamField = AtomicIntegerFieldUpdater.newUpdater(ServerPlayNetworkHandler.class, "messageCooldownBukkit");

    @Shadow
    public int teleportRequestTick;

    @Shadow
    public int ticks;

    @Shadow
    public Vec3d requestedTeleportPos;

    @Shadow
    public int requestedTeleportId;

    @Shadow public double lastTickX;
    @Shadow public double lastTickY;
    @Shadow public double lastTickZ;
    @Shadow public double updatedX;
    @Shadow public double updatedY;
    @Shadow public double updatedZ;
    @Shadow private boolean floating;
    @Shadow private int movePacketsCount;
    @Shadow private int lastTickMovePacketsCount;


    private ServerPlayNetworkHandler get() {
        return (ServerPlayNetworkHandler) (Object) this;
    }

    public PlayerImpl getPlayer_0() {
        return (PlayerImpl) ((IMixinServerEntityPlayer)(Object)this.player).getBukkitEntity();
    }

	// Content phase testing, with variable info


    
    // TODO: 1.19
    /**
     * @reason Bukkit AsyncChat
     * @author Bukkit4Fabric
     */
    /*
    @Overwrite
    public void handleMessage(TextStream.Message messag) {
        if (this.player.isRemoved() || this.player.getClientChatVisibility() == ChatVisibility.HIDDEN) {
            this.sendPacket(new GameMessageS2CPacket((new TranslatableText("chat.cannotSend")).formatted(Formatting.RED), MessageType.CHAT, player.getUuid()));
        } else {
            String message = messag.getRaw();
            boolean isSync = message.startsWith("/");
            this.player.updateLastActionTime();

            if (isSync)
                get().executeCommand(message);
            else if (message.isEmpty())
                BukkitLogger.getLogger().warning(this.player.getEntityName() + " tried to send an empty message");
            else if (this.player.getClientChatVisibility() == ChatVisibility.SYSTEM) {
                TranslatableText chatmessage = new TranslatableText("chat.cannotSend", new Object[0]);

                chatmessage.getStyle().withColor(Formatting.RED);
                this.sendPacket(new GameMessageS2CPacket(chatmessage, MessageType.CHAT, player.getUuid()));
            } else this.chat_(message, true);

            if (chatSpamField.addAndGet((ServerPlayNetworkHandler)(Object)this, 20) > 200 && !server.getPlayerManager().isOperator(this.player.getGameProfile())) {
                if (!isSync) {
                    Waitable<?> waitable = new WaitableImpl(() -> get().disconnect(new TranslatableText("disconnect.spam", new Object[0])));

                    ((IMixinMinecraftServer)(Object)server).getProcessQueue().add(waitable);

                    try {
                        waitable.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                } else get().disconnect(new TranslatableText("disconnect.spam", new Object[0]));

            }
        }
    }
          

    public void chat_(String s, boolean async) {
        if (s.isEmpty() || this.player.getClientChatVisibility() == ChatVisibility.HIDDEN)
            return;

        if (!async && s.startsWith("/")) {
            get().executeCommand(s);
        } else if (this.player.getClientChatVisibility() == ChatVisibility.SYSTEM) {
            // Do nothing, this is coming from a plugin
        } else {
            Player player = this.getPlayer_0();
            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new LazyPlayerSet(CraftServer.server));
            Bukkit.getServer().getPluginManager().callEvent(event);

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
                server.sendSystemMessage(new LiteralText(s), player.getUniqueId());
                if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
                    for (ServerPlayerEntity recipient : server.getPlayerManager().players)
                        for (Text txt : CraftChatMessage.fromString(s))
                            recipient.sendMessage(txt, MessageType.CHAT, player.getUniqueId());
                } else for (Player recipient : event.getRecipients())
                    recipient.sendMessage(s);
            }
        }
    }*/

    // 1.17 - onGameMessage, 1.18 - onChatMessage
    /*@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;filterText(Ljava/lang/String;Ljava/util/function/Consumer;)V"), method = "onChatMessage")
    public void onGameMessage_patch(ServerPlayNetworkHandler a, String s, Consumer con) {
        handleMessage(TextStream.Message.permitted(s));
    }*/


}
