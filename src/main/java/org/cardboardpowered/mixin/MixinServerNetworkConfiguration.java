package org.cardboardpowered.mixin;


import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.cardboardpowered.interfaces.INetworkConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerConfigurationNetworkHandler.class)
public class MixinServerNetworkConfiguration implements INetworkConfiguration {
	@Unique private ServerPlayerEntity replacementPlayer;

	@Override
	public void cardboard_setPlayer(ServerPlayerEntity entity) {
		this.replacementPlayer = entity;
	}

	@Redirect(method = "onReady", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;createPlayer(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/network/packet/c2s/common/SyncedClientOptions;)Lnet/minecraft/server/network/ServerPlayerEntity;"))
	private ServerPlayerEntity replacePlayer(PlayerManager manager, GameProfile profile, SyncedClientOptions syncedOptions) {
		if(replacementPlayer != null) {
			replacementPlayer.setClientOptions(syncedOptions);
			return replacementPlayer;
		}

		return manager.createPlayer(profile, syncedOptions);
	}
}
