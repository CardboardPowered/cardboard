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
 * @implNote Mixin set to priority 999 to allow
 *          other mods to inject after us. 
 */
@Mixin(value = FrostWalkerEnchantment.class, priority = 999)
public class MixinFrostWalkerEnchantment {
    
    // TODO 1.18!!!

    /**
     * @reason BlockFormEvent - Add call to {@link BukkitEventFactory#handleBlockFormEvent}
     * @author .
     * 
     * @param entity - The entity/player
     * @param world  - the world the entity is in.
     * @param pos    - The current {@link BlockPos}
     */
    @Overwrite
    public static void freezeWater(LivingEntity entity, World world, BlockPos pos, int i) {
        if (entity.isOnGround()) {
            BlockState state = Blocks.FROSTED_ICE.getDefaultState();
            float f = (float) Math.min(16, 2 + i);
            BlockPos.Mutable mutablePos = new BlockPos.Mutable();
            Iterator<BlockPos> iterator = BlockPos.iterate(pos.add((double) (-f), -1.0D, (double) (-f)), pos.add((double) f, -1.0D, (double) f)).iterator();

            while (iterator.hasNext()) {
                BlockPos blockpos1 = (BlockPos) iterator.next();
                if (blockpos1.isWithinDistance((Position) entity.getPos(), (double) f)) {
                    mutablePos.set(blockpos1.getX(), blockpos1.getY() + 1, blockpos1.getZ());
                    BlockState state1 = world.getBlockState(mutablePos);

                    /*if (state1.isAir()) {
                        BlockState state2 = world.getBlockState(blockpos1);
                        if (state2.getMaterial() == Material.WATER && (Integer) state2.get(FluidBlock.LEVEL) == 0 && state.canPlaceAt(world, blockpos1) && world.canPlace(state, blockpos1, ShapeContext.absent()))
                            if (BukkitEventFactory.handleBlockFormEvent(world, blockpos1, state, entity))
                                world.getBlockTickScheduler().schedule(blockpos1, Blocks.FROSTED_ICE, MathHelper.nextInt(entity.getRandom(), 60, 120));
                    }*/
                }
            }
        }
    }

}