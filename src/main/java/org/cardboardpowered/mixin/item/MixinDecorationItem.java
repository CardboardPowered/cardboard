package org.cardboardpowered.mixin.item;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.DecorationItem;
import net.minecraft.item.ItemUsageContext;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@MixinInfo(events = {"HangingPlaceEvent"})
@Mixin(value = DecorationItem.class, priority = 900)
public class MixinDecorationItem {

    @Inject(method = "useOnBlock", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/AbstractDecorationEntity;onPlace()V"))
    private void bukkitUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir, BlockPos blockPos, Direction direction, BlockPos blockPos2, PlayerEntity playerEntity, ItemStack itemStack, World world, AbstractDecorationEntity abstractDecorationEntity) {
        Player who = (context.getPlayer() == null) ? null : (Player) ((IMixinServerEntityPlayer) context.getPlayer()).getBukkit();
        org.bukkit.block.Block blockClicked = CraftBlock.at((ServerWorld) world, blockPos);
        org.bukkit.block.BlockFace blockFace = CraftBlock.notchToBlockFace(direction);
/*
        HangingPlaceEvent event = new HangingPlaceEvent((Hanging) ((IMixinEntity) abstractDecorationEntity).getBukkitEntity(), who, blockClicked, blockFace, CraftItemStack.asBukkitCopy(itemStack));
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            cir.setReturnValue(ActionResult.FAIL);
        }
*/
    }

}
