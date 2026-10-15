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
package org.cardboardpowered.mixin.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.cardboardpowered.bridge.world.item.BlockItemBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;

@Mixin(value = BlockItem.class, priority = 999) // Priority 999 to allow Carpet Mod
public class BlockItemMixin implements BlockItemBridge {

    @Shadow public BlockState getPlacementState(BlockPlaceContext blockactioncontext) {return null;}
    // @Shadow public BlockState placeFromTag(BlockPos blockposition, World world, ItemStack itemstack, BlockState iblockdata) {return null;}
    @Shadow public BlockPlaceContext updatePlacementContext(BlockPlaceContext blockactioncontext) {return null;}
    @Shadow public boolean placeBlock(BlockPlaceContext blockactioncontext, BlockState iblockdata) {return false;}
    @Shadow public boolean updateCustomBlockEntityTag(BlockPos blockposition, Level world, Player entityhuman, ItemStack itemstack, BlockState iblockdata) {return false;}
    @Shadow protected boolean mustSurvive() {return false;}

    private org.bukkit.block.BlockState bukkit_state;

    
    //@Shadow
    //public static BlockState with( BlockState state, Property property, String name) {
    //	return null; // Shadow method
    //}
   //  Lnet/minecraft/item/BlockItem;with(Lnet/minecraft/block/BlockState;Lnet/minecraft/state/property/Property;Ljava/lang/String;)Lnet/minecraft/block/BlockState;
    
    /**
     * @author Cardboard
     * @reason Fix LilyPad BlockState
     */
    @Inject(at = @At(value = "INVOKE_ASSIGN", target = 
            "Lnet/minecraft/world/item/BlockItem;getPlacementState(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;"), 
            method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;")
    public void bukkitWaterlilyPlacementFix(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> ci) {
        bukkit_state = null;
        if (((BlockItem)(Object)this) instanceof PlaceOnWaterBlockItem)
            bukkit_state = org.bukkit.craftbukkit.block.CraftBlockStates.getBlockState(context.getLevel(), context.getClickedPos());
    }

    /**
     * @reason BlockPlaceEvent for LilyPad
     */
    @Inject(at = @At(value = "INVOKE_ASSIGN", target =
            "Lnet/minecraft/world/item/BlockItem;updateCustomBlockEntityTag(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z"),
            method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", cancellable = true)
    public void doBukkitEvent_DoBlockPlaceEventForWaterlilies(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> ci) {
        // Singleplayer: the client predicts placement on its own ClientLevel; only the server fires Bukkit events.
        if (context.getLevel().isClientSide()) return;
        if (bukkit_state != null) {
            BlockPos pos = context.getClickedPos();
            Level world = context.getLevel();
            Player entityhuman = context.getPlayer();

            BlockPlaceEvent placeEvent = CraftEventFactory.callBlockPlaceEvent((ServerLevel) world, entityhuman, context.getHand(), bukkit_state, pos);
            if (placeEvent.isCancelled() || !placeEvent.canBuild()) {
                bukkit_state.update(true, false);
                ci.setReturnValue(InteractionResult.FAIL);
            }
        }
    }

    /**
     * @reason BlockCanBuildEvent
     */
    @Inject(at = @At("RETURN"), method = "canPlace(Lnet/minecraft/world/item/context/BlockPlaceContext;Lnet/minecraft/world/level/block/state/BlockState;)Z", cancellable = true)
    public void doBukkitEvent_BlockCanBuildEvent(BlockPlaceContext blockactioncontext, BlockState iblockdata, CallbackInfoReturnable<Boolean> ci) {
        if (blockactioncontext.getLevel().isClientSide()) return;
        Player entityhuman = blockactioncontext.getPlayer();
        CollisionContext voxelshapecollision = entityhuman == null ? CollisionContext.empty() : CollisionContext.of((Entity) entityhuman);

        boolean defaultReturn = (!this.mustSurvive() || iblockdata.canSurvive(blockactioncontext.getLevel(), blockactioncontext.getClickedPos())) && blockactioncontext.getLevel().isUnobstructed(iblockdata, blockactioncontext.getClickedPos(), voxelshapecollision);
        org.bukkit.entity.Player player = (blockactioncontext.getPlayer() instanceof ServerPlayer) ? (org.bukkit.entity.Player) ((ServerPlayerBridge)blockactioncontext.getPlayer()).getBukkitEntity() : null;

        BlockCanBuildEvent event = new BlockCanBuildEvent(CraftBlock.at((ServerLevel) blockactioncontext.getLevel(), blockactioncontext.getClickedPos()), player, CraftBlockData.fromData(iblockdata), defaultReturn);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        ci.setReturnValue(event.isBuildable());
    }

}
