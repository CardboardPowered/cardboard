package com.javazilla.bukkitfabric.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.javazilla.bukkitfabric.impl.block.DispenserBlockHelper;
import com.javazilla.bukkitfabric.interfaces.IMixinDispenserBlock;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointerImpl;
import net.minecraft.util.math.BlockPos;

@Mixin(DispenserBlock.class)
public class MixinDispenserBlock implements IMixinDispenserBlock {

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public void dispense(ServerWorld worldserver, BlockPos blockposition) {
        BlockPointerImpl sourceblock = new BlockPointerImpl(worldserver, blockposition);
        DispenserBlockEntity tileentitydispenser = (DispenserBlockEntity) sourceblock.getBlockEntity();
        int i = tileentitydispenser.chooseNonEmptySlot();

        if (i < 0) {
            worldserver.syncWorldEvent(1001, blockposition, 0);
        } else {
            ItemStack itemstack = tileentitydispenser.getStack(i);
            DispenserBehavior idispensebehavior = (DispenserBehavior) DispenserBlock.BEHAVIORS.get(itemstack.getItem());

            if (idispensebehavior != DispenserBehavior.NOOP) {
                DispenserBlockHelper.eventFired = false;
                tileentitydispenser.setStack(i, idispensebehavior.dispense(sourceblock, itemstack));
            }

        }
    }

}
