package com.javazilla.bukkitfabric.mixin.network;

import static org.bukkit.craftbukkit.CraftServer.server;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.LazyPlayerSet;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.craftbukkit.util.WaitableImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.BukkitLogger;
import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayNetworkHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.client.options.ChatVisibility;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("deprecation")
@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler implements IMixinPlayNetworkHandler {

    @Shadow 
    public ServerPlayerEntity player;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    private static AtomicInteger chatSpamField = new AtomicInteger();

    @Shadow
    public int teleportRequestTick;

    @Shadow
    public int ticks;

    @Shadow
    public Vec3d requestedTeleportPos;

    @Shadow
    public int requestedTeleportId;

    @Override
    public boolean isDisconnected() {
    	return false; // TODO
    }

    @Overwrite
    public void disconnect(String s) {
        String leaveMessage = Formatting.YELLOW + this.player.getEntityName() + " left the game.";

        PlayerKickEvent event = new PlayerKickEvent(CraftServer.INSTANCE.getPlayer(this.player), s, leaveMessage);

        if (CraftServer.INSTANCE.getServer().isRunning())
            CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        s = event.getReason();
        final Text ichatbasecomponent = CraftChatMessage.fromString(s, true)[0];

        get().connection.send(new DisconnectS2CPacket(ichatbasecomponent), (future) -> get().connection.disconnect(ichatbasecomponent));
        get().onDisconnected(ichatbasecomponent);
        get().connection.disableAutoRead();
        get().connection.getClass();
    }

    /**
     * @reason Bukkit
     * @author Bukkit4Fabric
     */
    @Overwrite
    public void executeCommand(String string) {
        BukkitLogger.getLogger().info(this.player.getName().getString() + " issued server command: " + string);
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(getPlayer(), string, new LazyPlayerSet(CraftServer.server));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        try {
            Bukkit.getServer().dispatchCommand(event.getPlayer(), event.getMessage().substring(1));
        } catch (org.bukkit.command.CommandException ex) {
            getPlayer().sendMessage(org.bukkit.ChatColor.RED + "An internal error occurred while attempting to perform this command");
            java.util.logging.Logger.getLogger(ServerPlayNetworkHandler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    public CraftPlayer getPlayer() {
        return (CraftPlayer) ((IMixinServerEntityPlayer)(Object)this.player).getBukkitEntity();
    }

    @Override
    public void chat(String s, boolean async) {
        if (s.isEmpty() || this.player.getClientChatVisibility() == ChatVisibility.HIDDEN)
            return;

        if (!async && s.startsWith("/")) {
            this.executeCommand(s);
        } else if (this.player.getClientChatVisibility() == ChatVisibility.SYSTEM) {
            // Do nothing, this is coming from a plugin
        } else {
            Player player = this.getPlayer();
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
    }

    /**
     * @reason Bukkit
     * @author Bukkit4Fabric
     */
    @Overwrite
    public void onGameMessage(ChatMessageC2SPacket packetplayinchat) {
        if (CraftServer.server.isStopped())
            return;

        boolean isSync = packetplayinchat.getChatMessage().startsWith("/");
        if (packetplayinchat.getChatMessage().startsWith("/"))
            NetworkThreadUtils.forceMainThread(packetplayinchat, ((ServerPlayNetworkHandler)(Object)this), this.player.getServerWorld());

        if (this.player.removed || this.player.getClientChatVisibility() == ChatVisibility.HIDDEN) {
            this.sendPacket(new GameMessageS2CPacket((new TranslatableText("chat.cannotSend")).formatted(Formatting.RED), MessageType.CHAT, player.getUuid()));
        } else {
            this.player.updateLastActionTime();
            String s = StringUtils.normalizeSpace( packetplayinchat.getChatMessage() );

            if (isSync)
                this.executeCommand(s);
            else if (s.isEmpty())
                BukkitLogger.getLogger().warning(this.player.getEntityName() + " tried to send an empty message");
            else if (this.player.getClientChatVisibility() == ChatVisibility.SYSTEM) {
                TranslatableText chatmessage = new TranslatableText("chat.cannotSend", new Object[0]);

                chatmessage.getStyle().withColor(Formatting.RED);
                this.sendPacket(new GameMessageS2CPacket(chatmessage, MessageType.CHAT, player.getUuid()));
            } else this.chat(s, true);

            if (chatSpamField.addAndGet(20) > 200 && !server.getPlayerManager().isOperator(this.player.getGameProfile())) {
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

    @Override
    public void teleport(Location location) {
        double d0 = location.getX();
        double d1 = location.getY();
        double d2 = location.getZ();
        float f = location.getYaw();
        float f1 = location.getPitch();
        Set<PlayerPositionLookS2CPacket.Flag> set = Collections.emptySet();

        if (Float.isNaN(f))
            f = 0;

        if (Float.isNaN(f1))
            f1 = 0;

        double d3 = set.contains(PlayerPositionLookS2CPacket.Flag.X) ? this.player.getX() : 0.0D;
        double d4 = set.contains(PlayerPositionLookS2CPacket.Flag.Y) ? this.player.getY() : 0.0D;
        double d5 = set.contains(PlayerPositionLookS2CPacket.Flag.Z) ? this.player.getZ() : 0.0D;
        float f2 = set.contains(PlayerPositionLookS2CPacket.Flag.Y_ROT) ? this.player.yaw : 0.0F;
        float f3 = set.contains(PlayerPositionLookS2CPacket.Flag.X_ROT) ? this.player.pitch : 0.0F;

        this.requestedTeleportPos = new Vec3d(d0, d1, d2);
        if (++this.requestedTeleportId == Integer.MAX_VALUE)
            this.requestedTeleportId = 0;

        this.teleportRequestTick = this.ticks;
        this.player.updatePositionAndAngles(d0, d1, d2, f, f1);
        this.player.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(d0 - d3, d1 - d4, d2 - d5, f - f2, f1 - f3, set, this.requestedTeleportId));
    }

    @Inject(at = @At("HEAD"), method = "onClientCommand", cancellable = true)
    public void onClientCommand(ClientCommandC2SPacket packetplayinentityaction, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packetplayinentityaction, get(), this.player.getServerWorld());
        if (this.player.removed) return;
        switch (packetplayinentityaction.getMode()) {
            case PRESS_SHIFT_KEY:
            case RELEASE_SHIFT_KEY:
                PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(this.getPlayer(), packetplayinentityaction.getMode() == ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY);
                CraftServer.INSTANCE.getPluginManager().callEvent(event);

                if (event.isCancelled())
                    ci.cancel();
                break;
            case START_SPRINTING:
            case STOP_SPRINTING:
                PlayerToggleSprintEvent e2 = new PlayerToggleSprintEvent(this.getPlayer(), packetplayinentityaction.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING);
                CraftServer.INSTANCE.getPluginManager().callEvent(e2);

                if (e2.isCancelled())
                    ci.cancel();
                break;
            default:
                break;
        }
    }

    private ServerPlayNetworkHandler get() {
        return (ServerPlayNetworkHandler) (Object) this;
    }

}