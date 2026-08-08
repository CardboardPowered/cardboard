/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2021 Cardboard contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 */
package org.cardboardpowered.mixin.server.level;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerGameModeBridge;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = ServerPlayerGameMode.class, priority = 999)
public class ServerPlayerGameModeMixin implements ServerPlayerGameModeBridge {

    @Shadow public ServerPlayer player;
    @Shadow public ServerLevel level;
    @Shadow private GameType gameModeForPlayer;
    @Shadow private boolean isDestroyingBlock;
    @Shadow private int destroyProgressStart;
    @Shadow private BlockPos destroyPos;
    @Shadow private int gameTicks;
    @Shadow private boolean hasDelayedDestroy;
    @Shadow private BlockPos delayedDestroyPos;
    @Shadow private int delayedTickStart;
    @Shadow private int lastSentState;

    private int cb_stat = 0;
    private PlayerInteractEvent cb_ev;
    private float cb_f2 = 0;
    private BlockPos cb_pos;
    
    @Inject(
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;instabuild:Z"),
            method = "handleBlockBreakAction", cancellable = true)
    public void processBlockBreakkingAction_cb1(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
    	cb_stat = 0;
    	PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, pos, direction, this.player.getInventory().getSelectedItem(), InteractionHand.MAIN_HAND);

    	this.cb_ev = event;
    	// System.out.println("PlayerInteractEvent! " + pos.toString());
        if (event.isCancelled()) {
            for (Direction dir : Direction.values()) {
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, pos.relative(dir)));
            }
            this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, pos));
            BlockEntity tileentity = this.level.getBlockEntity(pos);
            if (tileentity != null) {
                this.player.connection.send(tileentity.getUpdatePacket());
            }
            ci.cancel();
            return;
        }
    }
    
    @Redirect(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    public float cb_2(BlockState instance, net.minecraft.world.entity.player.Player playerEntity, BlockGetter blockView, BlockPos blockPos) {
    	this.cb_pos = blockPos;
    	float f2 = instance.getDestroyProgress(playerEntity, blockView, blockPos);
    	this.cb_f2 = f2;
        return f2;
    }
    
    @Inject(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void cb_3(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        // System.out.println("A = " + i);
    
    	if (cb_stat == 1) {
            if (cb_ev.useItemInHand() == Event.Result.DENY) {
                if (cb_f2 > 1.0f) {
                    this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, cb_pos));
                }
                return;
            }
            BlockDamageEvent blockEvent = CraftEventFactory.callBlockDamageEvent(this.player, cb_pos.getX(), cb_pos.getY(), cb_pos.getZ(), this.player.getInventory().getSelectedItem(), cb_f2 >= 1.0f);
            if (blockEvent.isCancelled()) {
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, cb_pos));
                ci.cancel();
                return;
            }
            // System.out.println("INSTA CHECK");
            if (blockEvent.getInstaBreak()) {
                cb_f2 = 2.0f;
            }
    	}
    }
    
    @Redirect(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    public boolean cb_4(BlockState instance) {
    	// CraftBukkit: - Swings at air do *NOT* exist.
    	if (cb_stat == 0 && cb_ev.useInteractedBlock() == Event.Result.DENY) {
    		return true;
    	}
    	
    	cb_stat += 1;
    	
		return instance.isAir();
    }
    
    /*@Redirect(
            method = "processBlockBreakingAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;method_41250(Lnet/minecraft/util/math/BlockPos;ZILjava/lang/String;)V"
            )
    )*/
    @SuppressWarnings("unused")
	private void block_damage_abort_event(ServerPlayerGameMode instance, BlockPos pos, boolean success, int sequence, String reason) {
    	// TODO: Update our Paper-API
    	// CraftEventFactory.callBlockDamageAbortEvent(this.player, pos, this.player.getInventory().getSelectedStack());
    	// instance.method_41250(pos, success, sequence, reason);
    }

    @Inject(at = @At("HEAD"), method = "destroyBlock", cancellable = true)
    public void blockBreak(BlockPos blockposition, CallbackInfoReturnable<Boolean> ci) {
        org.bukkit.block.Block bblock = CraftBlock.at(level, blockposition);

        boolean isSwordNoBreak = !this.player.getMainHandItem().canDestroyBlock(this.level.getBlockState(blockposition), this.level, blockposition, this.player);
        if (level.getBlockEntity(blockposition) == null && !isSwordNoBreak) {
            ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(this.level, blockposition);
            // TODO 1.17ify packet.state = Blocks.AIR.getDefaultState();
            this.player.connection.send(packet);
        }
        BlockBreakEvent event = new BlockBreakEvent(bblock, (Player) ((ServerPlayerBridge)this.player).getBukkitEntity());
        event.setCancelled(isSwordNoBreak);

        CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            if (isSwordNoBreak)
                ci.setReturnValue(false);

            this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, blockposition)); // Let the client know the block still exists

            // Brute force all possible updates
            for (Direction dir : Direction.values())
                this.player.connection.send(new ClientboundBlockUpdatePacket(level, blockposition.relative(dir)));

            // Update any tile entity data for this block
            BlockEntity tileentity = this.level.getBlockEntity(blockposition);
            if (tileentity != null)
                this.player.connection.send(tileentity.getUpdatePacket());

            ci.setReturnValue(false);
            return;
        }
    }

    public boolean interactResult = false;
    public boolean firedInteract = false;

    @Override
    public boolean getFiredInteractBF() {
        return firedInteract;
    }

    @Override
    public void setFiredInteractBF(boolean b) {
        this.firedInteract = b;
    }

    @Override
    public boolean getInteractResultBF() {
        return interactResult;
    }

    @Override
    public void setInteractResultBF(boolean b) {
        this.interactResult = b;
    }

    @Inject(at = @At("HEAD"), method = "useItemOn", cancellable = true)
    public void interactBlock(ServerPlayer entityplayer, Level world, ItemStack itemstack, InteractionHand enumhand, BlockHitResult movingobjectpositionblock, CallbackInfoReturnable<InteractionResult> ci) {
        InteractionResult result = UseBlockCallback.EVENT.invoker().interact(entityplayer, world, enumhand, movingobjectpositionblock);

        if (result != InteractionResult.PASS) {
        	ci.setReturnValue(result);
            return;
        }

        BlockPos blockposition = movingobjectpositionblock.getBlockPos();
        BlockState iblockdata = world.getBlockState(blockposition);
        InteractionResult enuminteractionresult = result;// ActionResult.PASS;
        boolean cancelledBlock = false;

        if (this.gameModeForPlayer == GameType.SPECTATOR) {
            MenuProvider itileinventory = iblockdata.getMenuProvider(world, blockposition);
            cancelledBlock = !(itileinventory instanceof MenuProvider);
        }

        if (entityplayer.getCooldowns().isOnCooldown(itemstack))
            cancelledBlock = true;

        PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(entityplayer, Action.RIGHT_CLICK_BLOCK, blockposition, movingobjectpositionblock.getDirection(), itemstack, cancelledBlock, enumhand);
        firedInteract = true;
        interactResult = event.useItemInHand() == Event.Result.DENY;

        if (event.useInteractedBlock() == Event.Result.DENY) {
            // If we denied a door from opening, we need to send a correcting update to the client, as it already opened the door.
            if (iblockdata.getBlock() instanceof DoorBlock) {
                boolean bottom = iblockdata.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
                entityplayer.connection.send(new ClientboundBlockUpdatePacket(world, bottom ? blockposition.above() : blockposition.below()));
            } else if (iblockdata.getBlock() instanceof CakeBlock) {
                // TODO ((CraftPlayer)((IMixinServerEntityPlayer)entityplayer).getBukkitEntity()).sendHealthUpdate();
            }
            ((CraftPlayer)((ServerPlayerBridge)entityplayer).getBukkitEntity()).updateInventory();
            enuminteractionresult = (event.useItemInHand() != Event.Result.ALLOW) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        } else if (this.gameModeForPlayer == GameType.SPECTATOR) {
            MenuProvider itileinventory = iblockdata.getMenuProvider(world, blockposition);

            if (itileinventory != null) {
                entityplayer.openMenu(itileinventory);
                ci.setReturnValue(InteractionResult.SUCCESS);
            } else ci.setReturnValue(InteractionResult.PASS);
            return;
        } else {
            boolean flag = !entityplayer.getMainHandItem().isEmpty() || !entityplayer.getOffhandItem().isEmpty();
            boolean flag1 = entityplayer.isSecondaryUseActive() && flag;
            ItemStack itemstack1 = itemstack.copy();

            if (!flag1) {
            	
            	 InteractionResult enuminteractionresult1 = iblockdata.useItemOn(player.getItemInHand(enumhand), world, player, enumhand, movingobjectpositionblock);
                 if (enuminteractionresult1.consumesAction()) {
                     CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, blockposition, itemstack1);
                     ci.setReturnValue(enuminteractionresult1);
                     return;
                 }
                 if (enuminteractionresult1 instanceof InteractionResult.TryEmptyHandInteraction && enumhand == InteractionHand.MAIN_HAND && (enuminteractionresult = iblockdata.useWithoutItem(world, player, movingobjectpositionblock)).consumesAction()) {
                     CriteriaTriggers.DEFAULT_BLOCK_USE.trigger(player, blockposition);
                     ci.setReturnValue(enuminteractionresult1);
                     return;
                 }
            	
                 /*
                  Old 1.21.1:
            	ItemActionResult iteminteractionresult = iblockdata.onUseWithItem(player.getStackInHand(enumhand), world, player, enumhand, movingobjectpositionblock);
            	
            	if (iteminteractionresult.isAccepted()) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger(player, blockposition, itemstack1);
                    ci.setReturnValue(iteminteractionresult.toActionResult());
                    return;
                }
                if (iteminteractionresult == ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && enumhand == Hand.MAIN_HAND && (enuminteractionresult = iblockdata.onUse(world, player, movingobjectpositionblock)).isAccepted()) {
                    Criteria.DEFAULT_BLOCK_USE.trigger(player, blockposition);
                    ci.setReturnValue(enuminteractionresult);
                    return;
                }
                */
            	
                // old 1.20.4:
            	/*
                enuminteractionresult = iblockdata.onUse(world, entityplayer, enumhand, movingobjectpositionblock);
                if (enuminteractionresult.isAccepted()) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger(entityplayer, blockposition, itemstack1);
                    ci.setReturnValue(enuminteractionresult);
                    return;
                }
                */
            }

            if (!itemstack.isEmpty() && enuminteractionresult != InteractionResult.SUCCESS && !interactResult) { // add !interactResult SPIGOT-764
                UseOnContext itemactioncontext = new UseOnContext(entityplayer, enumhand, movingobjectpositionblock);
                InteractionResult enuminteractionresult1;

                if (this.gameModeForPlayer.isCreative()) {
                    int i = itemstack.getCount();
                    enuminteractionresult1 = itemstack.useOn(itemactioncontext/*, enumhand*/);
                    itemstack.setCount(i);
                } else enuminteractionresult1 = itemstack.useOn(itemactioncontext/*, enumhand*/);

                if (enuminteractionresult1.consumesAction())
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(entityplayer, blockposition, itemstack1);

                ci.setReturnValue(enuminteractionresult1);
                return;
            }
        }
        ci.setReturnValue(enuminteractionresult);
    }

}
