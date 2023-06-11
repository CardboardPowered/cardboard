/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.enchantment.FrostWalkerEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

/**
 */
@Mixin(value = FrostWalkerEnchantment.class, priority = 999)
public class MixinFrostWalkerEnchantment {

    /**
     * @reason BlockFormEvent - Add call to {@link BukkitEventFactory#handleBlockFormEvent}
     * @author .
     * 
     * @param entity - The entity/player
     * @param world  - the world the entity is in.
     * @param pos    - The current {@link BlockPos}
     */
    @Overwrite
    public static void freezeWater(LivingEntity living, World worldIn, BlockPos pos, int level) {
        if (living.isOnGround()) {
            BlockState blockstate = Blocks.FROSTED_ICE.getDefaultState();
            int f = Math.min(16, 2 + level);
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

            for (BlockPos blockpos : BlockPos.iterate(pos.add(-f, -1, -f), pos.add(f, -1, f))) {
                if (blockpos.isWithinDistance(living.getPos(), f)) {
                    blockpos$mutable.set(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
                    BlockState blockstate1 = worldIn.getBlockState(blockpos$mutable);
                    if (blockstate1.isAir()) {
                        BlockState blockstate2 = worldIn.getBlockState(blockpos);
                        boolean isFull = blockstate2.getBlock() == Blocks.WATER && blockstate2.get(FluidBlock.LEVEL) == 0;
                        if (blockstate2.getMaterial() == Material.WATER && isFull && blockstate.canPlaceAt(worldIn, blockpos) && worldIn.canPlace(blockstate, blockpos, ShapeContext.absent())) {
                            if (BukkitEventFactory.handleBlockFormEvent(worldIn, blockpos, blockstate, living)) {
                                worldIn.scheduleBlockTick(blockpos, Blocks.FROSTED_ICE, MathHelper.nextInt(living.getRandom(), 60, 120));
                            }
                        }
                    }
                }
            }
        }
    }

}