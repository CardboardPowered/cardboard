package org.cardboardpowered.mixin.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public class BucketItemMixin extends Item {

    public BucketItemMixin(net.minecraft.world.item.Item.Properties settings) {
        super(settings);
    }

    @Shadow
    public Fluid content;

    // TODO
    /*
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FluidDrainable;tryDrainFluid(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/item/ItemStack;"))
    public void use_BF(World world, PlayerEntity player, Hand enumhand, CallbackInfoReturnable<ActionResultHolder<ItemStack>> ci) {
        BlockHitResult movingobjectpositionblock = raycast(world, player, this.fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.NONE : RaycastContext.FluidHandling.ANY);
        BlockHitResult movingobjectpositionblock1 = (BlockHitResult) movingobjectpositionblock;
        BlockPos blockposition = movingobjectpositionblock1.getBlockPos();
        BlockState iblockdata = world.getBlockState(blockposition);

        if (iblockdata.getBlock() instanceof FluidDrainable) {
            ItemStack dummyFluid = ((FluidDrainable) iblockdata.getBlock()).tryDrainFluid(player, FakeWorldAccess.INSTANCE, blockposition, iblockdata);
            PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent((ServerWorld) world, player, blockposition, blockposition, movingobjectpositionblock.getSide(), player.getStackInHand(enumhand), dummyFluid.getItem(), enumhand); // Paper - add enumhand
    
            if (event.isCancelled()) {
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new BlockUpdateS2CPacket(world, blockposition)); // SPIGOT-5163 (see PlayerInteractManager)
                ((Player)((IMixinServerEntityPlayer) player).getBukkitEntity()).updateInventory(); // SPIGOT-4541
                // ci.setReturnValue(new TypedActionResult(ActionResult.FAIL, player.getStackInHand(enumhand)));
                ci.setReturnValue(ActionResult.FAIL);
                return;
            }
        }
    }
    */

    @Inject(method = "emptyContents", at = @At("HEAD"), cancellable = true)
    public void placeFluid_BF(LivingEntity player, Level world, BlockPos blockposition, BlockHitResult movingobjectpositionblock, CallbackInfoReturnable<Boolean> ci) {
        // Singleplayer client prediction: vanilla only; the server fires PlayerBucketEmptyEvent.
        if (world.isClientSide()) return;
        if (this.content instanceof FlowingFluid) {
            BlockState iblockdata = world.getBlockState(blockposition);
            Block block = iblockdata.getBlock();
            boolean flag = iblockdata.canBeReplaced(this.content);
            boolean flag1 = iblockdata.isAir() || flag || block instanceof LiquidBlockContainer && ((LiquidBlockContainer) block).canPlaceLiquid(player, world, blockposition, iblockdata, this.content);
    
            // CraftBukkit start
            if (flag1 && player != null) {
            	if (player instanceof net.minecraft.world.entity.player.Player) {
	                PlayerBucketEmptyEvent event = CraftEventFactory.callPlayerBucketEmptyEvent(world, (net.minecraft.world.entity.player.Player) player, blockposition, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getDirection(), player.getItemInHand(player.getUsedItemHand()), player.getUsedItemHand());
	                if (event.isCancelled()) {
	                    ((ServerPlayer) player).connection.send(new ClientboundBlockUpdatePacket(world, blockposition));
	                    ((Player)((EntityBridge) player).getBukkitEntity()).updateInventory();
	                    ci.setReturnValue(false);
	                    return;
	                }
            	}
            }
        }
    }

}
