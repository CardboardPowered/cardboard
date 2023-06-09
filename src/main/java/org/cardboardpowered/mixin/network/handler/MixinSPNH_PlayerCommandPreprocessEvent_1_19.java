package org.cardboardpowered.mixin.network.handler;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.util.LazyPlayerSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayNetworkHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.mojang.brigadier.ParseResults;

import net.minecraft.command.argument.DecoratableArgumentList;
import net.minecraft.network.message.SignedCommandArguments;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 800)
public abstract class MixinSPNH_PlayerCommandPreprocessEvent_1_19 implements IMixinPlayNetworkHandler {

    @Shadow 
    public ServerPlayerEntity player;

    /**
     * @reason PlayerCommandPreprocessEvent
     * @author Cardboard 1.19.2+
     */
    @SuppressWarnings("unused")
	@Overwrite
    private void handleCommandExecution(CommandExecutionC2SPacket packet) {
        SignedMessage playerchatmessage;
        Object command = "/" + packet.command();
        BukkitFabricMod.LOGGER.info(this.player.getEntityName() + " issued server command: " + (String)command);
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(this.getPlayer(), (String)command, new LazyPlayerSet(CraftServer.server));
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        command = event.getMessage().substring(1);
        ParseResults<ServerCommandSource> parseresults = this.parse(packet.command());
        Map<String, SignedMessage> map = this.collectArgumentMessages(packet, DecoratableArgumentList.of(parseresults));
        if (event.isCancelled() || !packet.command().equals(command)) {
            for (SignedMessage message : map.values()) {
                this.player.server.getPlayerManager().sendMessageHeader(message, Set.of());
            }
            if (event.isCancelled()) {
                return;
            }
            map.clear();
            parseresults = this.parse((String)command);
        }
        Iterator<SignedMessage> iterator = map.values().iterator();
        do {
            if (iterator.hasNext()) continue;
            SignedCommandArguments.Impl commandsigningcontext_a = new SignedCommandArguments.Impl(map);
            parseresults = CommandManager.withCommandSource(parseresults, commandlistenerwrapper -> commandlistenerwrapper.withSignedArguments(commandsigningcontext_a));
            CraftServer.server.getCommandManager().execute(parseresults, (String)command);
            return;
        } while (this.canAcceptMessage(playerchatmessage = iterator.next()));
    }
    
    @Shadow
    private boolean canAcceptMessage(SignedMessage message) {
    	return true;
    }
    
    @Shadow
    private Map<String, SignedMessage> collectArgumentMessages(CommandExecutionC2SPacket packet, DecoratableArgumentList<?> arguments) {
    	return null; // Shadow method
    }
    
    @Shadow
    private ParseResults<ServerCommandSource> parse(String command) {
    	return null; // Shadow method
    }

    public PlayerImpl getPlayer() {
        return (PlayerImpl) ((IMixinServerEntityPlayer)(Object)this.player).getBukkitEntity();
    }

}