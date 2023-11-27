package org.cardboardpowered.mixin.network.handler;

import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayNetworkHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.mojang.brigadier.ParseResults;
import net.minecraft.command.argument.SignedArgumentList;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageChain.MessageChainException;
import net.minecraft.network.message.MessageChainTaskQueue;
import net.minecraft.network.message.SignedCommandArguments;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.cardboardpowered.CardboardConfig;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.util.LazyPlayerSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.Map;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 800)
public abstract class MixinSPNH_PlayerCommandPreprocessEvent_1_19 implements IMixinPlayNetworkHandler {

	@Shadow
	public ServerPlayerEntity player;

	/**
	 * @reason PlayerCommandPreprocessEvent
	 * @author Cardboard 1.19.4
	 */
	@SuppressWarnings("unused")
	@Overwrite
	private void handleCommandExecution(CommandExecutionC2SPacket packet, LastSeenMessageList lastseenmessages) {
		SignedMessage playerchatmessage;
		String command = "/" + packet.command();
		BukkitFabricMod.LOGGER.info(this.player.getEntityName() + " issued server command: " + command);
		PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(this.getPlayer(), command, new LazyPlayerSet(CraftServer.server));
		CraftServer.INSTANCE.getPluginManager().callEvent(event);

		if(event.isCancelled()) {
			return;
		}

		command = event.getMessage().substring(1);

		if(CardboardConfig.REGISTRY_COMMAND_FIX) {
			Bukkit.dispatchCommand(getPlayer(), command);
			return;
		}

		ParseResults<ServerCommandSource> parseresults = this.parse(packet.command());
		Map<String, SignedMessage> map;
		try {
			map = (packet.command().equals(command)) ? this.collectArgumentMessages(packet, SignedArgumentList.of(parseresults), lastseenmessages) : Collections.emptyMap(); // CraftBukkit
		} catch(MessageChain.MessageChainException e) {
			this.handleMessageChainException(e);
			return;
		}

		SignedCommandArguments.Impl arguments = new SignedCommandArguments.Impl(map);

		parseresults = CommandManager.withCommandSource(parseresults, (stack) ->
				stack.withSignedArguments(arguments, messageChainTaskQueue));
		CraftServer.server.getCommandManager().execute(parseresults, command);
	}

	@Shadow
	public void handleMessageChainException(MessageChain.MessageChainException e) {
	}

	//  private Map<String, SignedMessage> collectArgumentMessages(CommandExecutionC2SPacket packet, DecoratableArgumentList<?> arguments) {
	@Shadow
	private Map<String, SignedMessage> collectArgumentMessages(CommandExecutionC2SPacket packet, SignedArgumentList<?> a, LastSeenMessageList b) throws MessageChainException {
		return null; // Shadow method
	}

	@Shadow
	private ParseResults<ServerCommandSource> parse(String command) {
		return null; // Shadow method
	}

	@Shadow @Final private MessageChainTaskQueue messageChainTaskQueue;
	public PlayerImpl getPlayer() {
		return (PlayerImpl) ((IMixinServerEntityPlayer) (Object) this.player).getBukkitEntity();
	}

}
