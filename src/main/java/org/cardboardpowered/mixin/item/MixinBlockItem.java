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
package org.cardboardpowered.mixin.item;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
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
    // @Shadow public BlockState placeFromTag(BlockPos blockposition, World world, ItemStack itemstack, BlockState iblockdata) {return null;}
    @Shadow public ItemPlacementContext getPlacementContext(ItemPlacementContext blockactioncontext) {return null;}
    @Shadow public boolean place(ItemPlacementContext blockactioncontext, BlockState iblockdata) {return false;}
    @Shadow public boolean postPlacement(BlockPos blockposition, World world, PlayerEntity entityhuman, ItemStack itemstack, BlockState iblockdata) {return false;}
    @Shadow protected boolean checkStatePlacement() {return false;}

    private org.bukkit.block.BlockState bukkit_state;

    /**
     * @author Cardboard
     * @reason Fix LilyPad BlockState
     */
    @Inject(at = @At(value = "INVOKE_ASSIGN", target = 
            "Lnet/minecraft/item/BlockItem;getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;"), 
            method = "place", cancellable = true)
    public void bukkitWaterlilyPlacementFix(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> ci) {
        bukkit_state = null;
        if (((BlockItem)(Object)this) instanceof LilyPadItem)
            bukkit_state = org.bukkit.craftbukkit.block.CraftBlockState.getBlockState(context.getWorld(), context.getBlockPos());
    }

    /**
     * @reason BlockPlaceEvent for LilyPad
     */
    @Inject(at = @At(value = "INVOKE_ASSIGN", target =
            "Lnet/minecraft/item/BlockItem;postPlacement(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/BlockState;)Z"),
            method = "place", cancellable = true)
    public void doBukkitEvent_DoBlockPlaceEventForWaterlilies(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> ci) {
        if (bukkit_state != null) {
            BlockPos pos = context.getBlockPos();
            World world = context.getWorld();
            PlayerEntity entityhuman = context.getPlayer();

            BlockPlaceEvent placeEvent = BukkitEventFactory.callBlockPlaceEvent((ServerWorld) world, entityhuman, context.getHand(), bukkit_state, pos.getX(), pos.getY(), pos.getZ());
            if (placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild())) {
                bukkit_state.update(true, false);
                ci.setReturnValue(ActionResult.FAIL);
                return;
            }
        }
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