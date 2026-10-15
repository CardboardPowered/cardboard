package org.cardboardpowered.mixin.world.item;

import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@MixinInfo(events = {"HangingPlaceEvent"})
@Mixin(value = HangingEntityItem.class, priority = 900)
public class HangingEntityItemMixin {

    @Inject(method = "useOn", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/HangingEntity;playPlacementSound()V"))
    private void bukkitUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, BlockPos blockPos, Direction direction, BlockPos blockPos2, net.minecraft.world.entity.player.Player playerEntity, ItemStack itemStack, Level world, HangingEntity abstractDecorationEntity) {
        if (world.isClientSide()) return;
        Player who = (context.getPlayer() == null) ? null : (Player) ((ServerPlayerBridge) context.getPlayer()).getBukkitEntity();
        org.bukkit.block.Block blockClicked = CraftBlock.at((ServerLevel) world, blockPos);
        org.bukkit.block.BlockFace blockFace = CraftBlock.notchToBlockFace(direction);

        HangingPlaceEvent event = new HangingPlaceEvent((Hanging) ((EntityBridge) abstractDecorationEntity).getBukkitEntity(), who, blockClicked, blockFace, EquipmentSlot.HAND, CraftItemStack.asBukkitCopy(itemStack));
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

}