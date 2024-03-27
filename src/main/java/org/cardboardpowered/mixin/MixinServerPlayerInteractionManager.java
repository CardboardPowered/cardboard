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
package org.cardboardpowered.mixin;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinServerPlayerInteractionManager;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.CakeBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = ServerPlayerInteractionManager.class, priority = 999)
public class MixinServerPlayerInteractionManager implements IMixinServerPlayerInteractionManager {

    @Shadow public ServerPlayerEntity player;
    @Shadow public ServerWorld world;
    @Shadow private GameMode gameMode;
    @Shadow private boolean mining;
    @Shadow private int startMiningTime;
    @Shadow private BlockPos miningPos;
    @Shadow private int tickCounter;
    @Shadow private boolean failedToMine;
    @Shadow private BlockPos failedMiningPos;
    @Shadow private int failedStartMiningTime;
    @Shadow private int blockBreakingProgress;

    private int cb_stat = 0;
    private PlayerInteractEvent cb_ev;
    private float cb_f2 = 0;
    private BlockPos cb_pos;
    
    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;isCreative()Z"),
            method = "processBlockBreakingAction", cancellable = true)
    public void processBlockBreakkingAction_cb1(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
    	cb_stat = 0;
    	PlayerInteractEvent event = BukkitEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, pos, direction, this.player.getInventory().getMainHandStack(), Hand.MAIN_HAND);
        this.cb_ev = event;
    	// System.out.println("PlayerInteractEvent! " + pos.toString());
        if (event.isCancelled()) {
            for (Direction dir : Direction.values()) {
                this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, pos.offset(dir)));
            }
            this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, pos));
            BlockEntity tileentity = this.world.getBlockEntity(pos);
            if (tileentity != null) {
                this.player.networkHandler.sendPacket(tileentity.toUpdatePacket());
            }
            ci.cancel();
            return;
        }
    }
    
    @Redirect(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"))
    public float cb_2(BlockState instance, PlayerEntity playerEntity, BlockView blockView, BlockPos blockPos) {
    	this.cb_pos = blockPos;
    	float f2 = instance.calcBlockBreakingDelta(playerEntity, blockView, blockPos);
    	this.cb_f2 = f2;
        return f2;
    }
    
    @Inject(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isAir()Z"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void cb_3(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        // System.out.println("A = " + i);
    
    	if (cb_stat == 1) {
            if (cb_ev.useItemInHand() == Event.Result.DENY) {
                if (cb_f2 > 1.0f) {
                    this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, cb_pos));
                }
                return;
            }
            BlockDamageEvent blockEvent = BukkitEventFactory.callBlockDamageEvent(this.player, cb_pos.getX(), cb_pos.getY(), cb_pos.getZ(), this.player.getInventory().getMainHandStack(), cb_f2 >= 1.0f);
            if (blockEvent.isCancelled()) {
                this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, cb_pos));
                ci.cancel();
                return;
            }
            // System.out.println("INSTA CHECK");
            if (blockEvent.getInstaBreak()) {
                cb_f2 = 2.0f;
            }
    	}
    }
    
    @Redirect(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isAir()Z"))
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
	private void block_damage_abort_event(ServerPlayerInteractionManager instance, BlockPos pos, boolean success, int sequence, String reason) {
    	// TODO: Update our Paper-API
    	// BukkitEventFactory.callBlockDamageAbortEvent(this.player, pos, this.player.getInventory().getMainHandStack());
    	// instance.method_41250(pos, success, sequence, reason);
    }

    @Inject(at = @At("HEAD"), method = "tryBreakBlock", cancellable = true)
    public void blockBreak(BlockPos blockposition, CallbackInfoReturnable<Boolean> ci) {
        org.bukkit.block.Block bblock = CraftBlock.at(world, blockposition);

        boolean isSwordNoBreak = !this.player.getMainHandStack().getItem().canMine(this.world.getBlockState(blockposition), this.world, blockposition, this.player);
        if (world.getBlockEntity(blockposition) == null && !isSwordNoBreak) {
            BlockUpdateS2CPacket packet = new BlockUpdateS2CPacket(this.world, blockposition);
            // TODO 1.17ify packet.state = Blocks.AIR.getDefaultState();
            this.player.networkHandler.sendPacket(packet);
        }
        BlockBreakEvent event = new BlockBreakEvent(bblock, (Player) ((IMixinServerEntityPlayer)this.player).getBukkitEntity());
        event.setCancelled(isSwordNoBreak);

        CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            if (isSwordNoBreak)
                ci.setReturnValue(false);

            this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, blockposition)); // Let the client know the block still exists

            // Brute force all possible updates
            for (Direction dir : Direction.values())
                this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(world, blockposition.offset(dir)));

            // Update any tile entity data for this block
            BlockEntity tileentity = this.world.getBlockEntity(blockposition);
            if (tileentity != null)
                this.player.networkHandler.sendPacket(tileentity.toUpdatePacket());

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

    @Inject(at = @At("HEAD"), method = "interactBlock", cancellable = true)
    public void interactBlock(ServerPlayerEntity entityplayer, World world, ItemStack itemstack, Hand enumhand, BlockHitResult movingobjectpositionblock, CallbackInfoReturnable<ActionResult> ci) {
        ActionResult result = UseBlockCallback.EVENT.invoker().interact(entityplayer, world, enumhand, movingobjectpositionblock);

        if(result != null) {
            ci.setReturnValue(result);
            return;
        }

        BlockPos blockposition = movingobjectpositionblock.getBlockPos();
        BlockState iblockdata = world.getBlockState(blockposition);
        ActionResult enuminteractionresult = ActionResult.PASS;
        boolean cancelledBlock = false;

        if (this.gameMode == GameMode.SPECTATOR) {
            NamedScreenHandlerFactory itileinventory = iblockdata.createScreenHandlerFactory(world, blockposition);
            cancelledBlock = !(itileinventory instanceof NamedScreenHandlerFactory);
        }

        if (entityplayer.getItemCooldownManager().isCoolingDown(itemstack.getItem()))
            cancelledBlock = true;

        PlayerInteractEvent event = BukkitEventFactory.callPlayerInteractEvent(entityplayer, Action.RIGHT_CLICK_BLOCK, blockposition, movingobjectpositionblock.getSide(), itemstack, cancelledBlock, enumhand);
        firedInteract = true;
        interactResult = event.useItemInHand() == Event.Result.DENY;

        if (event.useInteractedBlock() == Event.Result.DENY) {
            // If we denied a door from opening, we need to send a correcting update to the client, as it already opened the door.
            if (iblockdata.getBlock() instanceof DoorBlock) {
                boolean bottom = iblockdata.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
                entityplayer.networkHandler.sendPacket(new BlockUpdateS2CPacket(world, bottom ? blockposition.up() : blockposition.down()));
            } else if (iblockdata.getBlock() instanceof CakeBlock) {
                // TODO ((PlayerImpl)((IMixinServerEntityPlayer)entityplayer).getBukkitEntity()).sendHealthUpdate();
            }
            ((PlayerImpl)((IMixinServerEntityPlayer)entityplayer).getBukkitEntity()).updateInventory();
            enuminteractionresult = (event.useItemInHand() != Event.Result.ALLOW) ? ActionResult.SUCCESS : ActionResult.PASS;
        } else if (this.gameMode == GameMode.SPECTATOR) {
            NamedScreenHandlerFactory itileinventory = iblockdata.createScreenHandlerFactory(world, blockposition);

            if (itileinventory != null) {
                entityplayer.openHandledScreen(itileinventory);
                ci.setReturnValue(ActionResult.SUCCESS);
            } else ci.setReturnValue(ActionResult.PASS);
            return;
        } else {
            boolean flag = !entityplayer.getMainHandStack().isEmpty() || !entityplayer.getOffHandStack().isEmpty();
            boolean flag1 = entityplayer.shouldCancelInteraction() && flag;
            ItemStack itemstack1 = itemstack.copy();

            if (!flag1) {
                enuminteractionresult = iblockdata.onUse(world, entityplayer, enumhand, movingobjectpositionblock);

                if (enuminteractionresult.isAccepted()) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger(entityplayer, blockposition, itemstack1);
                    ci.setReturnValue(enuminteractionresult);
                    return;
                }
            }

            if (!itemstack.isEmpty() && enuminteractionresult != ActionResult.SUCCESS && !interactResult) { // add !interactResult SPIGOT-764
                ItemUsageContext itemactioncontext = new ItemUsageContext(entityplayer, enumhand, movingobjectpositionblock);
                ActionResult enuminteractionresult1;

                if (this.gameMode.isCreative()) {
                    int i = itemstack.getCount();
                    enuminteractionresult1 = itemstack.useOnBlock(itemactioncontext/*, enumhand*/);
                    itemstack.setCount(i);
                } else enuminteractionresult1 = itemstack.useOnBlock(itemactioncontext/*, enumhand*/);

                if (enuminteractionresult1.isAccepted())
                    Criteria.ITEM_USED_ON_BLOCK.trigger(entityplayer, blockposition, itemstack1);

                ci.setReturnValue(enuminteractionresult1);
                return;
            }
        }
        ci.setReturnValue(enuminteractionresult);
    }

}
