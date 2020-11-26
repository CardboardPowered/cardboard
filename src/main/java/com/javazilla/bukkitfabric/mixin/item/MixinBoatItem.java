package com.javazilla.bukkitfabric.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

@Mixin(BoatItem.class)
public class MixinBoatItem extends Item {

    public MixinBoatItem(Settings settings) {
        super(settings);
    }

    @SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
    @Inject(method = "use", at = @At(value = "NEW", target = "(Lnet/minecraft/world/World;DDD)Lnet/minecraft/entity/vehicle/BoatEntity;"), cancellable = true)
    public void bukkitize(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult> ci) {
        ItemStack itemstack = player.getStackInHand(hand);
        BlockHitResult movingobjectpositionblock = raycast(world, player, RaycastContext.FluidHandling.ANY);
        org.bukkit.event.player.PlayerInteractEvent event = BukkitEventFactory.callPlayerInteractEvent((ServerPlayerEntity) player, org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getSide(), itemstack, hand);

        if (event.isCancelled()) {
            ci.setReturnValue(new TypedActionResult(ActionResult.PASS, itemstack));
        }
    }

}