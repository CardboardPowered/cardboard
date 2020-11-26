package com.javazilla.bukkitfabric.mixin.item;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.cardboardpowered.impl.world.FakeWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

@Mixin(BucketItem.class)
public class MixinBucketItem extends Item {

    public MixinBucketItem(Settings settings) {
        super(settings);
    }

    @Shadow
    public Fluid fluid;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FluidDrainable;tryDrainFluid(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/fluid/Fluid;"))
    public void use_BF(World world, PlayerEntity entityhuman, Hand enumhand, CallbackInfoReturnable<TypedActionResult> ci) {
        BlockHitResult movingobjectpositionblock = raycast(world, entityhuman, this.fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.NONE : RaycastContext.FluidHandling.ANY);
        BlockHitResult movingobjectpositionblock1 = (BlockHitResult) movingobjectpositionblock;
        BlockPos blockposition = movingobjectpositionblock1.getBlockPos();
        BlockState iblockdata = world.getBlockState(blockposition);

        if (iblockdata.getBlock() instanceof FluidDrainable) {
            Fluid dummyFluid = ((FluidDrainable) iblockdata.getBlock()).tryDrainFluid(FakeWorldAccess.INSTANCE, blockposition, iblockdata);
            PlayerBucketFillEvent event = BukkitEventFactory.callPlayerBucketFillEvent((ServerWorld) world, entityhuman, blockposition, blockposition, movingobjectpositionblock.getSide(), entityhuman.getStackInHand(enumhand), dummyFluid.getBucketItem(), enumhand); // Paper - add enumhand
    
            if (event.isCancelled()) {
                ((ServerPlayerEntity) entityhuman).networkHandler.sendPacket(new BlockUpdateS2CPacket(world, blockposition)); // SPIGOT-5163 (see PlayerInteractManager)
                ((Player)((IMixinServerEntityPlayer) entityhuman).getBukkitEntity()).updateInventory(); // SPIGOT-4541
                ci.setReturnValue(new TypedActionResult(ActionResult.FAIL, entityhuman.getStackInHand(enumhand)));
                return;
            }
        }
    }

    @Inject(method = "placeFluid", at = @At("HEAD"), cancellable = true)
    public void placeFluid_BF(PlayerEntity entityhuman, World world, BlockPos blockposition, BlockHitResult movingobjectpositionblock, CallbackInfoReturnable<Boolean> ci) {
        if (this.fluid instanceof FlowableFluid) {
            BlockState iblockdata = world.getBlockState(blockposition);
            Block block = iblockdata.getBlock();
            boolean flag = iblockdata.canBucketPlace(this.fluid);
            boolean flag1 = iblockdata.isAir() || flag || block instanceof FluidFillable && ((FluidFillable) block).canFillWithFluid(world, blockposition, iblockdata, this.fluid);
    
            // CraftBukkit start
            if (flag1 && entityhuman != null) {
                PlayerBucketEmptyEvent event = BukkitEventFactory.callPlayerBucketEmptyEvent((ServerWorld) world, entityhuman, blockposition, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getSide(), entityhuman.getStackInHand(entityhuman.getActiveHand()), entityhuman.getActiveHand());
                if (event.isCancelled()) {
                    ((ServerPlayerEntity) entityhuman).networkHandler.sendPacket(new BlockUpdateS2CPacket(world, blockposition));
                    ((Player)((IMixinEntity)((ServerPlayerEntity) entityhuman)).getBukkitEntity()).updateInventory();
                    ci.setReturnValue(false);
                    return;
                }
            }
        }
    }

}
