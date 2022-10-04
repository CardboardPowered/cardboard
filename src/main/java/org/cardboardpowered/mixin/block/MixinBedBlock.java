package org.cardboardpowered.mixin.block;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.cardboardpowered.interfaces.IMixinBedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public class MixinBedBlock implements IMixinBedBlock {

    @Inject(method = "onUse", at = @At(value = "INVOKE",target = "Lnet/minecraft/entity/player/PlayerEntity;trySleep(Lnet/minecraft/util/math/BlockPos;)Lcom/mojang/datafixers/util/Either;"))
    private void bukkitLogic(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        BlockState finaliblockdata = state;
        BlockPos finalblockposition = pos;
        if (!world.getDimension().isBedWorking()) {
            explodeBed(finaliblockdata, world, finalblockposition);
        }
    }

    @Override
    public ActionResult explodeBed(BlockState iblockdata, World world, BlockPos blockposition) {
        {
            {
                world.removeBlock(blockposition, false);
                BlockPos blockposition1 = blockposition.offset(((Direction) iblockdata.get(BedBlock.FACING)).getOpposite());

                if (world.getBlockState(blockposition1).getBlock() == ((BedBlock) (Object) this)) {
                    world.removeBlock(blockposition1, false);
                }

                world.createExplosion((Entity) null, DamageSource.badRespawnPoint(), (ExplosionBehavior) null, (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D, 5.0F, true, Explosion.DestructionType.DESTROY);
                return ActionResult.SUCCESS;
            }
        }
    }
}
