package com.javazilla.bukkitfabric.mixin.entity.block;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayNetworkHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

@Mixin(EndGatewayBlockEntity.class)
public class MixinEndGatewayBlockEntity extends EndPortalBlockEntity {

    @Shadow public BlockPos exitPortalPos;
    @Shadow public boolean exactTeleport;

    @Shadow
    public BlockPos findBestPortalExitPos() {
        return null;
    }

    @Shadow
    public void startTeleportCooldown() {
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;teleport(DDD)V"), method = "tryTeleportingEntity", cancellable = true)
    public void bukkitize(Entity entity, CallbackInfo ci) {
        BlockPos blockposition = this.exactTeleport ? this.exitPortalPos : this.findBestPortalExitPos();
        Entity entity1;

        if (entity instanceof EnderPearlEntity) {
            Entity entity2 = ((EnderPearlEntity) entity).getOwner();
            if (entity2 instanceof ServerPlayerEntity) Criteria.ENTER_BLOCK.trigger((ServerPlayerEntity) entity2, this.world.getBlockState(this.getPos()));

            if (entity2 != null) {
                entity1 = entity2;
                entity.remove();
            } else entity1 = entity;
        } else entity1 = entity.getRootVehicle();

        if (entity1 instanceof ServerPlayerEntity) {
            PlayerImpl player = (PlayerImpl) ((IMixinEntity)entity1).getBukkitEntity();
            org.bukkit.Location location = new Location(((IMixinWorld)world).getWorldImpl(), (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D);
            location.setPitch(player.getLocation().getPitch());
            location.setYaw(player.getLocation().getYaw());

            PlayerTeleportEvent teleEvent = new PlayerTeleportEvent(player, player.getLocation(), location, PlayerTeleportEvent.TeleportCause.END_GATEWAY);
            Bukkit.getPluginManager().callEvent(teleEvent);
            if (teleEvent.isCancelled()) {
                ci.cancel();
                return;
            }

            entity1.resetNetherPortalCooldown();
            ((IMixinPlayNetworkHandler) ((ServerPlayerEntity) entity1).networkHandler).teleport(teleEvent.getTo());
            this.startTeleportCooldown();
            ci.cancel();
            return;
        }
    }

}
