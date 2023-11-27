package org.cardboardpowered.mixin.item;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.cardboardpowered.impl.world.FakeWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public class MixinBucketItem extends Item {

    public MixinBucketItem(Settings settings) {
        super(settings);
    }

    @Shadow
    public Fluid fluid;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FluidDrainable;tryDrainFluid(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/item/ItemStack;"))
    public void use_BF(World world, PlayerEntity player, Hand enumhand, CallbackInfoReturnable<TypedActionResult> ci) {
        BlockHitResult movingobjectpositionblock = raycast(world, player, this.fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.NONE : RaycastContext.FluidHandling.ANY);
        BlockHitResult movingobjectpositionblock1 = (BlockHitResult) movingobjectpositionblock;
        BlockPos blockposition = movingobjectpositionblock1.getBlockPos();
        BlockState iblockdata = world.getBlockState(blockposition);

        if (iblockdata.getBlock() instanceof FluidDrainable) {
            ItemStack dummyFluid = ((FluidDrainable) iblockdata.getBlock()).tryDrainFluid(player, FakeWorldAccess.INSTANCE, blockposition, iblockdata);
            PlayerBucketFillEvent event = BukkitEventFactory.callPlayerBucketFillEvent((ServerWorld) world, player, blockposition, blockposition, movingobjectpositionblock.getSide(), player.getStackInHand(enumhand), dummyFluid.getItem(), enumhand); // Paper - add enumhand
    
            if (event.isCancelled()) {
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new BlockUpdateS2CPacket(world, blockposition)); // SPIGOT-5163 (see PlayerInteractManager)
                ((Player)((IMixinServerEntityPlayer) player).getBukkitEntity()).updateInventory(); // SPIGOT-4541
                ci.setReturnValue(new TypedActionResult(ActionResult.FAIL, player.getStackInHand(enumhand)));
                return;
            }
        }
    }

    @Inject(method = "placeFluid", at = @At("HEAD"), cancellable = true)
    public void placeFluid_BF(PlayerEntity player, World world, BlockPos blockposition, BlockHitResult movingobjectpositionblock, CallbackInfoReturnable<Boolean> ci) {
        if (this.fluid instanceof FlowableFluid) {
            BlockState iblockdata = world.getBlockState(blockposition);
            Block block = iblockdata.getBlock();
            boolean flag = iblockdata.canBucketPlace(this.fluid);
            boolean flag1 = iblockdata.isAir() || flag || block instanceof FluidFillable && ((FluidFillable) block).canFillWithFluid(player, world, blockposition, iblockdata, this.fluid);
    
            // CraftBukkit start
            if (flag1 && player != null) {
                PlayerBucketEmptyEvent event = BukkitEventFactory.callPlayerBucketEmptyEvent(world, player, blockposition, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getSide(), player.getStackInHand(player.getActiveHand()), player.getActiveHand());
                if (event.isCancelled()) {
                    ((ServerPlayerEntity) player).networkHandler.sendPacket(new BlockUpdateS2CPacket(world, blockposition));
                    ((Player)((IMixinEntity) player).getBukkitEntity()).updateInventory();
                    ci.setReturnValue(false);
                    return;
                }
            }
        }
    }

}
