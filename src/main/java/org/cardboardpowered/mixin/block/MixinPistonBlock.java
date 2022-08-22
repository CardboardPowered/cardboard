package org.cardboardpowered.mixin.block;

import java.util.AbstractList;
import java.util.List;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.cardboardpowered.extras.DualBlockList;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

//@MixinInfo(events = {"BlockPistonExtendEvent","BlockPistonRetractEvent","BlockPistonEvent"})
@Mixin(PistonBlock.class)
public class MixinPistonBlock {
    
    private PistonHandler cardboard_ph;

    @Redirect(at = @At(value = "NEW", target = "Lnet/minecraft/block/piston/PistonHandler;<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)V"), method = "move")
    public PistonHandler cardboard_storePH(World world, BlockPos pos, Direction dir, boolean retract) {
        return (cardboard_ph = new PistonHandler(world,pos,dir,retract));
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/piston/PistonHandler;getBrokenBlocks()Ljava/util/List;"),
            method = "move", cancellable = true)
    public void cardboard_doPistonEvents(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> ci) {

        final org.bukkit.block.Block bblock = ((IMixinWorld)world).getWorldImpl().getBlockAt(pos.getX(), pos.getY(), pos.getZ());

        final List<BlockPos> moved = cardboard_ph.getMovedBlocks();
        final List<BlockPos> broken = cardboard_ph.getBrokenBlocks();

        Direction enumdirection1 = retract ? cardboard_ph.pistonDirection : cardboard_ph.pistonDirection ;

        List<org.bukkit.block.Block> blocks = new DualBlockList(moved, broken, bblock.getWorld()) {

            @Override
            public int size() {
                return moved.size() + broken.size();
            }

            @Override
            public org.bukkit.block.Block get(int index) {
                if (index >= size() || index < 0)
                    throw new ArrayIndexOutOfBoundsException(index);
                BlockPos pos = (BlockPos) (index < moved.size() ? moved.get(index) : broken.get(index - moved.size()));
                return bblock.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
            }
        };
        BlockPistonEvent event = retract ? new BlockPistonExtendEvent(bblock, blocks, CraftBlock.notchToBlockFace(enumdirection1)) 
                        : new BlockPistonRetractEvent(bblock, blocks, CraftBlock.notchToBlockFace(enumdirection1));
        CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            for (BlockPos b : broken)
                world.updateListeners(b, Blocks.AIR.getDefaultState(), world.getBlockState(b), 3);
            for (BlockPos b : moved) {
                world.updateListeners(b, Blocks.AIR.getDefaultState(), world.getBlockState(b), 3);
                b = b.offset(enumdirection1);
                world.updateListeners(b, Blocks.AIR.getDefaultState(), world.getBlockState(b), 3);
            }
            ci.setReturnValue(false);
            return;
        }
    }

}