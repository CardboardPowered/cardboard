package com.javazilla.bukkitfabric.mixin;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LilyPadItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BlockItem.class)
public class MixinBlockItem {

    @Shadow public BlockState getPlacementState(ItemPlacementContext blockactioncontext) {return null;}
    @Shadow public BlockState placeFromTag(BlockPos blockposition, World world, ItemStack itemstack, BlockState iblockdata) {return null;}
    @Shadow public ItemPlacementContext getPlacementContext(ItemPlacementContext blockactioncontext) {return null;}
    @Shadow public boolean place(ItemPlacementContext blockactioncontext, BlockState iblockdata) {return false;}
    @Shadow public boolean postPlacement(BlockPos blockposition, World world, PlayerEntity entityhuman, ItemStack itemstack, BlockState iblockdata) {return false;}
    @Shadow protected boolean checkStatePlacement() {return false;}

    /**
     * @author BukkitFabric
     * @reason Bukkit Overwrite
     */
    @Overwrite
    public ActionResult place(ItemPlacementContext blockactioncontext) {
        if (!blockactioncontext.canPlace()) {
            return ActionResult.FAIL;
        } else {
            ItemPlacementContext blockactioncontext1 = this.getPlacementContext(blockactioncontext);

            if (blockactioncontext1 == null) {
                return ActionResult.FAIL;
            } else {
                BlockState iblockdata = this.getPlacementState(blockactioncontext1);
                org.bukkit.block.BlockState blockstate = null;
                if (((BlockItem)(Object)this) instanceof LilyPadItem)
                    blockstate = org.bukkit.craftbukkit.block.CraftBlockState.getBlockState(blockactioncontext1.getWorld(), blockactioncontext1.getBlockPos());

                if (iblockdata == null) {
                    return ActionResult.FAIL;
                } else if (!this.place(blockactioncontext1, iblockdata)) {
                    return ActionResult.FAIL;
                } else {
                    BlockPos blockposition = blockactioncontext1.getBlockPos();
                    World world = blockactioncontext1.getWorld();
                    PlayerEntity entityhuman = blockactioncontext1.getPlayer();
                    ItemStack itemstack = blockactioncontext1.getStack();
                    BlockState iblockdata1 = world.getBlockState(blockposition);
                    Block block = iblockdata1.getBlock();

                    if (block == iblockdata.getBlock()) {
                        iblockdata1 = this.placeFromTag(blockposition, world, itemstack, iblockdata1);
                        this.postPlacement(blockposition, world, entityhuman, itemstack, iblockdata1);
                        block.onPlaced(world, blockposition, iblockdata1, entityhuman, itemstack);

                        if (blockstate != null) {
                            org.bukkit.event.block.BlockPlaceEvent placeEvent = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockPlaceEvent((ServerWorld) world, entityhuman, blockactioncontext1.getHand(), blockstate, blockposition.getX(), blockposition.getY(), blockposition.getZ());
                            if (placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild())) {
                                blockstate.update(true, false);
                                return ActionResult.FAIL;
                            }
                        }
                        if (entityhuman instanceof ServerPlayerEntity)
                            Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity) entityhuman, blockposition, itemstack);
                    }
                    if ((entityhuman == null || !entityhuman.abilities.creativeMode) && itemstack != ItemStack.EMPTY)
                        itemstack.decrement(1);

                    return ActionResult.success(world.isClient);
                }
            }
        }
    }

    /**
     * @reason BlockCanBuildEvent
     * @author BukkitFabric
     */
    @Overwrite
    public boolean canPlace(ItemPlacementContext blockactioncontext, BlockState iblockdata) {
        PlayerEntity entityhuman = blockactioncontext.getPlayer();
        ShapeContext voxelshapecollision = entityhuman == null ? ShapeContext.absent() : ShapeContext.of((Entity) entityhuman);

        boolean defaultReturn = (!this.checkStatePlacement() || iblockdata.canPlaceAt(blockactioncontext.getWorld(), blockactioncontext.getBlockPos())) && blockactioncontext.getWorld().canPlace(iblockdata, blockactioncontext.getBlockPos(), voxelshapecollision);
        org.bukkit.entity.Player player = (blockactioncontext.getPlayer() instanceof ServerPlayerEntity) ? (org.bukkit.entity.Player) ((IMixinServerEntityPlayer)blockactioncontext.getPlayer()).getBukkitEntity() : null;

        BlockCanBuildEvent event = new BlockCanBuildEvent(CraftBlock.at((ServerWorld) blockactioncontext.getWorld(), blockactioncontext.getBlockPos()), player, CraftBlockData.fromData(iblockdata), defaultReturn);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);

        return event.isBuildable();
    }

}