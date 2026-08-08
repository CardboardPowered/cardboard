/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2025 Cardboard contributors
 */
package org.cardboardpowered.mixin.core.dispenser;

import org.cardboardpowered.bridge.world.level.LevelBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BoatDispenseItemBehavior.class)
public class BoatDispenseItemBehaviorMixin {

    // @Shadow
    // public ItemDispenserBehavior itemDispenser;

    //@Shadow
    // public BoatEntity.Type boatType;

    @Shadow
    private EntityType<? extends AbstractBoat> type;

    public ItemStack dispenseSilently(BlockSource isourceblock, ItemStack itemstack) {
        Direction enumdirection = (Direction) isourceblock.state().getValue(DispenserBlock.FACING);
        ServerLevel worldserver = isourceblock.level();
        double d0 = isourceblock.pos().getX() + (double) ((float) enumdirection.getStepX() * 1.125F);
        double d1 = isourceblock.pos().getY() + (double) ((float) enumdirection.getStepY() * 1.125F);
        double d2 = isourceblock.pos().getZ() + (double) ((float) enumdirection.getStepZ() * 1.125F);
        BlockPos blockposition = isourceblock.pos().relative(enumdirection);
        double d3;

        // FIXME: 1.18.2
        //if (worldserver.getFluidState(blockposition).isIn((Tag<Fluid>) FluidTags.WATER)) {
            d3 = 1.0D;
        //} else {
        //    if (!worldserver.getBlockState(blockposition).isAir() || !worldserver.getFluidState(blockposition.down()).isIn((Tag<Fluid>) FluidTags.WATER))
        //        return this.itemDispenser.dispense(isourceblock, itemstack);
        //    d3 = 0.0D;
       // }

        ItemStack itemstack1 = itemstack.split(1);
        org.bukkit.block.Block block = ((LevelBridge)worldserver).cardboard$getWorld().getBlockAt(isourceblock.pos().getX(), isourceblock.pos().getY(), isourceblock.pos().getZ());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);

        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(d0, d1 + d3, d2));
        // TODO if (!DispenserBlock.eventFired)
            CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            itemstack.grow(1);
            return itemstack;
        }

        if (!event.getItem().equals(craftItem)) {
            itemstack.grow(1);
            // Chain to handler for new item
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            DispenseItemBehavior idispensebehavior = (DispenseItemBehavior) DispenserBlock.DISPENSER_REGISTRY.get(eventStack.getItem());
            if (idispensebehavior != DispenseItemBehavior.NOOP && idispensebehavior != this) {
                idispensebehavior.dispense(isourceblock, eventStack);
                return itemstack;
            }
        }

        // BoatEntity entityboat = new BoatEntity(worldserver, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ());

        AbstractBoat entityboat = this.type.create(worldserver, EntitySpawnReason.DISPENSER);
        
        if (null != entityboat) {
        	entityboat.setInitialPos(event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ());
        	// 26.2: PostSpawnProcessor is typed to Entity
        	// 26.2: PostSpawnProcessor#accept was renamed to apply
        	EntityType.createDefaultStackConfig(worldserver, itemstack, null).apply((net.minecraft.world.entity.Entity) entityboat);
        	entityboat.setYRot(enumdirection.toYRot());
        }

        
        // entityboat.setVariant(this.boatType);
        if (!worldserver.addFreshEntity(entityboat)) itemstack.grow(1); // CraftBukkit
        return itemstack;
    }

}
