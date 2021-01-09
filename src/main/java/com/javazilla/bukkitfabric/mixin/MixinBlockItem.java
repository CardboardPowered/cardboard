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
package com.javazilla.bukkitfabric.mixin;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinServerPlayerInteractionManager;

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

@Mixin(value = BlockItem.class, priority = 999) // Priority 999 to allow Carpet Mod
public class MixinBlockItem {

    @Shadow public BlockState getPlacementState(ItemPlacementContext blockactioncontext) {return null;}
    @Shadow public BlockState placeFromTag(BlockPos blockposition, World world, ItemStack itemstack, BlockState iblockdata) {return null;}
    @Shadow public ItemPlacementContext getPlacementContext(ItemPlacementContext blockactioncontext) {return null;}
    @Shadow public boolean place(ItemPlacementContext blockactioncontext, BlockState iblockdata) {return false;}
    @Shadow public boolean postPlacement(BlockPos blockposition, World world, PlayerEntity entityhuman, ItemStack itemstack, BlockState iblockdata) {return false;}
    @Shadow protected boolean checkStatePlacement() {return false;}

    /**
     * We basically @Overwrite the whole method but
     * we use @Inject to allow other mods to not crash when injecting code
     * (ex. Carpet Mod)
     * 
     * @author BukkitFabric
     * @reason Bukkit Overwrite
     */
    @Inject(at = @At("HEAD"), method = "place", cancellable = true)
    public void place1(ItemPlacementContext blockactioncontext, CallbackInfoReturnable<ActionResult> ci) {
        if (!blockactioncontext.canPlace()) {
            ci.setReturnValue(ActionResult.FAIL);
            return;
        } else {
            ItemPlacementContext blockactioncontext1 = this.getPlacementContext(blockactioncontext);

            if (blockactioncontext1 == null) {
                ci.setReturnValue(ActionResult.FAIL);
                return;
            } else {
                BlockState iblockdata = this.getPlacementState(blockactioncontext1);
                org.bukkit.block.BlockState blockstate = null;
                if (((BlockItem)(Object)this) instanceof LilyPadItem)
                    blockstate = org.bukkit.craftbukkit.block.CraftBlockState.getBlockState(blockactioncontext1.getWorld(), blockactioncontext1.getBlockPos());

                boolean no = ((IMixinServerPlayerInteractionManager)((ServerPlayerEntity)blockactioncontext1.getPlayer()).interactionManager).getFiredInteractBF();
                if (!no) {
                    System.out.println("NO!");
                    ((IMixinServerPlayerInteractionManager)((ServerPlayerEntity)blockactioncontext1.getPlayer()).interactionManager).setFiredInteractBF(false);
                    ci.setReturnValue(ActionResult.FAIL);
                    return;
                }

                if (iblockdata == null) {
                    ci.setReturnValue(ActionResult.FAIL);
                    return;
                } else if (!this.place(blockactioncontext1, iblockdata)) {
                    ci.setReturnValue(ActionResult.FAIL);
                    return;
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
                            org.bukkit.event.block.BlockPlaceEvent placeEvent = BukkitEventFactory.callBlockPlaceEvent((ServerWorld) world, entityhuman, blockactioncontext1.getHand(), blockstate, blockposition.getX(), blockposition.getY(), blockposition.getZ());
                            if (placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild())) {
                                blockstate.update(true, false);
                                ci.setReturnValue(ActionResult.FAIL);
                                return;
                            }
                        }
                        if (entityhuman instanceof ServerPlayerEntity)
                            Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity) entityhuman, blockposition, itemstack);
                    }
                    if ((entityhuman == null || !entityhuman.abilities.creativeMode) && itemstack != ItemStack.EMPTY)
                        itemstack.decrement(1);

                    ci.setReturnValue(ActionResult.success(world.isClient));
                }
            }
        }
        ci.setReturnValue(ActionResult.PASS);
    }

    /**
     * @reason BlockCanBuildEvent
     */
    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/item/BlockItem;canPlace(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", cancellable = true)
    public void doBukkitEvent_BlockCanBuildEvent(ItemPlacementContext blockactioncontext, BlockState iblockdata, CallbackInfoReturnable<Boolean> ci) {
        PlayerEntity entityhuman = blockactioncontext.getPlayer();
        ShapeContext voxelshapecollision = entityhuman == null ? ShapeContext.absent() : ShapeContext.of((Entity) entityhuman);

        boolean defaultReturn = (!this.checkStatePlacement() || iblockdata.canPlaceAt(blockactioncontext.getWorld(), blockactioncontext.getBlockPos())) && blockactioncontext.getWorld().canPlace(iblockdata, blockactioncontext.getBlockPos(), voxelshapecollision);
        org.bukkit.entity.Player player = (blockactioncontext.getPlayer() instanceof ServerPlayerEntity) ? (org.bukkit.entity.Player) ((IMixinServerEntityPlayer)blockactioncontext.getPlayer()).getBukkitEntity() : null;

        BlockCanBuildEvent event = new BlockCanBuildEvent(CraftBlock.at((ServerWorld) blockactioncontext.getWorld(), blockactioncontext.getBlockPos()), player, CraftBlockData.fromData(iblockdata), defaultReturn);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        ci.setReturnValue(event.isBuildable());
    }

}