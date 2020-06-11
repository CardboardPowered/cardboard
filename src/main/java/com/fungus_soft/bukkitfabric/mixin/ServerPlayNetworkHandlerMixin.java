package com.fungus_soft.bukkitfabric.mixin;

import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
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

import com.fungus_soft.bukkitfabric.BukkitLogger;
import com.fungus_soft.bukkitfabric.interfaces.IMixinBukkitGetter;
import com.fungus_soft.bukkitfabric.interfaces.IMixinMinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.options.ChatVisibility;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import com.fungus_soft.bukkitfabric.interfaces.IMixinPlayNetworkHandler;

import static org.bukkit.craftbukkit.CraftServer.server;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements IMixinPlayNetworkHandler {

    @Shadow 
    public ServerPlayerEntity player;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

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
        return (CraftPlayer) ((IMixinBukkitGetter)(Object)this.player).getBukkitObject();
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
            Bukkit.getPluginManager().callEvent(event);

            if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
                // Evil plugins still listening to deprecated event
                final PlayerChatEvent queueEvent = new PlayerChatEvent(player, event.getMessage(), event.getFormat(), event.getRecipients());
                queueEvent.setCancelled(event.isCancelled());
                Waitable waitable = new WaitableImpl(()-> {
                    Bukkit.getPluginManager().callEvent(queueEvent);

                    if (queueEvent.isCancelled())
                        return;

                    String message = String.format(queueEvent.getFormat(), queueEvent.getPlayer().getDisplayName(), queueEvent.getMessage());
                    for (Text txt : CraftChatMessage.fromString(message))
                        CraftServer.server.sendMessage(txt);
                    if (((LazyPlayerSet) queueEvent.getRecipients()).isLazy()) {
                        for (ServerPlayerEntity plr : CraftServer.server.getPlayerManager().getPlayerList())
                            for (Text txt : CraftChatMessage.fromString(message))
                                plr.sendMessage(txt);
                    } else
                        for (Player plr : queueEvent.getRecipients())
                            plr.sendMessage(message);
                });

                if (async)
                    ((IMixinMinecraftServer)CraftServer.server).getProcessQueue().add(waitable);
                else
                    waitable.run();
                try {
                    waitable.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // This is proper habit for java. If we aren't handling it, pass it on!
                } catch (ExecutionException e) {
                    throw new RuntimeException("Exception processing chat event", e.getCause());
                }
            } else {
                if (event.isCancelled())
                    return;

                s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
                server.sendMessage(new LiteralText(s));
                if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
                    for (ServerPlayerEntity recipient : server.getPlayerManager().getPlayerList())
                        for (Text txt : CraftChatMessage.fromString(s))
                            recipient.sendMessage(txt);
                } else for (Player recipient : event.getRecipients())
                    recipient.sendMessage(s);
            }
        }
    }

    @Overwrite
    public void onChatMessage(ChatMessageC2SPacket packetplayinchat) {
        if (CraftServer.server.isStopped())
            return;

        boolean isSync = packetplayinchat.getChatMessage().startsWith("/");
        if (packetplayinchat.getChatMessage().startsWith("/"))
            NetworkThreadUtils.forceMainThread(packetplayinchat, ((ServerPlayNetworkHandler)(Object)this), this.player.getServerWorld());

        // CraftBukkit end
        if (this.player.removed || this.player.getClientChatVisibility() == ChatVisibility.HIDDEN) { // CraftBukkit - dead men tell no tales
            this.sendPacket(new ChatMessageS2CPacket((new TranslatableText("chat.cannotSend")).formatted(Formatting.RED)));
        } else {
            this.player.updateLastActionTime();
            String s = StringUtils.normalizeSpace( packetplayinchat.getChatMessage() );

            // CraftBukkit start
            if (isSync)
                this.executeCommand(s);
            else if (s.isEmpty())
                BukkitLogger.getLogger().warning(this.player.getEntityName() + " tried to send an empty message");
            else if (this.player.getClientChatVisibility() == ChatVisibility.SYSTEM) { // Re-add "Command Only" flag check
                TranslatableText chatmessage = new TranslatableText("chat.cannotSend", new Object[0]);

                chatmessage.getStyle().setColor(Formatting.RED);
                this.sendPacket(new ChatMessageS2CPacket(chatmessage));
            } else this.chat(s, true);


        }
    }

}