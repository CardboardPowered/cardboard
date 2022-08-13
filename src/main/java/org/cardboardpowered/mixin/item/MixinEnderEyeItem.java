package org.cardboardpowered.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
// import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.util.hit.HitResult;

@Mixin(EnderEyeItem.class)
public class MixinEnderEyeItem extends Item {

    public MixinEnderEyeItem(Settings settings) {
        super(settings);
    }

    /**
     * @reason .
     * @author .
     */
    /*@Overwrite
    public TypedActionResult<ItemStack> use(World world, PlayerEntity entityhuman, Hand enumhand) {
        ItemStack itemstack = entityhuman.getStackInHand(enumhand);
        BlockHitResult movingobjectpositionblock = raycast(world, entityhuman, RaycastContext.FluidHandling.NONE);

        if (movingobjectpositionblock.getType() == HitResult.Type.BLOCK && world.getBlockState(((BlockHitResult) movingobjectpositionblock).getBlockPos()).isOf(Blocks.END_PORTAL_FRAME)) {
            return TypedActionResult.pass(itemstack);
        } else {
            entityhuman.setCurrentHand(enumhand);
            if (world instanceof ServerWorld) {
                BlockPos blockposition = ((ServerWorld) world).getChunkManager().getChunkGenerator().locateStructure((ServerWorld) world, StructureFeature.STRONGHOLD, entityhuman.getBlockPos(), 100, false);

                if (blockposition != null) {
                    EyeOfEnderEntity entityendersignal = new EyeOfEnderEntity(world, entityhuman.getX(), entityhuman.getBodyY(0.5D), entityhuman.getZ());

                    entityendersignal.setItem(itemstack);
                    entityendersignal.initTargetPos(blockposition);

                    if (!world.spawnEntity(entityendersignal)) return new TypedActionResult<ItemStack>(ActionResult.FAIL, itemstack); // Bukkit

                    if (entityhuman instanceof ServerPlayerEntity)
                        Criteria.USED_ENDER_EYE.trigger((ServerPlayerEntity) entityhuman, blockposition);

                    world.playSound((PlayerEntity) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.NEUTRAL, 0.5F, 0.4F / (EnderEyeItem.RANDOM.nextFloat() * 0.4F + 0.8F));
                    world.syncWorldEvent((PlayerEntity) null, 1003, entityhuman.getBlockPos(), 0);
                    if (!entityhuman.abilities.creativeMode) itemstack.decrement(1);

                    entityhuman.incrementStat(Stats.USED.getOrCreateStat((EnderEyeItem)(Object)this));
                    entityhuman.swingHand(enumhand, true);
                    return TypedActionResult.success(itemstack);
                }
            }
            return TypedActionResult.consume(itemstack);
        }
    }*/

}
