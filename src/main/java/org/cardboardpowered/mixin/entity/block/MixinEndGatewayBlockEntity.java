package org.cardboardpowered.mixin.entity.block;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayNetworkHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndGatewayBlockEntity.class)
public class MixinEndGatewayBlockEntity {

	// I tried using LocalCapture but it kept making up
	// that i was using ServerWorld which crashed the game
	// So this messy solution will have to do

	@Shadow
	private static void startTeleportCooldown(World world, BlockPos pos, BlockState state, EndGatewayBlockEntity be) {
	}


	@Redirect(method = "tryTeleportingEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;resetPortalCooldown()V"))
	private static void onResetPortalCooldown(Entity instance) {
		// ignore, called somewhere else here
	}

	@Unique private static boolean wasCancelled;

	@Redirect(method = "tryTeleportingEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/EndGatewayBlockEntity;startTeleportCooldown(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/EndGatewayBlockEntity;)V"))
	private static void onStartTeleportCooldown(World world, BlockPos pos, BlockState state, EndGatewayBlockEntity blockEntity) {
		if(wasCancelled) {
			wasCancelled = false;
		} else {
			startTeleportCooldown(world, pos, state, blockEntity);
		}
	}

	@Redirect(method = "tryTeleportingEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;teleport(DDD)V"))
	private static void bukkitize(Entity target, double x, double y, double z) {
		if(!(target instanceof ServerPlayerEntity player)) {
			target.resetPortalCooldown();
			target.teleport(x, y, z);
			return;
		}

		Location loc = callEvent((IMixinWorld) player.getWorld(), (IMixinEntity) player, x, y, z);

		if(loc == null) {
			wasCancelled = true;
			return;
		}

		target.resetPortalCooldown();
		((IMixinPlayNetworkHandler) player.networkHandler).teleport(loc);
	}

	@Unique
	private static Location callEvent(IMixinWorld world, IMixinEntity teleported, double x, double y, double z) {
		PlayerImpl player = (PlayerImpl) teleported.getBukkitEntity();
		Location location = new Location(world.getWorldImpl(), x, y, z);
		location.setPitch(player.getLocation().getPitch());
		location.setYaw(player.getLocation().getYaw());

		PlayerTeleportEvent teleEvent = new PlayerTeleportEvent(player, player.getLocation(), location, PlayerTeleportEvent.TeleportCause.END_GATEWAY);
		return teleEvent.isCancelled() ? null : teleEvent.getTo();
	}

}
