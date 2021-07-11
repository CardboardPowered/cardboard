/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
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
package org.cardboardpowered.mixin.entity.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.dispenser.BoatDispenserBehavior;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;

@Mixin(BoatDispenserBehavior.class)
public class MixinBoatDispenserBehavior {

    @Shadow
    public ItemDispenserBehavior itemDispenser;

    @Shadow
    public BoatEntity.Type boatType;

    public ItemStack dispenseSilently(BlockPointer isourceblock, ItemStack itemstack) {
        Direction enumdirection = (Direction) isourceblock.getBlockState().get(DispenserBlock.FACING);
        ServerWorld worldserver = isourceblock.getWorld();
        double d0 = isourceblock.getX() + (double) ((float) enumdirection.getOffsetX() * 1.125F);
        double d1 = isourceblock.getY() + (double) ((float) enumdirection.getOffsetY() * 1.125F);
        double d2 = isourceblock.getZ() + (double) ((float) enumdirection.getOffsetZ() * 1.125F);
        BlockPos blockposition = isourceblock.getPos().offset(enumdirection);
        double d3;

        if (worldserver.getFluidState(blockposition).isIn((Tag<Fluid>) FluidTags.WATER)) {
            d3 = 1.0D;
        } else {
            if (!worldserver.getBlockState(blockposition).isAir() || !worldserver.getFluidState(blockposition.down()).isIn((Tag<Fluid>) FluidTags.WATER))
                return this.itemDispenser.dispense(isourceblock, itemstack);
            d3 = 0.0D;
        }

        ItemStack itemstack1 = itemstack.split(1);
        org.bukkit.block.Block block = ((IMixinWorld)worldserver).getWorldImpl().getBlockAt(isourceblock.getPos().getX(), isourceblock.getPos().getY(), isourceblock.getPos().getZ());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);

        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(d0, d1 + d3, d2));
        // TODO if (!DispenserBlock.eventFired)
            CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            itemstack.increment(1);
            return itemstack;
        }

        if (!event.getItem().equals(craftItem)) {
            itemstack.increment(1);
            // Chain to handler for new item
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            DispenserBehavior idispensebehavior = (DispenserBehavior) DispenserBlock.BEHAVIORS.get(eventStack.getItem());
            if (idispensebehavior != DispenserBehavior.NOOP && idispensebehavior != this) {
                idispensebehavior.dispense(isourceblock, eventStack);
                return itemstack;
            }
        }

        BoatEntity entityboat = new BoatEntity(worldserver, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ());

        entityboat.setBoatType(this.boatType);
        entityboat.setYaw(enumdirection.asRotation());
        if (!worldserver.spawnEntity(entityboat)) itemstack.increment(1); // CraftBukkit
        return itemstack;
    }

}
