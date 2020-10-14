package com.javazilla.bukkitfabric.mixin.item;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

@Mixin(EndCrystalItem.class)
public class MixinEndCrystalItem {

    @SuppressWarnings("deprecation")
    @Overwrite
    public ActionResult useOnBlock(ItemUsageContext itemactioncontext) {
        World world = itemactioncontext.getWorld();
        BlockPos blockpos = itemactioncontext.getBlockPos();
        BlockState iblockdata = world.getBlockState(blockpos);

        if (!iblockdata.isOf(Blocks.BEDROCK) && !iblockdata.isOf(Blocks.OBSIDIAN)) return ActionResult.FAIL;

        BlockPos blockpos1 = blockpos.up();
        if (!world.isAir(blockpos1)) return ActionResult.FAIL;
        double x = (double) blockpos1.getX();
        double y = (double) blockpos1.getY();
        double z = (double) blockpos1.getZ();
        List<Entity> list = world.getOtherEntities((Entity) null, new Box(x, y, z, x + 1.0D, y + 2.0D, z + 1.0D));

        if (!list.isEmpty())
        return ActionResult.FAIL;

        if (world instanceof ServerWorld) {
            EndCrystalEntity entityendercrystal = new EndCrystalEntity(world, x + 0.5D, y, z + 0.5D);

            entityendercrystal.setShowBottom(false);
            if (BukkitEventFactory.callEntityPlaceEvent(itemactioncontext, entityendercrystal).isCancelled()) return ActionResult.FAIL;
            world.spawnEntity(entityendercrystal);
            EnderDragonFight enderdragonbattle = ((ServerWorld) world).getEnderDragonFight();
            if (enderdragonbattle != null) enderdragonbattle.respawnDragon();
        }
        itemactioncontext.getStack().decrement(1);
        return ActionResult.success(world.isClient);
    }

}